package si.a.fragment;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.uri.BitcoinURI;

import android.app.Activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;

import si.a.application.WalletApplication;
import si.a.util.Constants;
import si.a.util.WalletUtils;
import si.a.coin.AddressBookActivity;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public final class WalletAddressFragment extends Fragment {
	
	public static final String TAG = WalletAddressFragment.class.getName();
	
	private WalletActivity activity;
	private WalletApplication application;
	private SharedPreferences sharedPreferences;
	private View coinAddressButton;
	private TextView coinAddressLabel;
	private ImageView coinAddressQrView;
	
	private Address lastSelectedAddress;
	private Bitmap bitmap;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.activity = (WalletActivity) activity;
		this.application = (WalletApplication) activity.getApplication();
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		return inflater.inflate(R.layout.wallet_address_fragment, container, false);
	}	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.i(TAG, "onViewCreated");

		coinAddressButton = (View) view.findViewById(R.id.coin_address_button);
		coinAddressLabel = (TextView) view.findViewById(R.id.coin_address_label);
		coinAddressQrView = (ImageView) view.findViewById(R.id.coin_address_qr);
		
		coinAddressButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AddressBookActivity.start(activity, false);
			}
		});
		
		coinAddressLabel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startShowQR();
			}
		});
		
		coinAddressQrView.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startShowQR();
				//startActivity(new Intent(activity, RequestCoinActivity.class));
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener);
		//updateView();
	}
	
	private final OnSharedPreferenceChangeListener preferenceListener = new OnSharedPreferenceChangeListener() {
		
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			//if(Constants.PREFS_KEY_SELECTED_ADDRESS.equals(key))
				//updateView();
		}
	};
	
	private void updateView() {
		//final Address selectedAddress = application.determineSelectedAddress();
		//application.determineSelectedAddress();
		
		/*
		if(!selectedAddress.equals(lastSelectedAddress)) {
			lastSelectedAddress = selectedAddress;
			
			coinAddressLabel.setText(WalletUtils.formatAddress(selectedAddress, 
				Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE));
			
			String coinAddress = BitcoinURI.convertToBitcoinURI(selectedAddress, null, null, null);
			int size = (int) (256 * getResources().getDisplayMetrics().density);
			
			bitmap = QR.getBitmap(coinAddress, size);
			coinAddressQrView.setImageBitmap(bitmap);
		}
		*/
	}
	
	private void startShowQR() {
		BitmapFragment.show(getFragmentManager(), bitmap);
	}
}