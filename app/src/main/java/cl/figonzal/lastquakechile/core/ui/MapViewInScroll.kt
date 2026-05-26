package cl.figonzal.lastquakechile.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.gms.maps.MapView


/**
 * This class correct the problem with scroll viewpager2 with GoogleMap
 */
class MapViewInScroll : MapView {
    constructor(context: Context?) : super(requireNotNull(context))
    constructor(context: Context?, attributeSet: AttributeSet?) : super(requireNotNull(context), attributeSet)
    constructor(context: Context?, attributeSet: AttributeSet?, i: Int) : super(requireNotNull(context), requireNotNull(attributeSet), i)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
}
