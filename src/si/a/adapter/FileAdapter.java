package si.a.adapter;

import java.io.File;
import java.util.List;

import si.a.coin.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public abstract class FileAdapter extends ArrayAdapter<File> {
	
	private Context context;
	private LayoutInflater inflater;

	public FileAdapter(Context context, List<File> files) {
		super(context, 0, files);
		
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		File file = getItem(position);
		
		if(convertView == null)
			convertView = this.inflater.inflate(R.layout.spinner_item, null);
		
		final TextView textView = (TextView)convertView.findViewById(android.R.id.text1);
		textView.setText(file.getName());
		
		return convertView;
	}
}
