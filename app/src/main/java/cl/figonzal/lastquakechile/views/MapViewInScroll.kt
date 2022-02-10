package cl.figonzal.lastquakechile.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView


/**
 * This class correct the problem with scroll viewpager2 with GoogleMap
 */
class MapViewInScroll : MapView {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attributeSet: AttributeSet?) : super(
        context!!, attributeSet
    )

    constructor(context: Context?, attributeSet: AttributeSet?, i: Int) : super(
        context!!, attributeSet!!, i
    )

    constructor(context: Context?, googleMapOptions: GoogleMapOptions?) : super(
        context!!, googleMapOptions
    )

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
}