package org.cxct.sportlottery.view.dialog

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OVER_SCROLL_ALWAYS
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.drake.spannable.addSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.ColorSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.network.news.News
import org.cxct.sportlottery.ui.base.BaseDialogFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.view.dialog.queue.BasePriorityDialog
import org.cxct.sportlottery.view.dialog.queue.PriorityDialog

class AnnouncementsDialog: BaseDialogFragment() {

    private val adapter = AnnouncementsAdapter()
    private var titleColor: Int = 0
    private var messageColor: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val ctx = requireContext()
        titleColor = ctx.getColor(R.color.color_0D2245)
        messageColor = ctx.getColor(R.color.color_6D7693)

        val rootView = LinearLayout(ctx)
        rootView.setBackgroundResource(R.drawable.bg_dialog_announcements)
        rootView.orientation = LinearLayout.VERTICAL
        rootView.layoutParams = LinearLayout.LayoutParams(330.dp, 432.dp)

        val titleLayout = FrameLayout(ctx)
        rootView.addView(titleLayout, LinearLayout.LayoutParams(-1, 48.dp))

        val title = AppCompatTextView(ctx)
        title.setTextColor(Color.WHITE)
        title.textSize = 18f
        title.typeface = Typeface.DEFAULT_BOLD
        title.gravity = Gravity.CENTER
        title.setText(R.string.P286)
        titleLayout.addView(title, FrameLayout.LayoutParams(-1, -1))

        val ivClose = AppCompatImageView(ctx)
        ivClose.setImageResource(R.drawable.ic_close_white_2)
        ivClose.setOnClickListener { dismiss() }
        12.dp.let { ivClose.setPadding(it, it, it, it) }
        val ivCloseParams = 44.dp.let { FrameLayout.LayoutParams(it, it) }
        ivCloseParams.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        titleLayout.addView(ivClose, ivCloseParams)

        val recyclerView = RecyclerView(ctx)
        recyclerView.setLinearLayoutManager()
        recyclerView.setPadding(0, 0, 0, 8.dp)
        recyclerView.clipToPadding = false
        recyclerView.adapter = adapter
        recyclerView.isScrollbarFadingEnabled = false
        recyclerView.isVerticalScrollBarEnabled = true
        recyclerView.overScrollMode = OVER_SCROLL_ALWAYS
        recyclerView.setWillNotDraw(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            recyclerView.verticalScrollbarThumbDrawable = ShapeDrawable()
                .setHeight(20.dp)
                .setWidth(4.dp)
                .setSolidColor(Color.parseColor("#C1CEE1"))
        }

        rootView.addView(recyclerView, LinearLayout.LayoutParams(-1, 384.dp))

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(null)
        dialog?.setCanceledOnTouchOutside(false)
        adapter.setNewInstance(arguments?.getParcelableArrayList("newsList"))
    }


    private inner class AnnouncementsAdapter: BaseQuickAdapter<News, BaseViewHolder>(0) {

        override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val textView = AppCompatTextView(parent.context)
            textView.textSize = 12f
            val dp10 = 12.dp
            val params = LinearLayout.LayoutParams(-1, -2)
            params.leftMargin = dp10
            params.topMargin = dp10
            params.rightMargin = dp10
            textView.layoutParams = params
            textView.setPadding(dp10, dp10, dp10, dp10)
            textView.setLineSpacing(2.dp.toFloat(), 1f)
            textView.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.white)
            return BaseViewHolder(textView)
        }

        override fun convert(holder: BaseViewHolder, item: News) {
            val textView = holder.itemView as TextView
            textView.text = "${item.title}: "
                .setSpan(listOf(ColorSpan(titleColor), StyleSpan(Typeface.BOLD)))
                .addSpan(item.message, ColorSpan(messageColor))
        }

    }


    companion object {

        fun createAnnouncements(fm: FragmentManager, newsList: ArrayList<News>): AnnouncementsDialog? {
            if (newsList.isEmpty()) {
                return null
            }
            val dialog = AnnouncementsDialog()
            val bundle = Bundle()
            bundle.putParcelableArrayList("newsList", newsList)
            dialog.arguments = bundle
            return dialog
        }

        fun buildAnnouncementsDialog(newsList: ArrayList<News>, priority: Int, fm: () -> FragmentManager): PriorityDialog? {
            if (newsList.isEmpty()) {
                return null
            }

            return object : BasePriorityDialog<AnnouncementsDialog>() {
                override fun createDialog() = createAnnouncements(fm(), newsList)!!
                override fun getFragmentManager() = fm.invoke()
                override fun priority() = priority
            }
        }

    }


}