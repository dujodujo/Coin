package si.a.coin;

import java.util.ArrayList;
import java.util.List;

import si.a.fragment.SendingAddressesFragment;
import si.a.fragment.WalletAddressesFragment;
import si.a.util.Constants;
import si.a.wiew.ViewPagerTabs;
import si.a.coin.app.R;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Transaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;

public class AddressBookActivity extends AbstractWalletActivity {
	
	private static final String TAG = AddressBookActivity.class.getName();
	private static final String EXTRA_SENDING = "sending";
	
	private WalletAddressesFragment walletAddressesFragment;
	private SendingAddressesFragment sendingAddressesFragment;
	
	public static void start(Context context, boolean sending) {
		Intent intent = new Intent(context, AddressBookActivity.class);
		intent.putExtra(EXTRA_SENDING, sending);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.address_book_onepane);

		final ViewPager pager = (ViewPager) findViewById(R.id.address_book_pager);
		final FragmentManager fragmentManager = getSupportFragmentManager();
		
		if(pager != null) {
			Log.i(TAG, "pager null");
			ViewPagerTabs pagerTabs = (ViewPagerTabs) findViewById(R.id.address_book_pager_tabs);
			pagerTabs.addTabHeaders(R.string.address_book_list_receiving_title, 
				R.string.address_book_list_sending_title);

			sendingAddressesFragment = new SendingAddressesFragment();
			walletAddressesFragment = new WalletAddressesFragment();

			StateAdapter pagerAdapter = new StateAdapter(fragmentManager);
			pager.setAdapter(pagerAdapter);
			pager.setOnPageChangeListener(pagerTabs);

			int position = 0;
			pager.setCurrentItem(position);
			pager.setPageMarginDrawable(R.color.bg_less);
			
			pagerTabs.onPageSelected(position);
			pagerTabs.onPageScrolled(position, 0, 0);
		} else {
			walletAddressesFragment = (WalletAddressesFragment)fragmentManager.findFragmentById(R.id.wallet_addresses_fragment);
			sendingAddressesFragment = (SendingAddressesFragment)fragmentManager.findFragmentById(R.id.sending_addresses_fragment);
		}
		updateFragment();
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
	
	public void updateFragment() {
		Log.i(TAG, "updateFragment");
		final List<ECKey> keys = (List<ECKey>) getWalletApplication().getWallet().getKeys();
		final ArrayList<Address> addresses = new ArrayList<Address>(keys.size());
		
		Log.i(TAG, keys.size() + " key size");
		for(final ECKey key : keys) {
			final Address address = key.toAddress(Constants.NETWORK_PARAMETERS);
			addresses.add(address);
		}
		sendingAddressesFragment.setWalletAddresses(addresses);
	}
	
	@Override
	public void proccessDirectTransaction(Transaction tx) {}
	
	private class StateAdapter extends FragmentStatePagerAdapter {

		public StateAdapter(final FragmentManager fm) { 
			super(fm); 
		}

		@Override
		public Fragment getItem(int position) {
			return walletAddressesFragment;
		}

		@Override
		public int getCount() { 
			return 1; 
		}
	}
}