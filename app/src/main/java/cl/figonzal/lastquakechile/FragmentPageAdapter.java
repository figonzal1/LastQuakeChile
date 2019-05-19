package cl.figonzal.lastquakechile;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import cl.figonzal.lastquakechile.views.MapFragment;
import cl.figonzal.lastquakechile.views.QuakeFragment;


public class FragmentPageAdapter extends FragmentPagerAdapter {

	private final String[] mTabTitles = new String[2];


	public FragmentPageAdapter(FragmentManager fm, Context context) {
		super(fm);
		mTabTitles[0] = context.getString(R.string.tab_list);
		mTabTitles[1] = context.getString(R.string.tab_map);
	}

	@Override
	public Fragment getItem(int position) {
		Fragment f = null;

		switch (position) {
			case 0:
				f = QuakeFragment.newInstance();
				break;

			case 1:
				f = MapFragment.newInstance();
				break;
		}
		return f;
	}

	@Override
	public int getItemPosition(@NonNull Object obj) {
		return POSITION_NONE;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mTabTitles[position];
	}

}
