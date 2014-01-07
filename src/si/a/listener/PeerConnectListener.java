package si.a.listener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import si.a.service.BlockchainService;
import si.a.service.BlockchainServiceImplementation;
import si.a.util.Constants;
import si.a.coin.AbstractWalletActivity;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.bitcoin.core.AbstractPeerEventListener;
import com.google.bitcoin.core.Peer;

public class PeerConnectListener extends AbstractPeerEventListener implements OnSharedPreferenceChangeListener {

	private int peerCount;
	private AbstractWalletActivity walletActivity;
	private AtomicBoolean stopped = new AtomicBoolean(false);
	private SharedPreferences sharedPreferences;
	private NotificationManager notificationManager;
	private BlockchainServiceImplementation blockchainService;
	private String packageName;
	
	private static final int NOTIFICATION_ID_CONNECTED = 0;
	private static final int NOTIFICATION_ID_COINS_RECEIVED = 1;
	
	private final Handler handler = new Handler();
	
	public PeerConnectListener(AbstractWalletActivity activity, BlockchainServiceImplementation blockchainService, String packageName) {
		this.walletActivity = (AbstractWalletActivity) activity;
		this.blockchainService = blockchainService;
		this.packageName = packageName;
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(walletActivity);
		this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	public void stop() {
		stopped.set(true);
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		notificationManager.cancel(NOTIFICATION_ID_CONNECTED);
	}
	
	private void changed(final int numPeers) {
		if(stopped.get())
			return;
		
		handler.post(new Runnable() {
			
			public void run() {
				boolean connectivityNotification = 
					sharedPreferences.getBoolean(Constants.PREFS_KEY_CONNECTIVITY_NOTIFICATION, false);
				
				if(!connectivityNotification || numPeers == 0) {
					notificationManager.cancel(NOTIFICATION_ID_CONNECTED);
				} else {
					//Intent intent = new Intent(blockchainService, WalletActivity.class);
					NotificationCompat.Builder notification = new NotificationCompat.Builder(blockchainService);
					notification.setContentTitle("AWallet");
					notification.setContentText("Connected");
					notification.setContentIntent(PendingIntent.getActivity(blockchainService, 0, 
						new Intent(blockchainService, WalletActivity.class), 0));
					notification.setWhen(System.currentTimeMillis());
					notification.setOngoing(true);
					notificationManager.notify(NOTIFICATION_ID_CONNECTED, notification.getNotification());
				}
				sendBroadcastPeerState(numPeers);
			}
		});
	}
	
	private void sendBroadcastPeerState(final int numPeers) {
		Intent broadcast = new Intent(BlockchainService.ACTION_PEER_STATE);
		broadcast.setPackage(this.packageName);
		broadcast.putExtra(BlockchainService.ACTION_PEER_STATE, numPeers);
		//sendStickyBroadcast(broadcast);
	}
	
	@Override
	public void onPeerConnected(Peer peer, int peerCount) {
		this.peerCount = peerCount;
		changed(peerCount);
	}

	@Override
	public void onPeerDisconnected(Peer peer, int peerCount) {
		this.peerCount = peerCount;
		changed(peerCount);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(Constants.PREFS_KEY_CONNECTIVITY_NOTIFICATION.equals(key))
			changed(peerCount);
	}
}
