package si.a.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.a.util.Constants;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.widget.Toast;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkConnection;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.WalletTransaction;
import com.google.bitcoin.store.WalletProtobufSerializer;
import com.google.common.base.Preconditions;

import si.a.util.Constants;
import si.a.util.WalletUtils;
import si.a.coin.WalletActivity;
import si.a.service.BlockchainService;
import si.a.service.BlockchainServiceImplementation;

public class WalletApplication extends Application {
	private static final String TAG = "WalletApplication";
	
	private SharedPreferences preferences;
	private ActivityManager activityManager;
	
	private File walletFile;
	private PackageInfo packageInfo;
	private Wallet wallet;
	
	private Intent blockChainServiceIntent;
	private Intent blockChainServiceCancelCoinReceivedIntent;
	private Intent blockChainServiceResetBlockChainIntent;
	
	private static int KEY_ROTATION_VERSION_CODE = 135;
	private static final Logger log = LoggerFactory.getLogger(WalletApplication.class);
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (final NameNotFoundException exception) {
			throw new RuntimeException(exception);
		}
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

		blockChainServiceIntent = new Intent(this, BlockchainServiceImplementation.class);
		blockChainServiceCancelCoinReceivedIntent = 
				new Intent(BlockchainService.ACTION_RESET_BLOCKCHAIN, null,this, BlockchainServiceImplementation.class);
		blockChainServiceResetBlockChainIntent = new Intent(BlockchainService.ACTION_RESET_BLOCKCHAIN, null, this, 
			BlockchainServiceImplementation.class);
		
		walletFile = getFileStreamPath(Constants.WALLET_FILENAME);
		
		loadWallet();
		ensureKey();
	}
	
	public Wallet getWallet() { 
		return wallet; 
	}
	
	private void moveWallet() throws ParseException {
		File oldWalletFile = getFileStreamPath(Constants.WALLET_FILENAME);
		if(oldWalletFile.exists()) {
			long start = System.currentTimeMillis();
			//wallet = restoreWalletFromBackup();
		}
	}
	
	private void loadWallet() {
		Log.i(TAG, "loadWallet");
		
		if(walletFile.exists()) {
			Log.i(TAG, "wallet exists");
			
			FileInputStream walletStream = null;
			
			try {
				walletStream = new FileInputStream(walletFile);
				try {
					wallet = new WalletProtobufSerializer().readWallet(walletStream);
					Log.i(TAG, "wallet loaded from: " + walletFile);
				} catch (IOException e) {
					Log.d("Exception", "Failed to read wallet");
				}
			} catch (FileNotFoundException exception) {
				Toast.makeText(WalletApplication.this, "FileNotFoundException", Toast.LENGTH_LONG).show();
			} finally {
				if(walletStream != null) {
					try {
						walletStream.close();
					} catch(IOException exception) {}
				}
			}
			if(!wallet.isConsistent()) {
				Toast.makeText(this, "Inconsistent wallet: " + walletFile.toString(), Toast.LENGTH_LONG).show();
			}
		} else {
			Log.i(TAG, "creating wallet");
			wallet = new Wallet(NetworkParameters.testNet());
		}
		Log.i(TAG, "end load wallet");
	}
	
	private void ensureKey() {
		Log.i(TAG, "ensureKey");
		if(wallet != null) {
			for(ECKey key : wallet.getKeys())
				if(!(wallet).isConsistent())
					return;
			addKeyToWallet();
		}
	}
	
	public PackageInfo getPackageInfo() { return packageInfo; }
	
	public int getMaxConnectedPeers() { return 6; }
	
	public void resetBlockchain() {
		startService(blockChainServiceCancelCoinReceivedIntent);
	}
	
	public void stopBlockchainService() {
		stopService(blockChainServiceIntent);
	}
	
	public void startBlockchainService(final boolean cancelCoinsReceived) {
		if(cancelCoinsReceived) {
			startService(blockChainServiceIntent);
		} else {
			startService(blockChainServiceCancelCoinReceivedIntent);
		}
	}
	
	private ServiceConnection startServiceConnection(final Transaction transaction) {
		 ServiceConnection serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder binder) {
				final BlockchainService blockchainService = ((BlockchainServiceImplementation.LocalBinder)binder).getService();
				blockchainService.broadcastTransaction(transaction);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				unbindService(this);
			}
		};
		return serviceConnection;
	}
	
	public boolean isServiceRunning(Class<? extends Service> service) {
		final String packageName = getPackageName();
		for(RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if(packageName.equals(serviceInfo.service.getPackageName()) && 
					service.getName().equals(serviceInfo.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	public Address determineSelectedAddress() {
		String selectedAddress = preferences.getString(Constants.PREFS_KEY_SELECTED_ADDRESS, null);
		Address firstAddress = null;
		for(final ECKey key : wallet.getKeys()) {
			final Address address = key.toAddress(Constants.NETWORK_PARAMETERS);
			if(address.toString().equals(selectedAddress))
				return address;
			if(firstAddress == null)
				firstAddress = address;
		}
		return firstAddress;
	}
	
	public void addKeyToWallet() {
		Log.i(TAG, "addKeyToWallet");
		wallet.addKey(new ECKey());
		storeKeys();
	}
	
	public void writeKeys(final OutputStream output) throws IOException {
		Log.i(TAG, "writeKeys");

		final List<ECKey> keys = new LinkedList<ECKey>();
		for(ECKey key : wallet.getKeys()) {
			if(wallet.isConsistent()) {
				keys.add(key);
			}
		}
		final Writer out = new OutputStreamWriter(output, Constants.UTF_8);
		WalletUtils.writeKeys(out, keys);
		out.close();
		Log.i(TAG, "end writeKeys");
	}
	
	public Wallet readKeys(InputStream input) throws IOException, ParseException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, Constants.UTF_8));
		List<ECKey> keys = WalletUtils.readKeys(reader);
		input.close();
		
		final Wallet wallet = new Wallet(Constants.NETWORK_PARAMETERS);
		for(ECKey key : keys)
			wallet.addKey(key);
		return wallet;
	}
	
	public void storeKeys() {
		Log.i(TAG, "storeKeys");
		try {
			writeKeys(openFileOutput(Constants.WALLET_KEY_BACKUP, Context.MODE_PRIVATE));
		} catch (final IOException ex) {
			Log.d("Problem with storage", "error");
		}
		Log.i(TAG, "end store keys");
	}
}