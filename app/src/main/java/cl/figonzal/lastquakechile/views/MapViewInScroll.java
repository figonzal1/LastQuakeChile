package cl.figonzal.lastquakechile.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

public class MapViewInScroll extends MapView {
    public MapViewInScroll(Context context) {
        super(context);
    }

    public MapViewInScroll(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MapViewInScroll(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public MapViewInScroll(Context context, GoogleMapOptions googleMapOptions) {
        super(context, googleMapOptions);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
