package com.hendraanggrian.widget

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.hendraanggrian.reveallayout.RevealableLayout
import com.hendraanggrian.reveallayout.RevealableLayoutImpl

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class RevealFrameLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0) : io.codetail.widget.RevealFrameLayout(context, attrs, defStyle), RevealableLayout {

    private val impl = RevealableLayoutImpl(context, attrs)

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        impl.addView(child)
    }

    override fun revealTo(source: View, target: View, reverse: Boolean): Collection<Animator> = impl.revealTo(source, target, reverse)

    override fun reveal(view: View, startX: Int, startY: Int, reverse: Boolean): Animator = impl.reveal(view, startX, startY, reverse)
}