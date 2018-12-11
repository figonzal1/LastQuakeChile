package cl.figonzal.lastquakechile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by figon on 28-12-2016.
 */

class FragmentPageAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT=1;
    private String tabTitles[]= {"GUC","GUC2"};


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
            case 1:
                f= QuakeFragment.newInstance();
                break;
        }
        return f;
    }

    @Override
    public int getItemPosition(Object obj){
        return POSITION_NONE;
    }
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return tabTitles[position];
    }

}
