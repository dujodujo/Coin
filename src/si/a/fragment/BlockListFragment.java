package si.a.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import si.a.adapter.BlocksAdapter;
import si.a.application.WalletApplication;
import si.a.loader.BlockLoader;
import si.a.loader.TransactionsLoader;
import si.a.service.BlockchainService;
import si.a.service.BlockchainServiceImplementation;
import si.a.coin.AbstractWalletActivity;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Wallet;

public class BlockListFragment extends ListFragment {
	
	public static final String TAG = BlockListFragment.class.getName();
	
	private AbstractWalletActivity walletActivity;
	private WalletApplication walletApplication;
	private Wallet wallet;
	private LoaderManager loaderManger;
	private BlockchainService blockchainService;
	
	private BlocksAdapter adapter;
	private Set<Transaction> transactions;
	private List<StoredBlock> blocks;
	
	private static final int BLOCK_LOADER = 0;
	private static final int TRANSACTION_LOADER = 1;
	private static final int MAX_BLOCKS = 32;
	private static final int MAX_CONNECTED_PEERS = 1;
	
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		public final String TAG = ServiceConnection.class.getName();

		public void onServiceConnected(ComponentName name, IBinder binder) {
			Log.i(TAG, "onServiceConnected");
			
			blockchainService = ((BlockchainServiceImplementation.LocalBinder) binder).getService();
			loaderManger.initLoader(BLOCK_LOADER, null, blockLoaderCallbacks);
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "onServiceDisconnected");

			loaderManger.destroyLoader(BLOCK_LOADER);
			blockchainService = null;
		}
	};
	
	private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.notifyDataSetChanged();
		}
	};
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "onAttach");
		
		this.walletActivity = (AbstractWalletActivity) activity;
		this.walletApplication = walletActivity.getWalletApplication();
		this.wallet = walletApplication.getWallet();
		this.loaderManger = getLoaderManager();
		this.blocks = new ArrayList<StoredBlock>(MAX_BLOCKS);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "onActivityCreated");
		
		walletActivity.bindService(new Intent(walletActivity, BlockchainServiceImplementation.class), serviceConnection, 
			Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		
		this.adapter = new BlocksAdapter(walletActivity, wallet, MAX_CONNECTED_PEERS, transactions);
		setListAdapter(this.adapter);
		Log.i(TAG, "onCreateEnd");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		walletActivity.unregisterReceiver(timeReceiver);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		walletActivity.registerReceiver(timeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		this.loaderManger.initLoader(TRANSACTION_LOADER, null, transactionLoaderCallbacks);
		this.adapter.notifyDataSetChanged();
	}

	public void clear() {
		this.blocks.clear();
		this.adapter.notifyDataSetChanged();
	}
		
	public void replace(Collection<StoredBlock> blocks) {
		this.blocks.clear();
		this.blocks.addAll(blocks);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final StoredBlock storedBlock = this.adapter.getItem(position);
		
		walletActivity.startActionMode(new ActionMode.Callback() {
			
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				mode.setTitle(Integer.toString(storedBlock.getHeight()));
				mode.setSubtitle(storedBlock.getHeader().getHashAsString());
				return true;
			}
			
			public void onDestroyActionMode(ActionMode mode) {
			}
			
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.blocks_context, menu);
				return true;
			}
			
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				return false;
			}
		});
	}

	private final LoaderCallbacks<List<StoredBlock>> blockLoaderCallbacks = new LoaderCallbacks<List<StoredBlock>>() {
		public final String TAG = LoaderCallbacks.class.getName();

		public Loader<List<StoredBlock>> onCreateLoader(int id, Bundle args) {
			Log.i(TAG, "onCreateLoader");
			return new BlockLoader(walletActivity, blockchainService, MAX_BLOCKS);
		}

		public void onLoadFinished(Loader<List<StoredBlock>> loader, List<StoredBlock> blocks) {
			Log.i(TAG, "onLoadFinished");

			replace(blocks);
			Loader<Set<Transaction>> transactionLoader = loaderManger.getLoader(TRANSACTION_LOADER);
			if(transactionLoader != null && transactionLoader.isStarted())
				transactionLoader.forceLoad();
		}

		public void onLoaderReset(Loader<List<StoredBlock>> loader) {
			clear();
		}
	};
	
	private final LoaderCallbacks<Set<Transaction>> transactionLoaderCallbacks = new LoaderCallbacks<Set<Transaction>>() {

		public Loader<Set<Transaction>> onCreateLoader(int id, Bundle args) {
			return new TransactionsLoader(walletActivity, wallet);
		}

		public void onLoadFinished(Loader<Set<Transaction>> loader, Set<Transaction> transactions) {
			BlockListFragment.this.transactions = transactions;
			BlockListFragment.this.adapter.notifyDataSetChanged();
		}

		public void onLoaderReset(Loader<Set<Transaction>> loader) {
			BlockListFragment.this.transactions.clear();
			BlockListFragment.this.transactions = null;
			BlockListFragment.this.adapter.notifyDataSetChanged();
		}
	};
}