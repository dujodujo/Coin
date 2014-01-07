package si.a.coin;

import si.a.coin.app.R;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.bitcoin.core.Transaction;

public class ExchangeRatesActivity extends AbstractWalletActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.exchange_rates_content);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void proccessDirectTransaction(Transaction tx) {}
}