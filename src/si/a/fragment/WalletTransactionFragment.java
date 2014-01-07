package si.a.fragment;

import si.a.fragment.TransactionListFragment.Direction;
import si.a.coin.app.R;
import si.a.wiew.ViewPagerTabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WalletTransactionFragment extends Fragment {
	
	public static final int STARTING_PAGE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		final View view = inflater.inflate(R.layout.wallet_transactins_fragment, container, false);
		final ViewPagerTabs pagerTabs = (ViewPagerTabs) view.findViewById(R.id.transactions_pager_tabs);
		//pagerTabs.addTabHeaders(R.array.transactions_pager_tabs);
		
		final StatePagerAdapter pagerAdapter = new StatePagerAdapter(getFragmentManager());
		ViewPager viewPager = (ViewPager) view.findViewById(R.id.transactions_pager);
		viewPager.setAdapter(pagerAdapter);
		//viewPager.setOnPageChangeListener(pagerTabs);
		viewPager.setCurrentItem(STARTING_PAGE);
		//pagerTabs.onPageSelected(STARTING_PAGE);
		//pagerTabs.onPageScrolled(STARTING_PAGE, 0, 0);
		
		return view;
	}
	
	private static class StatePagerAdapter extends FragmentStatePagerAdapter {
		
		public static final String TAG = StatePagerAdapter.class.getName();

		public StatePagerAdapter(FragmentManager fragmentManger) {
			super(fragmentManger);
		}

		@Override
		public Fragment getItem(int position) {
			Log.i(TAG, "getItem");
			Direction direction = null;
			/*
			if(position == 0)
				direction = Direction.RECEIVED;
			else if(position == 1)
				direction = null;
			else if(position == 2)
				direction = Direction.SENT;
			*/
			return TransactionListFragment.getInstance(direction);
		}

		@Override
		public int getCount() { return 3; }
	}
}
