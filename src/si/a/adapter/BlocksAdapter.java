package si.a.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import si.a.util.WalletUtils;
import si.a.coin.AbstractWalletActivity;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Wallet;

public class BlocksAdapter extends BaseAdapter {
	
	public static final String TAG = BlocksAdapter.class.getName();
	
	private AbstractWalletActivity walletActivity;
	private Wallet wallet;
	private TransactionsAdapter transactionAdapter;
	private List<StoredBlock> blocks;
	private Set<Transaction> transactions;
	
	private static final int MAX_BLOCKS = 32;
	
	public BlocksAdapter(Context activity, Wallet wallet, int connectedPeers, Set<Transaction> transactions) {
		super();
		Log.i(TAG, "BlocksAdapter");
		
		this.walletActivity = (AbstractWalletActivity) activity;
		this.wallet = wallet;
		this.transactionAdapter = new TransactionsAdapter(walletActivity, wallet, connectedPeers);
		this.blocks = new ArrayList<StoredBlock>(MAX_BLOCKS);
		this.transactions = transactions;
	}
	
	public int getCount() {
		return this.blocks.size();
	}

	public StoredBlock getItem(int location) {
		return this.blocks.get(location);
	}

	public long getItemId(int position) {
		Log.i(TAG, "getItemId");

		return WalletUtils.getLongHash(blocks.get(position).getHeader().getHash());
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i(TAG, "getView");

		ViewGroup row;
		if(convertView == null)
			row = (ViewGroup) LayoutInflater.from(walletActivity).inflate(R.layout.block_row, null);
		else
			row = (ViewGroup) convertView;
		
		StoredBlock storedBlock = getItem(position);
		Block header = storedBlock.getHeader();
		
		TextView rowHeight = (TextView) row.findViewById(R.id.block_list_height);
		int height = storedBlock.getHeight();
		rowHeight.setText(Integer.toString(height));
		
		TextView rowHash = (TextView) row.findViewById(R.id.block_list_hash);
		rowHash.setText(WalletUtils.formatHash(null, header.getHashAsString(), 8, 0, ' '));
		
		TextView rowTime = (TextView) row.findViewById(R.id.block_list_time);
		long time = header.getTimeSeconds() * DateUtils.SECOND_IN_MILLIS;
		rowTime.setText(DateUtils.getRelativeDateTimeString(walletActivity, 
			time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));

		int currentChildPosition = 0;
		int transactionChildCount = row.getChildCount() - currentChildPosition;
		
		if(transactions != null) {
			for(Transaction tx : transactions) {
				if(tx.getAppearsInHashes().contains(header.getHash())) {
					final View view;
					if(transactionChildCount > currentChildPosition) {
						view = row.getChildAt(currentChildPosition);
					} else {
						view = LayoutInflater.from(walletActivity).inflate(R.layout.transaction_row_line, null);
					}
					transactionAdapter.bindView(view, tx);
					currentChildPosition++;
				}
			}
		}
		return row;
	}
}