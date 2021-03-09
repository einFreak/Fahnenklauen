package com.felixwild.fahnenklauen.ui.search

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.R

abstract class SwipeToDeleteHandler(context: Context) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24)!!
    private val swipeBackground: ColorDrawable = ColorDrawable(ContextCompat.getColor(context, R.color.secondaryColor))

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

        val itemView = viewHolder.itemView
        val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

        if (dX > 0) { //swipe to right
            swipeBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
            deleteIcon.setBounds(itemView.left + iconMargin,itemView.top + iconMargin,itemView.left + iconMargin + deleteIcon.intrinsicWidth,itemView.bottom - iconMargin)
        } else if (dX < 0) { //swipe to left
            swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
            deleteIcon.setBounds(itemView.right - iconMargin - deleteIcon.intrinsicWidth,itemView.top + iconMargin,itemView.right - iconMargin,itemView.bottom - iconMargin)
        } else {
            swipeBackground.setBounds(0, 0, 0, 0)
        }


        swipeBackground.draw(c)
        c.save()
        if (dX > 0)
            c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
        else
            c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)


        deleteIcon.draw(c)
        c.restore()


        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        //return 0 for items that should not be deleted
        return super.getMovementFlags(recyclerView, viewHolder)
    }

}