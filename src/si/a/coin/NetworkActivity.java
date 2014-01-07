package si.a.coin;

import si.a.fragment.BlockListFragment;
import si.a.coin.app.R;
import si.a.wiew.ViewPagerTabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;

import com.google.bitcoin.core.Transaction;

public class NetworkActivity extends AbstractWalletActivity {	
	public static final String TAG = NetworkActivity.class.getName();
	private BlockListFragment blockListFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		
		setContentView(R.layout.network_content);
		
		ViewPager pager = (ViewPager) findViewById(R.id.network_pager);
		FragmentManager fragmentManager = getSupportFragmentManager();

		if(pager != null) {
			ViewPagerTabs pagerTabs = (ViewPagerTabs) findViewById(R.id.network_pager_tabs);
			pagerTabs.addTabHeaders(R.string.network_block_list_title);

			blockListFragment = new BlockListFragment();

			final StatePagerAdapter pagerAdapter = new StatePagerAdapter(fragmentManager);
			pager.setAdapter(pagerAdapter);
			pager.setOnPageChangeListener(pagerTabs);
			
			int position = 0;
			pager.setCurrentItem(position);
			pager.setPageMarginDrawable(R.color.bg_less);
			
			pagerTabs.onPageSelected(position);
			pagerTabs.onPageScrolled(position, 0, 0);
			
		} else {
			blockListFragment = (BlockListFragment) fragmentManager.findFragmentById(R.id.block_list_fragment);
		}
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
	
	private class StatePagerAdapter extends FragmentStatePagerAdapter {

		public StatePagerAdapter(FragmentManager fm) { super(fm); }

		@Override
		public Fragment getItem(int position) { 
			return blockListFragment; 
		}

		@Override
		public int getCount() { return 1; }
	}
}
