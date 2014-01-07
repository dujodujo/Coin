package si.a.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.Connection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.a.listener.PeerConnectListener;
import si.a.listener.WalletChangeListener;
import si.a.receiver.WalletBalanceWidgetProvider;
import si.a.util.Constants;
import si.a.coin.AbstractWalletActivity;
import si.a.coin.WalletActivity;
import si.a.application.WalletApplication;
import si.a.coin.app.R;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.CheckBox;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Peer;
import com.google.bitcoin.core.PeerEventListener;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.WalletEventListener;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.core.AbstractPeerEventListener;
import com.google.bitcoin.discovery.DnsDiscovery;
import com.google.bitcoin.discovery.PeerDiscovery;
import com.google.bitcoin.discovery.PeerDiscoveryException;
import com.google.bitcoin.store.DerbyBlockStore;

public class BlockchainServiceImplementation extends android.app.Service implements BlockchainService {
	public static final String TAG = BlockchainServiceImplementation.class.getName();
	
	private WalletApplication application;
	private SharedPreferences preferences;
	private BlockStore blockStore;
	private BlockChain blockChain;
	private PeerGroup peerGroup;
	private PeerEventListener peerEventListener;
	private AbstractWalletActivity walletActivity;
	private File blockchainFile;
	
	private final Handler handler = new Handler();
	private final Handler delayHandler = new Handler();
	
	private NotificationManager notificationManager;
	
	private static final int NOTIFICATION_ID_CONNECTED = 0;
	private final List<Address> notificationAddresses = new LinkedList<Address>();
	
	private long serviceCreatedDate;
	private int bestChainHeightEver;
	
	private static final Logger log = LoggerFactory.getLogger(BlockchainServiceImplementation.class);
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		application = (WalletApplication) getApplication();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Wallet wallet = application.getWallet();

		bestChainHeightEver = preferences.getInt(Constants.PREFS_KEY_BEST_CHAIN_HEIGHT_EVER, 0);
		peerEventListener = new PeerConnectListener(walletActivity, this, getPackageName());

		sendBroadcastPeerState(0);
		application.getWallet().addEventListener(walletEventListener);
		
		/*
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
		
		registerReceiver(connectionReceiver, intentFilter);
		
		blockchainFile = new File(getDir("blockstore", Context.MODE_PRIVATE), Constants.BLOCKCHAIN_FILENAME);
		boolean blockchainFileExists = blockchainFile.exists();
		
		if(!blockchainFileExists) {
			wallet.clearTransactions(0);
			wallet.setLastBlockSeenHash(null);
		}
		
		try {
			blockStore = new DerbyBlockStore(Constants.NETWORK_PARAMETERS, blockchainFile.getName());
			blockStore.getChainHead();
			long keyCreationDate = wallet.getEarliestKeyCreationTime();
			
		} catch (BlockStoreException x) {
			blockchainFile.delete();
		}
		
		try {
			blockChain = new BlockChain(Constants.NETWORK_PARAMETERS, wallet, blockStore);
		} catch (final BlockStoreException xx) {}
		
		registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		*/
	}
	
	public void broadcastTransaction(Transaction transaction) {}

	public List<Peer> getConnectedPeers() {
		return null;
	}

	public List<StoredBlock> getRecentBlocks(int maxBlocks) {
		return null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public class LocalBinder extends Binder {
		public BlockchainService getService() { return BlockchainServiceImplementation.this; }
	}	

	private void sendBroadcastPeerState(final int numPeers) {
		Intent broadcast = new Intent(BlockchainService.ACTION_PEER_STATE);
		broadcast.setPackage(getPackageName());
		broadcast.putExtra(ACTION_PEER_STATE_NUM_PEERS, numPeers);
		sendStickyBroadcast(broadcast);
	}
	
	private void removeBroadcastPeerState() {
		removeStickyBroadcast(new Intent(ACTION_PEER_STATE));
	}
	
	private void sendBroadcastBlockchainState(final int download) {
		final StoredBlock chainHead = blockChain.getChainHead();
		
		Intent broadcast = new Intent(ACTION_BLOCKCHAIN_STATE);
		broadcast.setPackage(getPackageName());
		broadcast.putExtra(ACTION_BLOCKCHAIN_STATE_BEST_CHAIN_DATE, chainHead.getHeader().getTime());
		broadcast.putExtra(ACTION_BLOCKCHAIN_STATE_BEST_CHAIN_HEIGHT, chainHead.getHeight());
		broadcast.putExtra(ACTION_BLOCKCHAIN_STATE_REPLAYING, chainHead.getHeight() < bestChainHeightEver);
		broadcast.putExtra(ACTION_BLOCKCHAIN_STATE_DOWNLOAD, download);
		
		sendStickyBroadcast(broadcast);
	}
	
	private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
		
		private boolean hasConnection;
		private boolean hasStorage = true;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			ConnectivityManager connectivityManager;
			connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
				boolean extraConnection = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
				boolean extraIsConnected = networkInfo != null && networkInfo.isConnected();
				hasConnection = extraConnection && extraIsConnected;
				
				check();
				
			} else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {
				this.hasStorage = false;
				check();
			} else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
				this.hasStorage = true;
				check();
			}
		}
		
		private void check() {
			Wallet wallet = application.getWallet();
			boolean connectionAndStorage = hasConnection && hasStorage;
			
			if(connectionAndStorage && peerGroup == null) {
				int bestBlockHeight = blockChain.getBestChainHeight();
			}
			
			peerGroup = new PeerGroup(Constants.NETWORK_PARAMETERS, blockChain);
			//peerGroup.addWallet(wallet);
			//peerGroup.setUserAgent("wallet", application.getPackageInfo().versionName);
			//peerGroup.addEventListener(PeerConnectListener);
			
			int maxConnectionPeers = application.getMaxConnectedPeers();
			String trustedPeer = preferences.getString(Constants.PREFS_KEY_TRUSTED_PEER, "").trim();
			boolean hasTrustedPeer = !trustedPeer.isEmpty();
			
			boolean connectOnlyTrustedPeer = hasTrustedPeer && preferences.getBoolean(Constants.PREFS_KEY_TRUSTED_PEER_ONLY, false);
			peerGroup.setMaxConnections(connectOnlyTrustedPeer ? 1 : maxConnectionPeers);
		}
	};
	
	
	private final PeerEventListener blockchainDownloadListener = new AbstractPeerEventListener() {

		@Override
		public void onBlocksDownloaded(Peer peer, Block block, int blocksLeft) {
			super.onBlocksDownloaded(peer, block, blocksLeft);
		}
		
		private final Runnable runnable = new Runnable() {

			public void run() {}
		};
	};
	
	private final WalletEventListener walletEventListener = new WalletChangeListener() {
		
		@Override
		public void onKeyAdded(ECKey key) {}

		@Override
		public void onWalletChanged() {
			notifyWidgets();
		}
	};

	public void notifyWidgets() {
		Log.i(TAG, "notify Widgets");
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		ComponentName providerName = new ComponentName(this, WalletBalanceWidgetProvider.class);
		
		int[] appWidgetIDS = appWidgetManager.getAppWidgetIds(providerName);
		
		if(appWidgetIDS.length > 0) {
			Log.i(TAG, "update widgets");
			Wallet wallet = application.getWallet();
			BigInteger balance = wallet.getBalance(BalanceType.ESTIMATED);
			WalletBalanceWidgetProvider.updateWidgets(this, appWidgetManager, appWidgetIDS, balance);
		}
	}
}