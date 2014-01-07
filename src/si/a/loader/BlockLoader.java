package si.a.loader;

import java.util.List;

import si.a.service.BlockchainService;

import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class BlockLoader extends AsyncTaskLoader<List<StoredBlock>> {
	
	public static final String TAG = BlockLoader.class.getName();
	
	private Context context;
	private BlockchainService service;
	private int maxBlocks;
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) { forceLoad(); }
	};
	
	public BlockLoader(Context context, BlockchainService service, int maxBlocks) {
		super(context);
		Log.i(TAG, "BlockLoader");
		
		this.context = context.getApplicationContext();
		this.service = service;
		this.maxBlocks = maxBlocks;
	}
	
	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		Log.i(TAG, "onStartLoading");

		context.registerReceiver(broadcastReceiver, new IntentFilter(BlockchainService.ACTION_BLOCKCHAIN_STATE));
	}

	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		Log.i(TAG, "onStopLoading");

		context.unregisterReceiver(broadcastReceiver);
	}

	@Override
	public List<StoredBlock> loadInBackground() {
		Log.i(TAG, "loadInBackground");

		return service.getRecentBlocks(maxBlocks);
	}
}
