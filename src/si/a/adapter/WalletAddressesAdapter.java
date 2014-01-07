package si.a.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import si.a.provider.AddressBookProvider;
import si.a.util.Constants;
import si.a.util.WalletUtils;
import si.a.coin.app.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;

public class WalletAddressesAdapter extends BaseAdapter {

	private static String TAG = WalletAddressesAdapter.class.getName();
	private Context context;
	private Wallet wallet;
	private LayoutInflater inflater;
	
	private List<ECKey> keys;
	private String selectedAddress;
	
	public WalletAddressesAdapter(Context context, Wallet wallet) {
		Log.i(TAG, "WalletAddressesAdapter");
		Resources resources = context.getResources();
	
		this.keys = new ArrayList<ECKey>();
		this.context = context;
		this.wallet = wallet;
		this.inflater = LayoutInflater.from(context);
	}
	
	public void addKeys(final Collection<ECKey> keys) {
		this.keys.clear();
		//Iterator<ECKey> keyIterator =  keys.iterator();
		//while(keyIterator.hasNext()) { this.keys.add(keyIterator.next()); }
		this.keys.addAll(keys);
		notifyDataSetChanged();
	}
	
	public void setSelectedAddress(String selectedAddress) {
		Log.i(TAG, "setSelectedAddress");

		this.selectedAddress = selectedAddress;
		notifyDataSetChanged();
	}
	
	public int getCount() { 
		return keys.size();
	}

	public Object getItem(int item) { 
		return keys.get(item);
	}

	public long getItemId(int itemId) { 
		return keys.get(itemId).hashCode(); 
	}

	public View getView(int position, View row, ViewGroup parent) {
		Log.i(TAG, "getView");
		
		ECKey key = (ECKey) getItem(position);
		Address address = key.toAddress(Constants.NETWORK_PARAMETERS);
		Log.i(TAG, address.toString() + " : address WalletAddressesAdapter");
		
		if(row == null)
			row = inflater.inflate(R.layout.address_book_rows, null);
		final boolean isDefaultAddress = address.toString().equals(selectedAddress);
		row.setBackgroundResource(isDefaultAddress ? R.color.bg_bright : R.color.bg_less);
		
		final TextView addressView = (TextView) row.findViewById(R.id.address_book_row_address);
		addressView.setText(WalletUtils.formatAddress(address, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE));
		Log.i(TAG, addressView.toString() + " addressView text");
		
		final TextView labelView = (TextView) row.findViewById(R.id.address_book_row_label);
		String label = AddressBookProvider.resolveLabel(context, address.toString());
		
		if(label != null) {
			labelView.setText(label);
			int color = android.R.color.holo_blue_bright;
			labelView.setTextColor(color);
		} else {
			labelView.setText("Addres unlabeled");
			int color = android.R.color.holo_blue_bright;
			labelView.setTextColor(color);
		}
		
		final TextView messageView = (TextView) row.findViewById(R.id.adress_book_row_message);
		messageView.setVisibility(View.VISIBLE);
		return row;
	}
}