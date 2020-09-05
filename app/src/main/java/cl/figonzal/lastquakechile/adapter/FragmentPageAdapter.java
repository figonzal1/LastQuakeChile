package cl.figonzal.lastquakechile.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.views.fragments.MapFragment;
import cl.figonzal.lastquakechile.views.fragments.QuakeFragment;
import cl.figonzal.lastquakechile.views.fragments.ReportsFragment;


public class FragmentPageAdapter extends FragmentPagerAdapter {

    private final String[] mTabTitles = new String[3];

    public FragmentPageAdapter(FragmentManager fm, Context context) {

        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        mTabTitles[0] = context.getString(R.string.tab_list);
        mTabTitles[1] = context.getString(R.string.tab_reports);
        mTabTitles[2] = context.getString(R.string.tab_map);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment f = new Fragment();

        switch (position) {

            case 0:
                f = QuakeFragment.newInstance();
                break;

            case 1:
                f = ReportsFragment.newInstance();
                break;

            case 2:
                f = MapFragment.newInstance();
                break;
        }
        return f;
    }

    @Override
    public int getCount() {
        return mTabTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }

}
