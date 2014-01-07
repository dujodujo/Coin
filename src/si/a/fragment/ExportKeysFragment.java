package si.a.fragment;

import si.a.listener.ButtonListener;
import si.a.listener.PasswordCheckListener;
import si.a.coin.AbstractWalletActivity;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class ExportKeysFragment extends Fragment {
	
	public static String TAG = "ExportKeysFragment";
	private WalletActivity activity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}
	
	@Override
	public void onAttach(Activity activity) {
		Log.i(TAG, "onAttach");
		super.onAttach(activity);
		
		this.activity = (WalletActivity) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onViewCreated");
		return inflater.inflate(R.layout.export_keys_fragment, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.i(TAG, "onViewCreated");

		final Button buttonPassword = (Button) view.findViewById(R.id.export_button_password);
		final EditText passwordView = (EditText) view.findViewById(R.id.export_keys_dialog_password);
		ButtonListener passwordButtonLitener = new ButtonListener(passwordView, buttonPassword);
		passwordView.addTextChangedListener(passwordButtonLitener);

		CheckBox showView = (CheckBox) view.findViewById(R.id.export_keys_dialog_show);
		showView.setOnCheckedChangeListener(new PasswordCheckListener(passwordView));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
		builder.setInverseBackgroundForced(true);
		builder.setTitle(R.string.export_keys_dialog_title);
		builder.setView(passwordView);
		
		builder.setPositiveButton(R.string.export_keys_dialog_button_export, 
			new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String password = passwordView.getText().toString().trim();
				passwordView.setText(null);
				
				exportPrivateKeys(password);
			}
		});
		
		builder.setNegativeButton(R.string.button_cancel, new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				passwordView.setText(null);
			}
		});
		
		builder.setOnCancelListener(new OnCancelListener() {

			public void onCancel(DialogInterface dialog) {
				passwordView.setText(null);
			}
		});
		builder.create();
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			activity.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void exportPrivateKeys(String password) {
		Log.i(TAG, "exportPrivateKeys");
	}
}
