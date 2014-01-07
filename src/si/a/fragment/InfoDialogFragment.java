package si.a.fragment;

import org.apache.http.util.LangUtils;

import si.a.coin.WalletActivity;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;

public class InfoDialogFragment extends DialogFragment {
	
	public static final String TAG = InfoDialogFragment.class.getName();
	
	private static final String FRAGMENT_TAG = InfoDialogFragment.class.getName();
	private static final String KEY_PAGE = "page";
	private WalletActivity walletActivity;
	private WebView webView;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "onAttach");
		
		this.walletActivity = (WalletActivity) activity;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.i(TAG, "onCreateDialog");

		Bundle bundle = getArguments();
		String page = bundle.getString(KEY_PAGE);
		
		this.webView = new WebView(walletActivity);
		this.webView.loadUrl("file://android_asset/" + page +  "_de.html");
		
		Dialog dialog = new Dialog(walletActivity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(webView);
		dialog.setCanceledOnTouchOutside(true);
		
		return dialog;
	}
	
	public static void page(final FragmentManager fragmentManager, final String page) {
		final DialogFragment fragment = InfoDialogFragment.getInstance(page);
		fragment.show(fragmentManager, FRAGMENT_TAG);
	}
	
	public static InfoDialogFragment getInstance(final String page) {
		InfoDialogFragment fragment = new InfoDialogFragment();
		final Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_PAGE, page);
		return fragment;
	}
}