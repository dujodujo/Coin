package si.a.coin;

import com.google.bitcoin.core.Transaction;

import si.a.util.Constants;
import si.a.application.WalletApplication;
import si.a.coin.app.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public abstract class AbstractWalletActivity extends FragmentActivity {
	
	private WalletApplication walletApplication;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		walletApplication = (WalletApplication) getApplication();		
		super.onCreate(savedInstanceState);
	}
	
	public abstract void proccessDirectTransaction(Transaction tx);
	
	public WalletApplication getWalletApplication() {
		return walletApplication;
	}
	
	public final void shortToast(String text, final Object... formatArgs) {
		toast(text, 0, Toast.LENGTH_SHORT, formatArgs);
	}
	
	public final void longToast(String text, final Object... formatArgs) {
		toast(text, 0, Toast.LENGTH_LONG, formatArgs);
	}
	
	public final void toast(final String text, final int imageResId, final int duration, final Object... formatArgs) {
		final View view = getLayoutInflater().inflate(R.layout.notification, null);
		TextView tv = (TextView) view.findViewById(R.id.transient_notification_text);
		tv.setText(String.format(text, formatArgs));
		tv.setCompoundDrawablesWithIntrinsicBounds(imageResId, 0, 0, 0);
		
		final Toast t = new Toast(this);
		t.setView(view);
		t.setDuration(duration);
		t.show();
	}
}