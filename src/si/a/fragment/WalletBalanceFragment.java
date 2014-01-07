package si.a.fragment;

import java.math.BigInteger;
import java.util.Date;

import com.google.bitcoin.core.Wallet;

import si.a.loader.WalletBalanceLoader;
import si.a.provider.ExchangeRateLoader;
import si.a.provider.ExchangeRateProvider;
import si.a.provider.ExchangeRateProvider.ExchangeRate;
import si.a.service.BlockchainService;
import si.a.util.Constants;
import si.a.util.WalletUtils;
import si.a.wiew.CurrencyTextView;
import si.a.coin.AbstractWalletActivity;
import si.a.coin.ExchangeRatesActivity;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;
import si.a.application.WalletApplication;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class WalletBalanceFragment extends Fragment {
	
	public static String TAG = "WalletBalanceFragment";
	
	private WalletApplication walletApplication;
	private WalletActivity activity;
	private Wallet wallet;
	private SharedPreferences preference;
	private LoaderManager loaderManager;
	
	private View viewBalance;
	private FrameLayout viewBalanceFrame;
	private CurrencyTextView viewBalanceLocal;
	private CurrencyTextView viewBalanceCoins;
	private TextView balanceTest;
	private TextView viewProgress;
	
	private BigInteger balance = null;
	private ExchangeRate exchangeRate = null;
	private boolean showBalance;
	private boolean showBalanceLocal;
	private int download;
	
	private Date currentChainDate = null;
	private boolean replaying = true;
	
	private static final int ID_BALANCE_LOADER = 0;
	private static final int ID_RATE_LOADER = 1;

	private BlockchainBroadcastReceiver broadcastReceiver = new BlockchainBroadcastReceiver();
	
	@Override
 	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.activity = (WalletActivity) activity;
		this.walletApplication = (WalletApplication) activity.getApplication();
		this.wallet = walletApplication.getWallet();
		this.preference = PreferenceManager.getDefaultSharedPreferences(activity);
		this.loaderManager = getLoaderManager();
		this.showBalance = true;
		this.showBalanceLocal = true;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.wallet_balance_fragment, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.i(TAG, "onViewCreated");
		
		boolean showExchangeRateOption = true;
		viewBalance = view.findViewById(R.id.wallet_balance);
		if(showExchangeRateOption) {
			viewBalance.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					startActivity(new Intent(getActivity(), ExchangeRatesActivity.class));
				}
			});
		} else {
			viewBalance.setEnabled(true);
		}
		
		viewBalanceCoins = (CurrencyTextView)view.findViewById(R.id.wallet_balance_coin);
		viewBalanceCoins.setSymbol(Constants.CURRENCY_CODE_COIN);
		viewBalanceCoins.setText("Coins");

		viewBalanceFrame = (FrameLayout) view.findViewById(R.id.wallet_balance_local_frame);
		
		if(showExchangeRateOption) {
			viewBalanceFrame.setForeground(getResources().getDrawable(R.drawable.stat_notify));
		}

		viewProgress = (TextView) view.findViewById(R.id.wallet_balance_progress);
		viewProgress.setText("Progress");

		viewBalanceLocal = (CurrencyTextView) view.findViewById(R.id.wallet_balance_local);
		viewBalanceLocal.setPrecision(Constants.LOCAL_PRECISION);
		viewBalanceLocal.setRelativeSpan(1);
		viewBalanceLocal.setText("Balance Local");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");

		loaderManager.destroyLoader(ID_RATE_LOADER);
		loaderManager.destroyLoader(ID_BALANCE_LOADER);
		activity.unregisterReceiver(broadcastReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();	
		Log.i(TAG, "onResume");
		
		activity.registerReceiver(broadcastReceiver, new IntentFilter(BlockchainService.ACTION_BLOCKCHAIN_STATE));		
		loaderManager.initLoader(ID_BALANCE_LOADER, null, balanceLoaderCallbacks);
		loaderManager.initLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);
		updateView();
	}
	
	private void updateView() {
		if(!isAdded())
			return;
		final boolean showProgress;
		
		if(currentChainDate != null) {
			boolean startDownload = download == BlockchainService.ACTION_BLOCKCHAIN_STATE_DOWNLOAD_OK;
			showProgress = true;
		} else
			showProgress = false;
		
		Log.i(TAG, showProgress + "show Progress");
		
		if(!showProgress) {
			viewBalance.setVisibility(View.VISIBLE);
			if(!showBalanceLocal)
				viewBalanceFrame.setVisibility(View.GONE);
			if(balance != null) {
				Log.i(TAG, balance + " : balance");
				
				viewBalanceCoins.setVisibility(View.VISIBLE);
				viewBalanceCoins.setPrecision(Integer.parseInt(preference.getString(Constants.PREFS_KEY_COIN_PRECISION, 
					Constants.PREFS_KEY_DEFAULT_COIN_PRECISION)));
				viewBalanceCoins.setAmount(balance);
				if(showBalanceLocal) {
					if(exchangeRate != null) {
						final BigInteger localValue = WalletUtils.getLocalValue(balance, exchangeRate.rate);
						viewBalanceFrame.setVisibility(View.VISIBLE);
						viewBalanceLocal.setSymbol(Constants.CURRENCY_CODE_COIN);
						viewBalanceLocal.setAmount(localValue);
						viewBalanceLocal.setTextColor(getResources().getColor(R.color.fg_less_significant));
					} else {
						viewBalanceFrame.setVisibility(View.INVISIBLE);
					}
				}
			} else {
				viewBalanceCoins.setVisibility(View.INVISIBLE);
			}
			viewProgress.setVisibility(View.GONE);
		} else {
			viewProgress.setVisibility(View.VISIBLE);
			viewBalance.setVisibility(View.INVISIBLE);
		}
	}
	
	private final class BlockchainBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			download = intent.getIntExtra(BlockchainService.ACTION_BLOCKCHAIN_STATE_DOWNLOAD, 
				BlockchainService.ACTION_BLOCKCHAIN_STATE_DOWNLOAD_OK);
			currentChainDate = (Date) intent.getSerializableExtra(BlockchainService.ACTION_BLOCKCHAIN_STATE_BEST_CHAIN_DATE);
			replaying = intent.getBooleanExtra(BlockchainService.ACTION_BLOCKCHAIN_STATE_REPLAYING, false);
			updateView();
		}
	};

	private final LoaderCallbacks<Cursor> rateLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new ExchangeRateLoader(activity);
		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			if(data != null) {
				data.moveToFirst();
				exchangeRate = ExchangeRateProvider.getExchangeRate(data);
				updateView();
			}
		}

		public void onLoaderReset(Loader<Cursor> loader) {}
	};

	private final LoaderCallbacks<BigInteger> balanceLoaderCallbacks = new LoaderManager.LoaderCallbacks<BigInteger>() {

		public Loader<BigInteger> onCreateLoader(int id, Bundle args) {
			Log.i(TAG, "onCreateLoader");
			return new WalletBalanceLoader(activity, wallet);
		}

		public void onLoadFinished(Loader<BigInteger> loader, BigInteger balance) {
			Log.i(TAG, "onLoadFinished");
			WalletBalanceFragment.this.balance = balance;
			updateView();
		}

		public void onLoaderReset(Loader<BigInteger> loader) {}
	};
}