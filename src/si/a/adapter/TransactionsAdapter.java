package si.a.adapter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import si.a.provider.AddressBookProvider;
import si.a.util.Constants;
import si.a.util.WalletUtils;
import si.a.coin.app.R;
import si.a.wiew.CurrencyTextView;
import si.a.wiew.ProgressView;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionConfidence;
import com.google.bitcoin.core.TransactionConfidence.ConfidenceType;
import com.google.bitcoin.core.Wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TransactionsAdapter extends BaseAdapter {
	public static final String TAG = TransactionsAdapter.class.getName();
	
	private Context context;
	private LayoutInflater inflater;
	private Wallet wallet;
	private SharedPreferences sharedPrefernces;
	
	private List<Transaction> transactions = new ArrayList<Transaction>();
	private Map<String, String> labelCache = new HashMap<String, String>();
	
	private int coinPrecision;
	private int connectedPeers;
	private final String CACHE_NULL = "";
	
	private static final int VIEW_TYPE_TRANSACTION = 0;
	private static final int VIEW_TYPE_WARNING = 1;
	
	private static final String CONFIDENCE_SYMBOL_DEAD = "-,-";
	private static final String CONFIDENCE_SYMBOL_UNKNOWN = "?";
		
	public TransactionsAdapter(Context context, Wallet wallet, int connectedPeers) {
		Log.i(TAG, "TransactionsAdapter");
		
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.wallet = wallet;
		this.connectedPeers = connectedPeers;
	}
	
	public void setPrecision(final int precision) {
		this.coinPrecision = precision;
		notifyDataSetChanged();
	}
	
	public void clear() {
		transactions.clear();
		notifyDataSetChanged();
	}
	
	public int getCount() {
		return transactions.size();
	}

	public Object getItem(int position) {
		return transactions.get(position);
	}

	public long getItemId(int position) {
		Log.i(TAG, "getItemId");

		if(position == transactions.size())
			return 0;
		return WalletUtils.getLongHash(transactions.get(position).getHash());
	}
	
	@Override
	public int getItemViewType(int position) {
		Log.i(TAG, "getItmeViewType");

		if(position == transactions.size())
			return VIEW_TYPE_WARNING;
		else
			return VIEW_TYPE_TRANSACTION;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	public View getView(int position, View row, ViewGroup parent) {
		Log.i(TAG, "getView");
		
		int type = getItemViewType(position);
		if(type == VIEW_TYPE_TRANSACTION) {
			if(row == null)
				row = inflater.inflate(R.layout.transaction_row, null);
				
			Transaction tx = transactions.get(position);
			bindView(row, tx);
		} else if(type == VIEW_TYPE_WARNING) {
			if(row == null)
				row = inflater.inflate(R.layout.transaction_row, null);
			
			TextView messageView = (TextView) row.findViewById(R.id.transient_notification_text);
			messageView.setText("Message");
		} else {
			throw new IllegalStateException();
		}
		return row;
	}
	
	public void bindView(View row, Transaction tx) {
		Log.i(TAG, "bindView");
		
		final TransactionConfidence confidence = tx.getConfidence();
		final ConfidenceType confidenceType = confidence.getConfidenceType();
		
		boolean isSelf = confidence.getSource().equals(TransactionConfidence.Source.SELF);
		boolean isCoin = tx.isCoinBase();
		boolean isLocal = WalletUtils.isLocal(tx);
		
		try {
			final BigInteger value = tx.getValue(wallet);
			boolean sent = value.signum() < 0;
			
			final ProgressView rowConfidence = (ProgressView) row.findViewById(R.id.transaction_row_confidence);
			final TextView rowConfidenceText = (TextView) row.findViewById(R.id.transaction_row_confidence_text);
			
			if(confidenceType == ConfidenceType.UNKNOWN) {
				rowConfidence.setVisibility(View.VISIBLE);
				rowConfidenceText.setVisibility(View.GONE);
				
				rowConfidence.setProgress(1);
				rowConfidence.setMaxProgress(1);
				rowConfidence.setSize(confidence.numBroadcastPeers());
				rowConfidence.setColors(Color.GREEN, Color.CYAN);
			
			} else if(confidenceType == ConfidenceType.BUILDING) {
				rowConfidence.setVisibility(View.VISIBLE);
				rowConfidenceText.setVisibility(View.GONE);
				
				rowConfidence.setProgress(confidence.getDepthInBlocks());
				rowConfidence.setMaxProgress(isCoin ? Constants.NETWORK_PARAMETERS.getSpendableCoinbaseDepth()
						: Constants.MAX_NUM_CONFIRMATIONS);
				rowConfidence.setSize(1);
				rowConfidence.setMaxSize(connectedPeers-1);
				rowConfidence.setColors(Color.BLUE, Color.DKGRAY);
			
			} else if(confidenceType == ConfidenceType.DEAD) {
				rowConfidence.setVisibility(View.GONE);
				rowConfidenceText.setVisibility(View.VISIBLE);
				
				rowConfidenceText.setText(CONFIDENCE_SYMBOL_DEAD);
				rowConfidenceText.setTextColor(Color.RED);
			} else {
				rowConfidence.setVisibility(View.GONE);
				rowConfidenceText.setVisibility(View.VISIBLE);
				
				rowConfidenceText.setText(CONFIDENCE_SYMBOL_UNKNOWN);
				rowConfidenceText.setTextColor(Color.BLACK);
			}
			
			int textColor = confidenceType == ConfidenceType.DEAD ? Color.RED : Color.BLACK;
			
			TextView location = (TextView) row.findViewById(R.id.transaction_row_location);
			if(isLocal)
				location.setText(R.string.symbol_local);
			else if(sent) {
				location.setText(R.string.symbol_finish);
			} else {
				location.setText(R.string.symbol_start);
			}
			location.setTextColor(Color.BLUE);
			
			View rowCoin = row.findViewById(R.id.transaction_row_coinbase);
			rowCoin.setVisibility(isCoin ? View.VISIBLE : View.GONE);
			
			TextView rowAddress = (TextView) row.findViewById(R.id.transaction_row_address);
			Address address = sent ? WalletUtils.getFinishAddress(tx) : WalletUtils.getStartAddress(tx);
			
			String label;
			if(address != null)
				label = resolveLabel(address.toString());
			else
				label = "?";
			rowAddress.setTextColor(Color.RED);
			rowAddress.setText(label != null ? label : address.toString());
			rowAddress.setTypeface(label != null ? Typeface.DEFAULT : Typeface.MONOSPACE);
			
			
			CurrencyTextView rowValue = (CurrencyTextView) row.findViewById(R.id.transaction_row_value);
			rowValue.setTextColor(Color.BLACK);
			rowValue.setSigned(true);
			rowValue.setPrecision(Constants.COIN_MAX_PRECISION);
			rowValue.setAmount(value);
					
		} catch(ScriptException se) {
			throw new RuntimeException(se);
		}
	}
	
	public String resolveLabel(final String address) {
		Log.i(TAG, "resolveLabel");

		final String cachedLabel = labelCache.get(address);
		
		if(cachedLabel == null) {
			String label = AddressBookProvider.resolveLabel(context, address);
			if(label != null)
				labelCache.put(address, label);
			else
				labelCache.put(address, CACHE_NULL);
			return label;
		} else {
			return cachedLabel != CACHE_NULL ? cachedLabel : null;
		}
	}
	
	public void replace(Transaction tx) {
		Log.i(TAG, "replace");

		this.transactions.clear();
		this.transactions.add(tx);
		notifyDataSetChanged();
	}
	
	public void replace(Collection<Transaction> transactions) {
		Log.i(TAG, "replace");
		
		this.transactions.clear();
		this.transactions.addAll(transactions);
		notifyDataSetChanged();
	}
}