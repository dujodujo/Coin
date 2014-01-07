package si.a.util;

import java.math.BigInteger;

import android.util.Log;

import com.google.bitcoin.core.Utils;
import si.a.provider.ExchangeRateProvider.ExchangeRate;
import si.a.wiew.CurrencyAmountView;
import si.a.wiew.CurrencyAmountView.CurrencyListener;

public class CurrencyAmount {
	
	private static final String TAG = CurrencyAmount.class.getName();
	
	private CurrencyAmountView coinAmountView;
	private CurrencyAmountView localAmountView;	
	private CurrencyAmountView.CurrencyListener listener;
	
	private ExchangeRate exchangeRate;
	private boolean exchangeDirection;
	
	public CurrencyAmount(final CurrencyAmountView coinAmountView, final CurrencyAmountView localAmountView) {
		Log.i(TAG, "CurrencyAmount");
		
		this.coinAmountView = coinAmountView;
		this.coinAmountView.setListener(coinAmountViewListener);
		
		this.localAmountView = localAmountView;
		this.localAmountView.setListener(coinAmountViewListener);
		update();
	}
	
	public void update() {
		Log.i(TAG, "update");

		if(exchangeRate != null) {
			localAmountView.setCurrencySymbol(exchangeRate.currencyCode);
			if(exchangeDirection) {
				final BigInteger coinValue = coinAmountView.getCoinAmount();
				Log.i(TAG, coinValue + " :coinValue");
				
				if(coinValue != null) {
					localAmountView.setCoinAmount(null);
					localAmountView.setHint(WalletUtils.getLocalValue(coinValue, exchangeRate.rate));
					coinAmountView.setHint(null);
				}
			} else {
				final BigInteger localValue = coinAmountView.getCoinAmount();
				Log.i(TAG, localValue + " :localValue");
				
				if(localValue != null) {
					coinAmountView.setCoinAmount(null);
					coinAmountView.setHint(WalletUtils.getCoinValue(localValue, exchangeRate.rate));
					localAmountView.setHint(null);
				}
			}
		}
	}
	
	public void setExchangeRate(final ExchangeRate exchangeRate) {
		Log.i(TAG, "setExchangeRate");

		this.exchangeRate = exchangeRate;
		update();
	}
	
	public void setListener(final CurrencyListener listener) {
		this.listener = listener;
	}
	
	public CurrencyAmountView.CurrencyListener coinAmountViewListener = new CurrencyAmountView.CurrencyListener() {
		
		public void focusChanged(boolean hasFocus) {
			Log.i(TAG, "focusChanged");

			if(listener != null)
				listener.focusChanged(hasFocus);
		}
		
		public void done() {
			Log.i(TAG, "done");

			if(listener != null)
				listener.changed();
		}
		
		public void changed() {
			Log.i(TAG, "changed");

			if(coinAmountView.getCoinAmount() != null) {
				exchangeDirection = true;
				update();
			} else
				coinAmountView.setHint(null);
			
			if(listener != null)
				listener.changed();
		}
	};
	
	public CurrencyAmountView.CurrencyListener localAmountViewListener = new CurrencyAmountView.CurrencyListener() {
		
		public void focusChanged(boolean hasFocus) {
			Log.i(TAG, "focusChanged");

			if(listener != null)
				listener.focusChanged(hasFocus);
		}
		
		public void done() {
			Log.i(TAG, "done");

			if(listener != null)
				listener.done();
		}
		
		public void changed() {
			Log.i(TAG, "changed");

			if(localAmountView.getCoinAmount() != null) {
				exchangeDirection = false;
				update();
			}
		}
	};

	public void setCoinAmount(final BigInteger amount) {
		coinAmountView.setCoinAmount(amount);
	}
}