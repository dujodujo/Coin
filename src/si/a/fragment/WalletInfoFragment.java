package si.a.fragment;

import si.a.service.BlockchainService;
import si.a.util.Constants;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

public class WalletInfoFragment extends Fragment implements OnSharedPreferenceChangeListener {
	
	private WalletActivity walletActivity;
	private int download;
	private TextView messageView;
	private SharedPreferences preferences;
	private BlockchainBroadcastReciver broadcastReceiver = new BlockchainBroadcastReciver();
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.walletActivity = (WalletActivity) activity;
		this.preferences = PreferenceManager.getDefaultSharedPreferences(walletActivity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		messageView = (TextView) inflater.inflate(R.layout.wallet_info_fragment, container);
		
		messageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				boolean backup = preferences.getBoolean(Constants.PREFS_KEY_BACKUP, true);
				//if(backup)
				//walletActivity.startExportKey();
				//else
				InfoDialogFragment.page(getFragmentManager(), "safety");
			}
		});
		return messageView;
	}
	
	@Override
	public void onPause() {
		walletActivity.unregisterReceiver(broadcastReceiver);
		preferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		preferences.registerOnSharedPreferenceChangeListener(this);
		walletActivity.registerReceiver(broadcastReceiver, new IntentFilter(BlockchainService.ACTION_BLOCKCHAIN_STATE));
		updateView();
	}

	private void updateView() {
		boolean backup = preferences.getBoolean(Constants.PREFS_KEY_BACKUP, true);
		boolean safety = preferences.getBoolean(Constants.PREFS_KEY_DISCLAIMER, true);
		
		int progress = 0;
		if(download == BlockchainService.ACTION_BLOCKCHAIN_STATE_DOWNLOAD_OK)
			progress = 0;
		else if((download & BlockchainService.ACTION_BLOCKCHAIN_STATE_DOWNLOAD_STORAGE_PROBLEM) != 0)
			progress = R.string.blockchain_state_progress_problem_storage;
		else if((download & BlockchainService.ACTION_BLOCKCHAIN_STATE_DOWNLOAD_NETWORK_PROBLEM) != 0)
			progress = R.string.blockchain_state_progress_problem_network;
			
		final SpannableStringBuilder text = new SpannableStringBuilder();
		
		if(progress != 0)
			text.append(Html.fromHtml("<b>" + getString(progress) + "</b>"));
		if(progress != 0 && (backup || safety))
			text.append('\n');
		if (backup)
			text.append(Html.fromHtml(getString(R.string.wallet_info_fragment_backup)));
		if (backup && safety)
			text.append('\n');
		if (safety)
			text.append(Html.fromHtml(getString(R.string.wallet_info_fragment_safety)));
		text.append('\n');
		text.append("Wallet Disclaimer Text Example");
		messageView.setText(text);
		
		View view = getView();
		ViewParent parent = view.getParent();
		View fragment = parent instanceof FrameLayout ? (FrameLayout) parent : view;
		fragment.setVisibility(text.length() > 0 ? View.VISIBLE : View.GONE);
	}
	
	private final class BlockchainBroadcastReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			download = intent.getIntExtra(BlockchainService.ACTION_BLOCKCHAIN_STATE_DOWNLOAD, 
				BlockchainService.ACTION_BLOCKCHAIN_STATE_DOWNLOAD_OK);
			updateView();
		}
	}
	
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if(Constants.PREFS_KEY_DISCLAIMER.equals(key) || Constants.PREFS_KEY_BACKUP.equals(key))
			updateView();
	}
}