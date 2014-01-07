package si.a.fragment;

import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import si.a.adapter.TransactionsAdapter;
import si.a.application.WalletApplication;
import si.a.listener.WalletChangeListener;
import si.a.provider.AddressBookProvider;
import si.a.util.Constants;
import si.a.util.WalletUtils;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.TransactionConfidence.ConfidenceType;

public class TransactionListFragment extends ListFragment implements LoaderCallbacks<List<Transaction>>, 
		OnSharedPreferenceChangeListener {
	
	public static final String TAG = TransactionListFragment.class.getName();
	
	public enum Direction { RECEIVED, SENT };
	
	private WalletActivity activity;
	private WalletApplication walletApplication;
	private Wallet wallet;
	private ContentResolver contentResolver;
	private LoaderManager loaderManager;
	private TransactionsAdapter adapter;
	
	private static final String KEY_DIRECTION = "direction";
	private static final int SHOW_TRANSACTIONS_BYTES = 2500;
	
	private final Handler handler = new Handler();
	private Direction direction;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.activity = (WalletActivity) activity;
		this.walletApplication = (WalletApplication) activity.getApplication();
		this.wallet = walletApplication.getWallet();
		this.contentResolver = activity.getContentResolver();
		this.loaderManager = getLoaderManager();
	}	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		
		setRetainInstance(true);
		this.direction = (Direction) getArguments().getSerializable(KEY_DIRECTION);
		
		this.adapter = new TransactionsAdapter(activity, wallet, 3);
		setListAdapter(adapter);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.i(TAG, "onViewCreated");
		
		String text = direction == Direction.SENT ? "empty text sent" : "empty text received";
		SpannableStringBuilder emptyText = new SpannableStringBuilder(text);
		emptyText.setSpan(new StyleSpan(Typeface.BOLD), 0, emptyText.length(), SpannableStringBuilder.SPAN_POINT_MARK);
		setEmptyText(emptyText);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		
		contentResolver.registerContentObserver(AddressBookProvider.contentUri(activity.getPackageName()), 
			true, addressBookObserver);
		loaderManager.initLoader(0, null, this);
		//wallet.addEventListener(transactionChangeListener);
		
		updateView();
	}
	
	private void updateView() {
		adapter.clear();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		final Transaction tx = (Transaction) adapter.getItem(position);
		
		if(tx == null)
			startClicked();
		else
			startTransactionClicked(tx);
	}
	
	private void startClicked() {
		//activity.startExportKey();
	}
	
	private void startTransactionClicked(final Transaction tx) {
		activity.startActionMode(new ActionMode.Callback() {
			
			private Address address;
			private byte[] transactions;
			
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				final MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.wallet_options, menu);
				return true;
			}

			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				
				try {
					
					final Date time = tx.getUpdateTime();
					final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(activity);
					final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(activity);
					
					mode.setTitle(time != null ? (DateUtils.isToday(time.getTime()) ? "today" : dateFormat.format(time))
							+ ", " + timeFormat.format(time) : null);
					
					BigInteger value = tx.getValue(wallet);
					final boolean sent = value.signum() < 0;
					
					address = sent ? WalletUtils.getFinishAddress(tx) : WalletUtils.getStartAddress(tx);
					
					String label = null;
					if(tx.isCoinBase()) {
						label = getString(R.string.wallet_transaction_fragment_coin);
					} else if(address != null) {
						label = AddressBookProvider.resolveLabel(activity, address.toString());
					} else {
						label = "?";
					}
					
					final String prefix = getString(sent ? R.string.symbol_finish : R.string.symbol_start) + " ";
					
					mode.setSubtitle(label != null ? prefix + label : WalletUtils.formatAddress(prefix, 
						address, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE));
					
					menu.findItem(R.id.wallet_transactions_context_edit_address).setVisible(address != null);
					transactions = tx.unsafeBitcoinSerialize();
					menu.findItem(R.id.wallet_transactions_context_show).setVisible(transactions.length < SHOW_TRANSACTIONS_BYTES);
					
					return true;
					
				} catch(final ScriptException ex) {
					return false;
				}
			}
			
			public void onDestroyActionMode(ActionMode mode) {}
			
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				
				switch(item.getItemId()) {
					case R.id.wallet_transactions_context_edit_address:
						startEditAddress(tx);
						
						mode.finish();
						return true;
					case R.id.wallet_transactions_context_show:
						startShow();
						
						mode.finish();
						return true;
					case R.id.wallet_transactions_context_explorer:						
						startActivity(new Intent(Intent.ACTION_VIEW, 
							Uri.parse(Constants.BLOCK_EXPLORER_BASE_URL + "tx/" + tx.getHashAsString())));
						mode.finish();
						return true;
				}
				
				return false;
			}
			
			private void startEditAddress(final Transaction tx) {}
			
			private void startShow() {}
		});
	}

	private WalletChangeListener transactionChangeListener = new WalletChangeListener() {

		public void onKeyAdded(ECKey key) {}

		@Override
		public void onWalletChanged() {
			adapter.notifyDataSetChanged();
		}
	};
	
	public static TransactionListFragment getInstance(final Direction direction) {
		Log.i(TAG, "getInstance");
		final TransactionListFragment fragment = new TransactionListFragment();
		
		final Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_DIRECTION, direction);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	private final ContentObserver addressBookObserver = new ContentObserver(handler) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			adapter.clear();
		}
	};
	
	public Loader<List<Transaction>> onCreateLoader(int id, Bundle bundle) {
		Log.i(TAG, "onCreateLoader");
		return new TransactionLoader(activity, wallet, direction);
	}

	public void onLoadFinished(final Loader<List<Transaction>> loader, final List<Transaction> transactions) {
		Log.i(TAG, "onLoadFinished");

		adapter.replace(transactions);
	}

	public void onLoaderReset(Loader<List<Transaction>> transactions) {}
	
	private static class TransactionLoader extends AsyncTaskLoader<List<Transaction>> {
		
		public static final String TAG = TransactionLoader.class.getName();
		private final Wallet wallet;
		private Direction direction;
		
		public TransactionLoader(Context context, final Wallet wallet, final Direction direction) {
			super(context);
			Log.i(TAG, "TransactionLoader");
			
			this.wallet = wallet;
			this.direction = direction;
		}
		
		@Override
		protected void onStartLoading() {
			super.onStartLoading();
			Log.i(TAG, "onStartLoading");
			
			//wallet.addEventListener(transactionAddRemoveListener);
			//transactionAddRemoveListener.onReorganize(null);
			forceLoad();
		}
		
		@Override
		protected void onStopLoading() {
			super.onStopLoading();
			Log.i(TAG, "onStopLoading");
			
			//wallet.removeEventListener(transactionAddRemoveListener);
			//transactionAddRemoveListener.removeCallbacks();
		}

		@Override
		public List<Transaction> loadInBackground() {
			Log.i(TAG, "loadInBackground");

			List<Transaction> filteredTransactions = new ArrayList<Transaction>(0);
			Collections.sort(filteredTransactions, transactionComparator);

			/*
			final Set<Transaction> transactions = wallet.getTransactions(false, true);
			final List<Transaction> filteredTransactions = new ArrayList<Transaction>(transactions.size());
			
			try {
				for(final Transaction tx : transactions) {
					final boolean sent = tx.getValue(wallet).signum() < 0;
					if((direction == Direction.RECEIVED && !sent) || direction == null || (direction == Direction.SENT && sent))
						filteredTransactions.add(tx);
				}
			} catch(final ScriptException se) {}
			Collections.sort(filteredTransactions, transactionComparator);
			*/
			return filteredTransactions;
		}
		
		private final WalletChangeListener transactionAddRemoveListener = new WalletChangeListener() {

			public void onKeyAdded(ECKey key) {}

			@Override
			public void onWalletChanged() { forceLoad(); }
		};
				
		private static final Comparator<Transaction> transactionComparator = new Comparator<Transaction>() {
			
			public int compare(final Transaction tx1, final Transaction tx2) {
				
				final boolean pending1 = tx1.getConfidence().getConfidenceType() == ConfidenceType.BUILDING;
				final boolean pending2 = tx2.getConfidence().getConfidenceType() == ConfidenceType.BUILDING;
				
				if(pending1 != pending2)
					return pending1 ? 1 : -1;
				
				final Date update1 = tx1.getUpdateTime();
				final long time1 = update1 != null ? update1.getTime() : 0;
				final Date update2 = tx2.getUpdateTime();
				final long time2 = update2 != null ? update2.getTime() : 0;
				
				if(time1 > time2)
					return -1;
				else if(time1 < time2)
					return 1;
				else
					return 0;
			}
		};
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateView();
	}
}
