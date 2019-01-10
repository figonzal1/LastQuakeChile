package cl.figonzal.lastquakechile;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by figon on 28-12-2016.
 */


public class FragmentPageAdapter extends FragmentPagerAdapter {

    private final String[] tabTitles = {"GUC"};


    FragmentPageAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int position) {
        Fragment f =null;

        switch (position){
            case 0:
                f= QuakeFragment.newInstance();
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
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return tabTitles[position];
    }

}
