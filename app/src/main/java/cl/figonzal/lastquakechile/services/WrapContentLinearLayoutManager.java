package cl.figonzal.lastquakechile.services;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;

public class WrapContentLinearLayoutManager extends LinearLayoutManager {

	public WrapContentLinearLayoutManager(Context context) {
		super(context);
	}

	@Override
	public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

		try {
			super.onLayoutChildren(recycler, state);
		} catch (IndexOutOfBoundsException e) {
			Log.e("RECYCLER VIEW", "OUT INDEX BUG");
			Crashlytics.log(Log.DEBUG, "RECYCLER VIEW", "OUT INDEX BUG");
		}
	}
}
