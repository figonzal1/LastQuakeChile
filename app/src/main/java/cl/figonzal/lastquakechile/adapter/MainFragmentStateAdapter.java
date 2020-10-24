package cl.figonzal.lastquakechile.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.views.fragments.MapFragment;
import cl.figonzal.lastquakechile.views.fragments.QuakeFragment;
import cl.figonzal.lastquakechile.views.fragments.ReportsFragment;


public class MainFragmentStateAdapter extends FragmentStateAdapter {

    private static final String[] tabs = new String[3];

    public MainFragmentStateAdapter(@NonNull FragmentActivity fa, @NonNull Context context) {
        super(fa);

        tabs[0] = context.getString(R.string.tab_list);
        tabs[1] = context.getString(R.string.tab_reports);
        tabs[2] = context.getString(R.string.tab_map);
    }

    @NonNull
    public static String[] getTabs() {
        return tabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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
    public int getItemCount() {
        return tabs.length;
    }
}
