package org.cxct.sportlottery.ui.viewpager.adapter

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

class FragmentPagerItems(val context: Context): ArrayList<FragmentPagerItem>() {
    class Creator(context: Context) {
        private val items: FragmentPagerItems
        init {
            items = FragmentPagerItems(context)
        }
        fun add(@StringRes title: Int, clazz: Class<out Fragment?>): Creator {
            return add(FragmentPagerItem.of(items.context.getString(title), clazz))
        }

        fun add(@StringRes title: Int, clazz: Class<out Fragment?>, args: Bundle?): Creator {
            return add(FragmentPagerItem.of(items.context.getString(title), clazz, args))
        }

        fun add(@StringRes title: Int, width: Float, clazz: Class<out Fragment?>): Creator {
            return add(FragmentPagerItem.of(items.context.getString(title), width, clazz))
        }

        fun add(
            @StringRes title: Int, width: Float, clazz: Class<out Fragment?>,
            args: Bundle?
        ): Creator {
            return add(FragmentPagerItem.of(items.context.getString(title), width, clazz, args))
        }

        fun add(title: CharSequence, clazz: Class<out Fragment?>): Creator {
            return add(FragmentPagerItem.of(title, clazz))
        }

        fun add(title: CharSequence, clazz: Class<out Fragment?>, args: Bundle?): Creator {
            return add(FragmentPagerItem.of(title, clazz, args))
        }

        fun add(item: FragmentPagerItem): Creator {
            items.add(item)
            return this
        }

        fun create(): FragmentPagerItems {
            return items
        }
    }

    companion object {
        fun with(context: Context): Creator {
            return Creator(context)
        }
    }
}