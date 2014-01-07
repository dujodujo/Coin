package si.a.fragment;

import java.util.ArrayList;

import com.google.bitcoin.core.Address;

import si.a.coin.AbstractWalletActivity;
import si.a.coin.app.R;
import si.a.provider.AddressBookProvider;
import si.a.util.Constants;
import si.a.util.WalletUtils;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class SendingAddressesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static String TAG = SendingAddressesFragment.class.getName();
	private AbstractWalletActivity activity;
	private LoaderManager loaderManager;
	
	private SimpleCursorAdapter adapter;
	private Handler handler = new Handler();
	
	private String walletAddressesSelection = null;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.activity = (AbstractWalletActivity) activity;
		this.loaderManager = getLoaderManager();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
		
		adapter = new SimpleCursorAdapter(activity, R.layout.address_book_rows, null, 
			new String[] { AddressBookProvider.KEY_LABEL, AddressBookProvider.KEY_ADDRESS }, 
			new int[] { R.id.address_book_row_label, R.id.address_book_row_address }, 0);
		
		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if(!AddressBookProvider.KEY_ADDRESS.equals(cursor.getColumnName(columnIndex)))
					return false;
				TextView textView = (TextView)view;
				textView.setText(WalletUtils.formatHash(cursor.getString(columnIndex), Constants.ADDRESS_FORMAT_GROUP_SIZE, 
					Constants.ADDRESS_FORMAT_LINE_SIZE));
				return true;
			}
		});
		setListAdapter(adapter);
		loaderManager.initLoader(0, null, this);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.send_coins_fragment_options, menu);
		//menu.findItem(R.id.send);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onListItemClick(ListView listView, View view, final int position, long id) {
		super.onListItemClick(listView, view, position, id);
		
		activity.startActionMode(new ActionMode.Callback() {
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.sending_addresses_options, menu);
				return true;
			}
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				String label = getLabel(position);
				mode.setTitle(label);
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch(item.getItemId()) {
					case R.id.sending_addresses_options_send:
						return true;
					case R.id.sending_addresses_options_edit:
						return true;
					case R.id.sending_addresses_options_remove:
						return true;
				}
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {}
			
			private String getLabel(final int position) {
				Cursor cursor = (Cursor) adapter.getItem(position);
				return cursor.getString(cursor.getColumnIndex(AddressBookProvider.KEY_LABEL));
			}			
		});
	}

	public void setWalletAddresses(ArrayList<Address> addresses) {
		Log.i(TAG, "setWalletAddresses");
		final StringBuilder builder = new StringBuilder();
		for(Address address : addresses)
			builder.append(address.toString()).append(", ");
		if(addresses.size() > 0)
			builder.setLength(builder.length()-1);
		Log.i(TAG, builder.toString());
		walletAddressesSelection = builder.toString();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		final Uri uri = AddressBookProvider.contentUri(activity.getPackageName());
		return new CursorLoader(activity, uri, null, AddressBookProvider.SELECTION_IN,
			new String[] {walletAddressesSelection}, AddressBookProvider.KEY_LABEL);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
}