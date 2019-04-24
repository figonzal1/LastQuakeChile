package cl.figonzal.lastquakechile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import cl.figonzal.lastquakechile.views.MapFragment;
import cl.figonzal.lastquakechile.views.QuakeFragment;


public class FragmentPageAdapter extends FragmentPagerAdapter {

	private final String[] mTabTitles = {"Listado", "Mapa"};


	public FragmentPageAdapter (FragmentManager fm) {
		super(fm);

	}

	@Override
	public Fragment getItem (int position) {
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
	public int getItemPosition (@NonNull Object obj) {
		return POSITION_NONE;
	}

	@Override
	public int getCount () {
		return 2;
	}

	@Override
	public CharSequence getPageTitle (int position) {
		return mTabTitles[position];
	}

}
