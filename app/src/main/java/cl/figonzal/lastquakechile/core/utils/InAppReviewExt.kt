package cl.figonzal.lastquakechile.core.utils

import android.app.Activity
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import timber.log.Timber

fun Activity.getReviewInfo(reviewManager: ReviewManager): ReviewInfo? {

    var reviewInfo: ReviewInfo? = null

    val manager = reviewManager.requestReviewFlow()

    manager.addOnCompleteListener { task: Task<ReviewInfo?> ->
        when {
            task.isSuccessful -> reviewInfo = task.result
            else -> {
                Toast.makeText(
                    this,
                    "In App ReviewFlow failed to start",
                    Toast.LENGTH_LONG
                ).show()

                Timber.e("GetReviewInfoError: ${task.exception?.message}")
            }
        }
    }

    return reviewInfo
}

fun Activity.startReviewFlow(reviewManager: ReviewManager, reviewInfo: ReviewInfo?) {

    reviewInfo?.let {
        with(reviewManager.launchReviewFlow(this, it)) {
            addOnCompleteListener {
                Toast.makeText(this@startReviewFlow, "Rating complete", Toast.LENGTH_LONG).show()
            }
            addOnFailureListener {
                Toast.makeText(this@startReviewFlow, "Rating failed", Toast.LENGTH_LONG).show()
            }
        }
    }

}
