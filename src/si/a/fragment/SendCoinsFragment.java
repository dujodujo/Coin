package si.a.fragment;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;

import si.a.adapter.AutoCompleteAddressAdapter;
import si.a.adapter.TransactionsAdapter;
import si.a.application.WalletApplication;
import si.a.provider.AddressBookProvider;
import si.a.provider.ExchangeRateLoader;
import si.a.provider.ExchangeRateProvider;
import si.a.provider.ExchangeRateProvider.ExchangeRate;
import si.a.util.AddressLabel;
import si.a.util.Constants;
import si.a.util.CurrencyAmount;
import si.a.util.WalletUtils;
import si.a.coin.AbstractWalletActivity;
import si.a.coin.SendCoinsActivity;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;
import si.a.wiew.CurrencyAmountView;
import si.a.wiew.CurrencyAmountView.CurrencyListener;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public final class SendCoinsFragment extends Fragment {
	
	public static final String TAG = SendCoinsFragment.class.getName();
	private static final Logger log = LoggerFactory.getLogger(SendCoinsFragment.class);
	
	private static final int ID_RATE_LOADER = 0;
	
	private SendCoinsActivity activity;
	private WalletApplication walletApplication;
	private Wallet wallet;
	private ContentResolver contentResolver;
	private LoaderManager loaderManager;
	private SharedPreferences sharedPreferences;
	
	private Handler handler = new Handler();
	private Handler backgroundHandler;
	private HandlerThread backgroundThread;
	
	private AutoCompleteTextView receivingAddressView;
	private View receivingStaticView;
	private TextView receivingStaticAddressView;
	private TextView receivingStaticLabelView;
	private CurrencyAmount currencyAmount;
	private ListView sentTransactionListView;
	private TransactionsAdapter sentTransactionAdapter;
	private AddressLabel validatedAddress;
	private Transaction sentTransaction;
	
	private Button start;
	private Button cancel;
	private State state;
	
	private enum State { INPUT, PREPARATION, SENDING, SENT, FAILED }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "onAttach");
		
		this.activity = (SendCoinsActivity) activity;
		this.walletApplication = (WalletApplication) activity.getApplication();
		this.wallet = walletApplication.getWallet();
		this.contentResolver = activity.getContentResolver();
		this.loaderManager = getLoaderManager();
		this.state = State.INPUT;
		this.sentTransaction = null;
		this.validatedAddress = null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		return inflater.inflate(R.layout.send_coins_fragment, container, false);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.send_coins_fragment_options, menu);
		MenuItem scanAction = (MenuItem) menu.findItem(R.id.send_coins_options_scan);
		scanAction.setVisible(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.send_coins_options_scan:
				//startScan();
				return true;
			case R.id.send_coins_options_empty:
				//startEmpty();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		receivingAddressView = (AutoCompleteTextView) view.findViewById(R.id.send_coins_receiving_address);
		receivingAddressView.setAdapter(new AutoCompleteAddressAdapter(activity, null));
		//receivingAddressView.setOnFocusChangeListener(receivingAddressListener);
		//receivingAddressView.addTextChangedListener(receivingAddressListener);
		
		receivingStaticView = view.findViewById(R.id.send_coins_receiving_static);
		receivingStaticAddressView = (TextView) view.findViewById(R.id.send_coins_receiving_static_address);
		receivingStaticLabelView = (TextView) view.findViewById(R.id.send_coins_receiving_static_label);
		
		CurrencyAmountView coinAmountView = (CurrencyAmountView) view.findViewById(R.id.send_coins_amount);
		coinAmountView.setCurrencySymbol(Constants.CURRENCY_CODE_COIN);
		CurrencyAmountView localAmountView = (CurrencyAmountView) view.findViewById(R.id.send_coins_amount_local);
		currencyAmount = new CurrencyAmount(coinAmountView, localAmountView);
		
		sentTransactionListView = (ListView) view.findViewById(R.id.send_coins_transaction_list);
		sentTransactionAdapter = new TransactionsAdapter(activity, wallet, 3);
		sentTransactionListView.setAdapter(sentTransactionAdapter);
		
		start = (Button) view.findViewById(R.id.send_coins_go);		
		start.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {}
		});
		
		cancel = (Button) view.findViewById(R.id.send_coins_cancel);
		cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				activity.finish();
			}
		});
		
		if(savedInstanceState != null) {
			restoreInstanceState(savedInstanceState);
		} else {
			Intent intent = activity.getIntent();
			String action = intent.getAction();
			Uri intentUri = intent.getData();
			String scheme = intentUri != null ? intentUri.getScheme() : null;
			
			if(Intent.ACTION_VIEW.equals(action) && intentUri != null && "bitcoin".equals(scheme)) {
				Log.i(TAG, "start State Coin");
				//startStateCoinUri(scheme);
			} else if(intent.hasExtra(SendCoinsActivity.INTENT_ADDRESS)) {
				Log.i(TAG, "start Intent Extras");
				//startIntentExtras(intent.getExtras());
			}
		}
	}
	
	private void startIntentExtras(Bundle extras) {
		Log.i(TAG, "start Intent Extras");
		
		String address = extras.getString(SendCoinsActivity.INTENT_ADDRESS);
		String addressLabel = extras.getString(SendCoinsActivity.INTENT_ADDRESS_LABEL);
		BigInteger amount = (BigInteger) extras.getSerializable(SendCoinsActivity.INTENT_AMOUNT);
		
		update(address, addressLabel, amount);
	}
	
	private LoaderCallbacks<Cursor> rateLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

		public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new ExchangeRateLoader(activity);
		}

		public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
			if(data != null) {
				data.moveToFirst();
				final ExchangeRate exchangeRate = ExchangeRateProvider.getExchangeRate(data);
				if(state == State.INPUT)
					currencyAmount.setExchangeRate(exchangeRate);
			}
		}

		public void onLoaderReset(android.support.v4.content.Loader<Cursor> arg0) {}
	};
	
	private void restoreInstanceState(Bundle savedInstanceState) {}
	
	public void update(String address, String addressLabel, BigInteger amount) {
		Log.i(TAG, "update");

		try {
			receivingAddressView.setText(null);
			validatedAddress = new AddressLabel(Constants.NETWORK_PARAMETERS, address, addressLabel);
		} catch(Exception e) {
			receivingAddressView.setText(address);
			validatedAddress = null;
		}

		receivingAddressView.setText(null);
		validatedAddress = null;
		updateView();
	}
	
	private void updateView() {
		Log.i(TAG, "updateView");

		if(validatedAddress != null) {
			receivingAddressView.setVisibility(View.GONE);
			receivingStaticAddressView.setVisibility(View.VISIBLE);
			receivingStaticAddressView.setText(WalletUtils.formatAddress(validatedAddress.address, 
				Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE));
			
			String addressBookLabel = AddressBookProvider.resolveLabel(activity, validatedAddress.address.toString());
			String staticLabel;
			
			if(addressBookLabel != null)
				staticLabel = addressBookLabel;
			else if(validatedAddress.label != null)
				staticLabel = validatedAddress.label;
			else
				staticLabel = getString(R.string.address_unlabel);
			
			receivingStaticLabelView.setText(staticLabel);
			receivingStaticLabelView.setTextColor(getResources().getColor(validatedAddress.label != null ? Color.BLACK : Color.RED));
		} else {
			receivingStaticView.setVisibility(View.GONE);
			receivingAddressView.setVisibility(View.VISIBLE);
		}

		receivingAddressView.setEnabled(state == State.INPUT);
		receivingStaticView.setEnabled(state == State.INPUT);
		
		if(sentTransaction != null) {
			sentTransactionListView.setVisibility(View.VISIBLE);
			sentTransactionAdapter.setPrecision(Constants.COIN_MAX_PRECISION);
			sentTransactionAdapter.replace(sentTransaction);
		} else {
			sentTransactionListView.setVisibility(View.GONE);
			sentTransactionAdapter.clear();
		}

		if(state == State.INPUT) {
			cancel.setText(R.string.button_cancel);
			start.setText(R.string.send_coins_button_send);
		} else if(state == State.PREPARATION) {
			cancel.setText(R.string.button_cancel);
			start.setText(R.string.send_coins_preparing_message);
		} else if(state == State.SENDING) {
			cancel.setText(R.string.send_coins_fragment_button_back);
			start.setText(R.string.send_coins_sending_message);
		} else if(state == State.SENT) {
			cancel.setText(R.string.send_coins_fragment_button_back);
			start.setText(R.string.send_coins_sent_message);
		} else if(state == State.FAILED) {
			cancel.setText(R.string.send_coins_fragment_button_back);
			start.setText(R.string.send_coins_failed_message);
		}
	}
	
	private final ContentObserver contentObserver = new ContentObserver(handler) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			updateView();
		}
	};
	
	private CurrencyAmountView.CurrencyListener amountListener = new CurrencyListener() {

		public void changed() {}

		public void done() {}

		public void focusChanged(boolean hasFocus) {
			start.requestFocusFromTouch();
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		
		contentResolver.registerContentObserver(AddressBookProvider.contentUri(activity.getPackageName()), true, contentObserver);
		currencyAmount.setListener(amountListener);
		this.loaderManager.initLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);
		updateView();
	}

	@Override
	public void onPause() {
		super.onPause();
		
		loaderManager.destroyLoader(ID_RATE_LOADER);
		currencyAmount.setListener(null);
		contentResolver.unregisterContentObserver(contentObserver);
	}
	
	private void startScan() {
		Log.i(TAG, "startScan");
	}
	
	private void startEmpty() {
		//final BigInteger availableAmount = wallet.getBalance(BalanceType.AVAILABLE);
		//Log.i(TAG, availableAmount.toString());
	}
	
	private class ReceivingAddressListener implements OnFocusChangeListener, TextWatcher {
		
		public void onFocusChange(View v, boolean hasFocus) {}

		public void afterTextChanged(Editable s) {}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		public void onTextChanged(CharSequence s, int start, int before, int count) {}
	}
}
