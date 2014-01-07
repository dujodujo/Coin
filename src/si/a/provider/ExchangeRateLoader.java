package si.a.provider;

import si.a.util.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.v4.content.CursorLoader;

public class ExchangeRateLoader extends CursorLoader implements OnSharedPreferenceChangeListener {
	
	private final SharedPreferences preferences;
	
	public ExchangeRateLoader(Context context) {
		super(context, ExchangeRateProvider.contentUri(context.getPackageName()), null, 
			ExchangeRateProvider.KEY_CURRENCY_CODE, new String[] {null}, null);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(Constants.PREFS_KEY_EXCHANGE_CURRENCY.equals(key)) {
			onCurrencyChanged();
		}
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		preferences.registerOnSharedPreferenceChangeListener(this);
		onCurrencyChanged();
	}

	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		preferences.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	private void onCurrencyChanged() {
		String exchangeCurrency = preferences.getString(Constants.PREFS_KEY_EXCHANGE_CURRENCY, null);
		setSelectionArgs(new String[] {exchangeCurrency});
		forceLoad();
	}
}