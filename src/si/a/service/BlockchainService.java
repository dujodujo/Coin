package si.a.service;

import java.util.List;

import android.R;

import com.google.bitcoin.core.Peer;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;

public interface BlockchainService {
	
	public static final String ACTION_PEER_STATE = R.class.getPackage().getName() + ".peer_state";
	public static final String ACTION_PEER_STATE_NUM_PEERS = "number_of_peers";
	
	public static final String ACTION_BLOCKCHAIN_STATE = R.class.getPackage().getName() + ".blockchain_state";
	public static final String ACTION_BLOCKCHAIN_STATE_DOWNLOAD = "download";
	public static final String ACTION_BLOCKCHAIN_STATE_BEST_CHAIN_DATE = "best_chain_date";
	public static final String ACTION_BLOCKCHAIN_STATE_BEST_CHAIN_HEIGHT = "best_chain_height";
	public static final String ACTION_BLOCKCHAIN_STATE_REPLAYING = "replaying";
	
	public static final int ACTION_BLOCKCHAIN_STATE_DOWNLOAD_OK = 0;
	public static final int ACTION_BLOCKCHAIN_STATE_DOWNLOAD_STORAGE_PROBLEM = 1;
	public static final int ACTION_BLOCKCHAIN_STATE_DOWNLOAD_NETWORK_PROBLEM = 2;
	
	public static final String ACTION_CANCEL_COINS_RECEIVED = R.class.getPackage().getName() + "cancel_coins_received";
	public static final String ACTION_RESET_BLOCKCHAIN = R.class.getPackage().getName() + ".reset_blockchain";
	
	void broadcastTransaction(Transaction transaction);
	
	List<Peer> getConnectedPeers();
	
	List<StoredBlock> getRecentBlocks(int maxBlocks);
}
