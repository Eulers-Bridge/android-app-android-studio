package com.eulersbridge.isegoria.util.extension

/**
 * Created by Yikai Gong on 10/07/19.
 */

import android.content.Context

import android.widget.ArrayAdapter


class HintAdapter<T> : ArrayAdapter<T> {

    constructor(context: Context, resource: Int) : super(context, resource) {}


    override fun getCount(): Int {
        // Don't display last item. It is used as hint.
        val count = super.getCount()
        return if (count > 0) count - 1 else count
    }

//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view = super.getDropDownView(position, convertView, parent)
//        val tv = view as TextView
//        if (position == super.getCount()-1)
//            tv.setTextColor(Color.GRAY)
//        return view
//    }

    override fun isEnabled(position: Int): Boolean {
        return if (position == super.getCount()-1)
            false
        else
            super.isEnabled(position)
    }
}