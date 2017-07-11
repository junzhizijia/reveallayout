package com.hendraanggrian.reveallayout

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import io.codetail.animation.ViewAnimationUtils
import java.util.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class RevealableLayoutImpl(context: Context, attrs: AttributeSet?) : RevealableLayout {

    private val revealId: Int
    private val revealDuration: Int
    private val revealCenter: RevealCenter

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RevealLayout)
        revealId = a.getResourceId(R.styleable.RevealLayout_revealId, -1)
        revealDuration = a.getInt(R.styleable.RevealLayout_revealDuration, -1)
        revealCenter = RevealCenter.valueOf(a.getInt(R.styleable.RevealLayout_revealCenter, RevealCenter.CENTER.attrId))
        a.recycle()
    }

    fun addView(child: View?) {
        child?.let {
            if (it.id == revealId) {
                it.post {
                    reveal(it, revealCenter).apply {
                        if (revealDuration != -1) {
                            duration = revealDuration.toLong()
                        }
                    }.start()
                }
            }
        }
    }

    override fun revealTo(source: View, target: View, reverse: Boolean): Collection<Animator> {
        val animators = ArrayList<Animator>()
        // Cancel all concurrent events on view
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            source.cancelPendingInputEvents()
        }

        // Coordinates of circle initial point
        val parent = target.parent as ViewGroup
        val srcRect = createRect(parent, source)
        val trgRect = createRect(parent, target)

        animators.add(ViewAnimationUtils.createCircularReveal(
                target,
                trgRect.centerX(),
                trgRect.centerY(),
                if (reverse) getEndRadius(target) else (srcRect.width() / 2).toFloat(),
                if (reverse) (srcRect.width() / 2).toFloat() else getEndRadius(target),
                View.LAYER_TYPE_HARDWARE
        ))

        // Put Mask view at circle 8initial points
        //target.setX(srcRect.left - trgRect.centerX());
        //target.setY(srcRect.top - trgRect.centerY());

        val c0X = (srcRect.centerX() - trgRect.centerX()).toFloat()
        val c0Y = (srcRect.centerY() - trgRect.centerY()).toFloat()
        val path = AnimatorPath()
        if (!reverse) {
            path.moveTo(c0X, c0Y)
            path.curveTo(c0X, c0Y, 0f, c0Y, trgRect.left.toFloat(), trgRect.top.toFloat())
        } else {
            path.moveTo(trgRect.left.toFloat(), trgRect.top.toFloat())
            path.curveTo(trgRect.left.toFloat(), trgRect.top.toFloat(), 0f, c0Y, c0X, c0Y)
        }
        animators.add(ObjectAnimator.ofObject(object : OnSetListener {
            override fun setMaskLocation(location: PathPoint) {
                target.x = location.mX
                target.y = location.mY
            }
        }, "maskLocation", PathEvaluator(), *path.points.toTypedArray()))

        return animators
    }

    override fun reveal(view: View, startX: Int, startY: Int, reverse: Boolean): Animator = ViewAnimationUtils.createCircularReveal(
            view,
            startX,
            startY,
            if (reverse) getEndRadius(view) * 2 else 0f,
            if (reverse) 0f else getEndRadius(view) * 2)


    private interface OnSetListener {

        fun setMaskLocation(location: PathPoint)
    }

    private fun createRect(parent: ViewGroup, view: View): Rect {
        val rect = Rect()
        view.getDrawingRect(rect)
        parent.offsetDescendantRectToMyCoords(view, rect)
        return rect
    }

    private fun getEndRadius(view: View): Float {
        val cx = (view.left + view.right) / 2
        val cy = (view.top + view.bottom) / 2
        val dx = Math.max(cx, view.width - cx)
        val dy = Math.max(cy, view.height - cy)
        return Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
    }
}