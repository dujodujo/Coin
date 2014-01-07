package si.a.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;



import android.content.res.AssetManager;
import android.util.Log;

public abstract class HttpGetThread extends Thread {
	
	private final String TAG = "HttpGetThread";
	private final AssetManager assets;
	private final String url;
	
	private static final Logger log = LoggerFactory.getLogger(HttpGetThread.class);
	
	public HttpGetThread(final AssetManager assets, final String url) {
		this.assets = assets;
		this.url = url;
	}

	@Override
	public void run() {
		Log.i(TAG, "run");
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			if(connection instanceof HttpURLConnection) {
				final InputStream keyStoreInputStream = assets.open("ssl-keystore");
				final KeyStore keystore = KeyStore.getInstance("BKS");
				keystore.load(keyStoreInputStream, "password".toCharArray());
				keyStoreInputStream.close();
				
				final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
				trustManagerFactory.init(keystore);
				final SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
				((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
			}
			
			connection.setConnectTimeout(Constants.HTTP_TIMEOUT_MS);
			connection.setReadTimeout(Constants.HTTP_TIMEOUT_MS);
			connection.setRequestProperty("Accept-Charset", "utf-8");
			connection.connect();
			
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), 
					Constants.UTF_8), 64);
				final String line = reader.readLine().trim();
				reader.close();

				Log.i(TAG, line);
				//handleLine(line, serverTime);
			}
			
		} catch (final Exception ex) {
		} finally {
			if(connection != null)
				connection.disconnect();
		}
		Log.i(TAG, "end run");
	}
}