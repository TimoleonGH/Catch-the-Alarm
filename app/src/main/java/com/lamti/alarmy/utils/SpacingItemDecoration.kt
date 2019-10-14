package com.lamti.alarmy.utils

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.lamti.alarmy.R


class SpacingItemDecoration (private val context: Context, private val padding: Int): RecyclerView.ItemDecoration() {

    private var mDivider: Drawable? = null

    init {
        mDivider = ContextCompat.getDrawable(context, R.drawable.divider)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
//        outRect.top = padding
        outRect.left = padding
        outRect.right = padding
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)

        val left = parent.paddingLeft + padding
        val right = parent.width - parent.paddingRight - padding

        val childCount = parent.childCount
        for (i in 1 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + (mDivider?.intrinsicHeight ?: 0)

            mDivider?.let {
                it.setBounds(left, top, right, bottom)
                it.draw(canvas)
            }

        }
    }
}