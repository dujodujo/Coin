package si.a.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import si.a.adapter.WalletAddressesAdapter;
import si.a.provider.AddressBookProvider;
import si.a.coin.AddressBookActivity;
import si.a.coin.app.R;
import si.a.application.WalletApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.bitcoin.core.AbstractWalletEventListener;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.WalletEventListener;

public class WalletAddressesFragment extends ListFragment {
	
	private static final String TAG = "WalletAddressesFragment";
	private AddressBookActivity activity;
	private WalletApplication application;
	private Wallet wallet;
	private ContentResolver contentResolver;
	private SharedPreferences sharedPreferences;
	private WalletAddressesAdapter adapter;
	
	@Override
	public void onAttach(Activity activity) {
		Log.i(TAG, "onAttach");
		super.onAttach(activity);

		this.activity = (AddressBookActivity) activity;
		this.application = (WalletApplication) activity.getApplication();
		this.contentResolver = activity.getContentResolver();
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
		this.wallet = application.getWallet();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		adapter = new WalletAddressesAdapter(activity.getApplicationContext(), wallet);
		Address currentAddress = application.determineSelectedAddress();
		adapter.setSelectedAddress(currentAddress.toString());
		setListAdapter(this.adapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		contentResolver.registerContentObserver(AddressBookProvider.contentUri(activity.getPackageName()), true, contentObserver);
		wallet.addEventListener(walletListener);
		walletListener.onKeyAdded(null);
		updateView();
	}

	@Override
	public void onPause() {
		wallet.removeEventListener(walletListener);
		contentResolver.unregisterContentObserver(contentObserver);
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.wallet_addresses_fragment_options, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.wallet_addresses_options_add:
				handleAddAddress();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}
	
	private void handleAddAddress() {
		new AlertDialog.Builder(activity).setTitle(
				R.string.wallet_addresses_fragment_add_dialog_title)
			.setMessage(R.string.wallet_addresses_fragment_add_dialog_message)
			.setPositiveButton(R.string.button_add, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					application.addKeyToWallet();
					activity.updateFragment();
				}
			}).setNegativeButton(R.string.button_cancel, null).show();
	}
	
	private void updateView() {
		if(adapter != null)
			adapter.notifyDataSetChanged();
	}
	
	private Handler handler = new Handler();
	
	private ContentObserver contentObserver = new ContentObserver(handler) {};
	
	private WalletEventListener walletListener = new AbstractWalletEventListener() {

		@Override
		public void onKeyAdded(ECKey key) {
			super.onKeyAdded(key);
			
			final Iterable<ECKey> keys = application.getWallet().getKeys();
			final Collection<ECKey> keyCollection = (Collection<ECKey>) keys;			
			handler.post(new Runnable() {
				@Override
				public void run() {
					adapter.addKeys(keyCollection);
				}
			});
		}
	};
}