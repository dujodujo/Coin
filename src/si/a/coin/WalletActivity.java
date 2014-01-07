package si.a.coin;

import java.math.BigInteger;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.GetAddrMessage;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.AbstractBlockChain.NewBlockType;

//import si.a.parser.StringInputParser;
//import si.a.service.BlockchainService;
//import si.a.service.BlockchainServiceImplementation;
import si.a.util.Constants;
import si.a.util.HttpGetThread;
import si.a.application.WalletApplication;
import si.a.coin.app.R;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class WalletActivity extends AbstractWalletActivity {

	public static final String TAG = "WalletActivity";
	
	private static final int REQUEST_CODE_SCAN = 0;
	private static final int DIALOG_IMPORT_KEYS = 1;
	private static final int DIALOG_EXPORT_KEYS = 2;

	private WalletApplication application;
	private SharedPreferences prefs;
	private Wallet wallet;
	
	private static final Logger log = LoggerFactory.getLogger(WalletActivity.class);
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        application = getWalletApplication();
        wallet = application.getWallet();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.wallet_content);
    }

	@Override
	public void proccessDirectTransaction(Transaction tx) {}
    
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//unbindService(serviceConnection);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wallet_options, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "onOptionsItemSelected");
		
		switch(item.getItemId()) {
			case R.id.wallet_options_address_book:
				AddressBookActivity.start(this, true);
				return true;
			case R.id.wallet_options_exchange_rate:
				startActivity(new Intent(this, ExchangeRatesActivity.class));
				return true;
			case R.id.wallet_network_activity:
				startActivity(new Intent(this, NetworkActivity.class));
				return true;
			case R.id.wallet_options_scan:
				startScanActivity();
				return true;
			case R.id.wallet_send_coins_activity:
				//startActivity(new Intent(this, SendCoinsActivity.class));
				SendCoinsActivity.startSendingCoins(this, Constants.DONATION_ADDRESS, 
					getString(R.string.wallet_donate_address_label), null);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void startScanActivity() {
		//startActivityForResult(new Intent(this, ScanActivity.class), REQUEST_CODE_SCAN);
	}
	
	/*
	@Override
	public void proccessDirectTransaction(Transaction tx) {
		final Wallet wallet = getWalletApplication().getWallet();
		
		try {
			if(wallet.isTransactionRelevant(tx)) {
				wallet.receivePending(tx, null);
				WalletApplication walletApplication = (WalletApplication) getApplication();
				walletApplication.broadcastTransaction(tx);
			} else {
				longToast("Direct transaction failed");
			}
		} catch(VerificationException ve) {
			longToast("Direct Transaction failed");
		}
	}

	public ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			blockchainService = ((BlockchainServiceImplementation.LocalBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName name) {
			blockchainService = null;
		}
	};

	public BlockchainService getBlockchainService() {
		return blockchainService;
	}
	
	public void startScan() {
		//startActivityForResult(new Intent(this, ScanActivity.class), REQUEST_CODE_SCAN);
	}
	
	public void startExportKey() {}
	
	public void startDisconnect() {
		getWalletApplication().stopBlockchainService();
		finish();
	}
	
	public void startSendCoins() {
		startActivity(new Intent(this, SendCoinsActivity.class));
	}
	
	public void startRequestCoins() {
		//startActivity(new Intent(this, RequestCoinsActivity.class));
	}
	*/
}