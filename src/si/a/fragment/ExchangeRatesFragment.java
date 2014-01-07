package si.a.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import si.a.loader.WalletBalanceLoader;
import si.a.provider.ExchangeRateProvider;
import si.a.provider.ExchangeRateProvider.ExchangeRate;
import si.a.service.BlockchainService;
import si.a.util.Constants;
import si.a.util.WalletUtils;
import si.a.wiew.CurrencyTextView;
import si.a.coin.AbstractWalletActivity;
import si.a.coin.app.R;
import si.a.application.WalletApplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;

import android.util.Log;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;

public class ExchangeRatesFragment extends ListFragment {

	public static String TAG = "ExchangeRatesFragment";
	
	private AbstractWalletActivity activity;
	private WalletApplication application;
	private Wallet wallet;
	private SharedPreferences preferences;
	private LoaderManager loaderManager;
	private CursorAdapter adapter;
	
	private boolean replaying = false;
	public BigInteger balance = null;
	
	private static final int ID_BALANCE_LOADER = 0;
	private static final int ID_RATE_LOADER = 1;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.activity = (AbstractWalletActivity) activity;
		this.application = (WalletApplication) activity.getApplication();
		this.wallet = application.getWallet();
		this.preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		this.loaderManager = getLoaderManager();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "onActivityCreated");
		
		this.adapter = new ResourceCursorAdapter(activity, R.layout.exchange_rate_row, null, true) {
			
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				ExchangeRate exchangeRate = ExchangeRateProvider.getExchangeRate(cursor);
				view.setBackgroundResource(R.color.bg_bright);
				
				TextView currencyCodeView = (TextView) view.findViewById(R.id.exchange_rate_row_currency_code);
				currencyCodeView.setText(exchangeRate.currencyCode);

				CurrencyTextView rateView = (CurrencyTextView) view.findViewById(R.id.exchange_rate_row_rate);
				rateView.setPrecision(Constants.LOCAL_PRECISION);
				rateView.setAmount(WalletUtils.getLocalValue(Utils.COIN, exchangeRate.rate));
								
				CurrencyTextView walletView = (CurrencyTextView) view.findViewById(R.id.exchange_rate_row_balance);
				walletView.setPrecision(Constants.LOCAL_PRECISION);
				
				walletView.setAmount(WalletUtils.getLocalValue(Utils.COIN, exchangeRate.rate));
				walletView.setTextColor(getResources().getColor(R.color.fg_less_significant));
			}
		};
		setListAdapter(adapter);
	}
	
	private final BlockchainBroadcastReceiver broadcastReceiver = new BlockchainBroadcastReceiver();
	
	@Override
	public void onResume() {
		super.onResume();
		
		loaderManager.initLoader(ID_BALANCE_LOADER, null, balanceLoaderCallbacks);
		loaderManager.initLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);
		activity.registerReceiver(broadcastReceiver, new IntentFilter(BlockchainService.ACTION_BLOCKCHAIN_STATE));
		updateView();
	}
	
	@Override
	public void onPause() {
		super.onPause();

		loaderManager.destroyLoader(ID_BALANCE_LOADER);
		loaderManager.destroyLoader(ID_RATE_LOADER);
		activity.unregisterReceiver(broadcastReceiver);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		final Cursor cursor = (Cursor) adapter.getItem(position);
		final ExchangeRate exchangeRate = ExchangeRateProvider.getExchangeRate(cursor);
		
		activity.startActionMode(new ActionMode.Callback() {
			
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				final MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.exchange_rates_context, menu);
				return true;
			}
			
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				mode.setTitle(exchangeRate.currencyCode);
				return true;
			}
			
			public void onDestroyActionMode(ActionMode mode) {}
			
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) { return true; }
		});
	}
	
	private class BlockchainBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//replaying = intent.getBooleanExtra(BlockchainService.ACTION_BLOCKCHAIN_STATE_REPLAYING, false);
			updateView();
		}
	}
	
	private void updateView() {
		//balance = application.getWallet().getBalance(BalanceType.ESTIMATED);
		if(adapter != null)
			((BaseAdapter) adapter).notifyDataSetChanged();
	}
	
	private final LoaderCallbacks<Cursor> rateLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new CursorLoader(activity, 
				ExchangeRateProvider.contentUri(activity.getPackageName()), null, null, null, null);
		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			adapter.swapCursor(data);
		}
		
		public void onLoaderReset(Loader<Cursor> loader) {
			adapter.swapCursor(null);
		}
	};

	private final LoaderCallbacks<BigInteger> balanceLoaderCallbacks = new LoaderManager.LoaderCallbacks<BigInteger>() {

		public Loader<BigInteger> onCreateLoader(int id, Bundle args) {
			return new WalletBalanceLoader(activity, wallet);
		}

		public void onLoadFinished(Loader<BigInteger> loader, BigInteger balance) {
			ExchangeRatesFragment.this.balance = balance;
			updateView();
		}

		public void onLoaderReset(Loader<BigInteger> loader) {}
	};
}