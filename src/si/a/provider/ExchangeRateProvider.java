package si.a.provider;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.Utils;

import si.a.util.Constants;
import si.a.util.WalletUtils;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;

public class ExchangeRateProvider extends ContentProvider {
	public static String TAG = "ExchangeRateProvider";
	
	public static String KEY_CURRENCY_CODE = "currency_code";
	private static final String KEY_RATE = "rate";
	
	private Map<String, ExchangeRate> exchangeRates = new HashMap<String, ExchangeRate>();

	private static long UPDATE_FREQ = 10 * DateUtils.MINUTE_IN_MILLIS;
	private long lastUpdated = 0;
	
	private static final Logger log = LoggerFactory.getLogger(ExchangeRateProvider.class);
	
	@Override
	public boolean onCreate() {
		return true;
	}
	
	public static Uri contentUri(String packageName) {
		Log.i(TAG, packageName);
		return Uri.parse("content://" + packageName + "." + "exchanges_rates");
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.i(TAG, "query");
		
		long now = System.currentTimeMillis();
		exchangeRates = getBlockchainInfo();
		lastUpdated = now;
		
		if(exchangeRates == null)
			return null;
		MatrixCursor cursor = new MatrixCursor(new String[] {BaseColumns._ID, KEY_CURRENCY_CODE, KEY_RATE});
		if(selection == null) {
			for(Map.Entry<String, ExchangeRate> entry : exchangeRates.entrySet() ) {
				ExchangeRate rate = entry.getValue();
				cursor.newRow().add(rate.currencyCode.hashCode()).add(rate.currencyCode).add(rate.rate.doubleValue());
			}
		} else if(selection.equals(KEY_CURRENCY_CODE)) {
			String selectedCode = selectionArgs[0];
			ExchangeRate rate = selectedCode != null ? exchangeRates.get(selectedCode) : null;
			if(rate == null) {
				String defaultCode = defaultCurrencyCode();
				rate = defaultCode != null ? exchangeRates.get(defaultCode) : null;
				if(rate == null) {
					rate = exchangeRates.get(Constants.EXCHANGE_RATE);
					if(rate == null)
						return null;
				}
			}
			cursor.newRow().add(rate.currencyCode.hashCode()).add(rate.currencyCode).add(rate.rate.doubleValue());
		}
		return cursor;
	}

	private String defaultCurrencyCode() {
		
		try {
			return Currency.getInstance(Locale.getDefault()).getCurrencyCode();
		} catch(IllegalArgumentException exception) {
			return null;
		}
	}
	
	private Map<String, ExchangeRate> getBlockchainInfo() {
		Log.i("TAG", "getBlockchainInfo");
		try {
			URL url = new URL("https://blockchain.info/ticker");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(Constants.HTTP_TIMEOUT_MS);
			connection.setReadTimeout(Constants.HTTP_TIMEOUT_MS);
			connection.connect();
			
			if(connection.getResponseCode() != HttpsURLConnection.HTTP_OK)
				return null;
			
			InputStreamReader reader = null;
			try {
				reader = new InputStreamReader(connection.getInputStream());
				StringBuilder content = new StringBuilder();
				get(reader, content);
				
				Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
				JSONObject head = new JSONObject(content.toString());
				
				for(final Iterator<String> i = head.keys(); i.hasNext();) {
					final String currencyCode = i.next();
					if(!"timestamp".equals(currencyCode)) {
						final JSONObject o = head.getJSONObject(currencyCode);
						String rate = o.optString("24h", null);
						if (rate == null)
							rate = o.optString("7d", null);
						if (rate == null)
							rate = o.optString("30d", null);
						if (rate != null) {
							try {
								rates.put(currencyCode, new ExchangeRate(currencyCode, Utils.toNanoCoins(rate)));
							} catch (final ArithmeticException x) {}
						}
					}
				}
				return rates;
			} finally {
				if(reader != null)
					connection.disconnect();
					reader.close();
			}
		} catch(Exception exception) {}
		
		return null;
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) { return 0; }

	@Override
	public String getType(Uri uri) { return null; }

	@Override
	public Uri insert(Uri uri, ContentValues values) { return null; }
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
	
	public static ExchangeRate getExchangeRate(Cursor cursor) {
		String currencyCode = cursor.getString(cursor.getColumnIndex(ExchangeRateProvider.KEY_CURRENCY_CODE));
		BigInteger rate = BigInteger.valueOf(cursor.getLong(cursor.getColumnIndex(ExchangeRateProvider.KEY_RATE)));
		return new ExchangeRate(currencyCode, rate);
	}
	
	private Map<String, ExchangeRate> getBitcoinCharts() {
		Log.i(TAG, "getBitcoinCharts");
		
		try {
			final URL url = new URL("http://api.bitcoincharts.com/v1/weighted_prices.json");
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(Constants.HTTP_TIMEOUT_MS);
			connection.setReadTimeout(Constants.HTTP_TIMEOUT_MS);
			connection.connect();
			
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.i(TAG, "HTTP_NO");
				return null;
			}
			Reader reader = null;
			
			try {
				reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream(), 1024), Constants.UTF_8);
				final StringBuilder content = new StringBuilder();
				//get(reader, content);
				Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
				
				final JSONObject head = new JSONObject(content.toString());
				for(Iterator<String> i = head.keys(); i.hasNext();) {
					String currencyCode = i.next();
					if(!"timestamp".equals(currencyCode)) {
						JSONObject obj = head.getJSONObject(currencyCode);
						String rate = obj.optString("24h", null);
						if(rate == null)
							rate = obj.optString("7d", null);
						if(rate == null)
							rate = obj.optString("30d", null);
						if(rate != null) {
							try {
								Log.i(TAG, currencyCode.toString());
								Log.i(TAG, Utils.toNanoCoins(rate).toString());
								rates.put(currencyCode, new ExchangeRate(currencyCode, Utils.toNanoCoins(rate)));
							} catch(ArithmeticException exception) {}
						}
					}
				}
				return rates;
			} finally {
				if(reader != null)
					reader.close();
			}
		} catch (Exception e) {}
		return null;
	}
	
	public static long get(InputStreamReader reader, StringBuilder builder) throws IOException {
		char[] buffer = new char[256];
		long count = 0;
		int n = 0;
		while((n = reader.read(buffer)) != -1) {
			builder.append(buffer, 0, n);
			count += n;
		}
		return count;
	}
	
	public static class ExchangeRate {
		public final String currencyCode;
		public final BigInteger rate;
		
		public ExchangeRate(String currencyCode, BigInteger rate) {
			this.currencyCode = currencyCode;
			this.rate = rate;
		}
		
		public String toString() {
			return getClass().getSimpleName() + "[" + currencyCode + ":" +
				WalletUtils.formatValue(rate, Constants.COIN_MAX_PRECISION) + "]";
		}
	}
}
