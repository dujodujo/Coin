package si.a.adapter;

import si.a.provider.AddressBookProvider;
import si.a.util.Constants;
import si.a.util.WalletUtils;
import si.a.coin.AbstractWalletActivity;
import si.a.coin.app.R;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class AutoCompleteAddressAdapter extends CursorAdapter {
	
	public static final String TAG = AutoCompleteAddressAdapter.class.getName();
	
	public AutoCompleteAddressAdapter(Context context, Cursor cursor) {
		super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
		Log.i(TAG, "AutoCompleteAddressAdapter");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Log.i(TAG, "bindView");

		final String label = cursor.getString(cursor.getColumnIndex(AddressBookProvider.KEY_LABEL));
		final String address = cursor.getString(cursor.getColumnIndex(AddressBookProvider.KEY_ADDRESS));
		
		final ViewGroup viewGroup = (ViewGroup) view;
		final TextView labelView = (TextView) viewGroup.findViewById(R.id.address_book_row_label);
		labelView.setText(label);
		
		final TextView addressView = (TextView) viewGroup.findViewById(R.id.address_book_row_address);
		addressView.setText(WalletUtils.formatHash(address, Constants.ADDRESS_FORMAT_GROUP_SIZE, 
			Constants.ADDRESS_FORMAT_LINE_SIZE));
	} 	

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Log.i(TAG, "newView");

		LayoutInflater inflater = LayoutInflater.from(context);
		return inflater.inflate(R.layout.address_book_rows, parent, false);
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(AddressBookProvider.KEY_ADDRESS));
	}
}