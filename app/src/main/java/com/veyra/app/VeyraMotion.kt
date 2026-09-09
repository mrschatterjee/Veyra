package com.veyra.app

import android.view.View
import android.view.animation.DecelerateInterpolator

object VeyraMotion {
    fun enter(view: View, distance: Float = 28f) {
        view.alpha = 0f
        view.translationY = distance
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(420L)
            .setInterpolator(DecelerateInterpolator(1.6f))
            .start()
    }

    fun switch(view: View, change: () -> Unit) {
        view.animate()
            .alpha(0.72f)
            .translationY(-6f)
            .setDuration(90L)
            .withEndAction {
                change()
                view.alpha = 0.72f
                view.translationY = 8f
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(260L)
                    .setInterpolator(DecelerateInterpolator(1.4f))
                    .start()
            }
            .start()
    }
}
