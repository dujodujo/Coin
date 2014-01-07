package si.a.listener;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import android.os.Handler;
import android.util.Log;

import com.google.bitcoin.core.WalletEventListener;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Wallet;

public abstract class WalletChangeListener implements WalletEventListener {
	public static String TAG = WalletChangeListener.class.getName();
	
	private long throttleMS;
	private boolean coinsRelevant;
	private boolean reorganizeRelevant;
	private boolean confidenceRelevant;
	
	private AtomicLong lastMessageTime = new AtomicLong(0);
	private Handler handler = new Handler();
	private AtomicBoolean relevant = new AtomicBoolean();
	
	private static long THROTTLE_MS = 500;

	public WalletChangeListener() {
		this(THROTTLE_MS);
		Log.i(TAG, "WalletChangeListener");
	}
	
	public WalletChangeListener(long throttle) {
		this(throttle, true, true, true);
		Log.i(TAG, "WalletChangeListener");
	}
	
	public WalletChangeListener(boolean coinsRelevant, boolean reorganizeRelevant, boolean confidenceRelevant) {
		this(THROTTLE_MS, coinsRelevant, reorganizeRelevant, confidenceRelevant);
		Log.i(TAG, "WalletChangeListener");
	}
	
	public WalletChangeListener(long throttle, boolean coinsRelevant, boolean reorganizeRelevant, boolean confidenceRelevant) {
		this.throttleMS = throttle;
		this.coinsRelevant = coinsRelevant;
		this.reorganizeRelevant = reorganizeRelevant;
		this.confidenceRelevant = confidenceRelevant;
		Log.i(TAG, "WalletChangeListener");
	}
	
	public void onCoinsReceived(Wallet wallet, Transaction tx, BigInteger previousBalance, BigInteger currentBalance) {
		if(coinsRelevant)
			relevant.set(true);
	}

	public void onCoinsSent(Wallet wallet, Transaction tx, BigInteger previousBalance, BigInteger currentBalance) {
		if(coinsRelevant)
			relevant.set(true);
	}
	
	public void onReorganize(Wallet wallet) {
		if(reorganizeRelevant)
			relevant.set(true);
	}

	public void onTransactionConfidenceChanged(Wallet wallet, Transaction transaction) {
		if(confidenceRelevant)
			relevant.set(true);
	}
	
	private Runnable runnable = new Runnable() {

		public void run() {
			lastMessageTime.set(System.currentTimeMillis());
			onWalletChanged();
		}
	};
	
	public void removeCallbacks() {
		handler.removeCallbacksAndMessages(null);
	}
	
	public void removeCallbacksAndMessages() {
		handler.removeCallbacksAndMessages(null);
	}
	
	public void onWalletChanged(Wallet wallet) {
		Log.i(TAG, "onWalletChanged");

		if(relevant.getAndSet(false)) {
			handler.removeCallbacksAndMessages(null);
			long now = System.currentTimeMillis();
			if((now - lastMessageTime.get()) > throttleMS) {
				handler.post(runnable);
			} else {
				handler.postDelayed(runnable, throttleMS);
			}
		}
	}

	public abstract void onWalletChanged();
}
