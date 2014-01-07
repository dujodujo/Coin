package si.a.adapter;

import com.google.bitcoin.core.Wallet;

import android.content.Context;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SendingAddressesAdapter extends BaseAdapter {
	
	private Context context;
	private Wallet wallet;
	private LayoutInflater inflater;
	
	public SendingAddressesAdapter(Context context, Wallet wallet, String[] xyy, int[] xyz, int x) {
		super();
		
		this.context = context;
		this.wallet = wallet;
		this.inflater = LayoutInflater.from(null);
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}
}
