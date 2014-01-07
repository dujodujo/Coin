package si.a.coin;

import java.math.BigInteger;

import si.a.fragment.InfoDialogFragment;
import si.a.coin.app.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.bitcoin.core.Transaction;

public class SendCoinsActivity extends AbstractWalletActivity {
	
	public static final String TAG = SendCoinsActivity.class.getName();
	
	public static final String INTENT_ADDRESS = "address";
	public static final String INTENT_ADDRESS_LABEL = "address label";
	public static final String INTENT_AMOUNT = "amount";

	public static void startSendingCoins(Context context, String address, String address_label, BigInteger amount) {
		Log.i(TAG, "onCreate");

		final Intent intent = new Intent(context, SendCoinsActivity.class);
		intent.putExtra(INTENT_ADDRESS, address);
		intent.putExtra(INTENT_ADDRESS_LABEL, address_label);
		intent.putExtra(INTENT_AMOUNT, amount);
		context.startActivity(intent);
	}		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		
		setContentView(R.layout.send_coins_activity);
		//getWalletApplication().startBlockchainService(false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.send_coins_options, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "onCreate");

		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.send_coins_options_help:
				InfoDialogFragment.page(getSupportFragmentManager(), "send coins");
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void proccessDirectTransaction(Transaction tx) {}
}