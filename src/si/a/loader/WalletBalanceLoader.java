package si.a.loader;

import java.math.BigInteger;

import si.a.listener.WalletChangeListener;

//import si.a.listener.WalletChangeListener;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;

public final class WalletBalanceLoader extends AsyncTaskLoader<BigInteger> {
	public static final String TAG = WalletBalanceLoader.class.getName();
	final Wallet wallet;
	
	public WalletBalanceLoader(Context context, Wallet wallet) {
		super(context);
		this.wallet = wallet;
	}
	
	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		Log.i(TAG, "onStartLoading");
		
		wallet.addEventListener(walletChangeListener);
		forceLoad();
	}
	
	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		Log.i(TAG, "onStopLoading");
		
		wallet.removeEventListener(walletChangeListener);
		walletChangeListener.removeCallbacks();
	}	
	
	@Override
	public BigInteger loadInBackground() {
		return wallet.getBalance(BalanceType.ESTIMATED);
	}
	
	private WalletChangeListener walletChangeListener = new WalletChangeListener() {

		@Override
		public void onWalletChanged() { 
			forceLoad(); 
		}

		public void onKeyAdded(ECKey key) {}
	};
}
