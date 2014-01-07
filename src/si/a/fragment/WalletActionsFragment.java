package si.a.fragment;

import si.a.coin.WalletActivity;
import si.a.coin.app.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;

public class WalletActionsFragment extends Fragment {

	private WalletActivity walletActivity;
	private Button requestCoins;
	private Button sendCoins;
	private Button sendCoinsQr;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.walletActivity = (WalletActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View view = inflater.inflate(R.layout.wallet_actions_fragment, container);
		
		requestCoins = (Button) view.findViewById(R.id.wallet_actions_request);
		requestCoins.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//walletActivity.startRequestCoins();
			}
		});
		
		sendCoins = (Button) view.findViewById(R.id.wallet_actions_send);
		sendCoins.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//walletActivity.startSendCoins();
			}
		});
		
		sendCoinsQr = (Button) view.findViewById(R.id.wallet_actions_send_qr);
		sendCoinsQr.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				//walletActivity.startScan();
			}
		});
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//updateView();
	}
	
	private void updateView() {
		View view = getView();
		ViewParent parent = view.getParent();
		View fragment = (FrameLayout) parent;
		fragment.setVisibility(View.VISIBLE);
	}
}
