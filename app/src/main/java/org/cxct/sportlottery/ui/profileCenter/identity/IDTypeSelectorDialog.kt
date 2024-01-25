package org.cxct.sportlottery.ui.profileCenter.identity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tbruyelle.rxpermissions2.RxPermissions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.DialogIdtypeSelectorBinding
import org.cxct.sportlottery.network.index.config.IdentityType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JsonUtil
import org.cxct.sportlottery.util.RCVDecoration
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter

class IDTypeSelectorDialog(private val context: FragmentActivity, private val lifecycle: Lifecycle): Dialog(context)
    , OnItemClickListener, LifecycleEventObserver {

    private val binding: DialogIdtypeSelectorBinding = DialogIdtypeSelectorBinding.inflate(context.layoutInflater)
    private val idAdapter = IDTypeAdapter().apply { setOnItemClickListener(this@IDTypeSelectorDialog) }

    private val recomdeIDType = mutableListOf<IdentityType>()
    private val othersIDType = mutableListOf<IdentityType>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycle.addObserver(this)
        setOnDismissListener { lifecycle.removeObserver(this) }
        binding.ivClose.setOnClickListener { dismiss() }
        sConfigData?.identityTabTypeList?.forEach {
            if (it.type == 1) {
                recomdeIDType.add(it)
            } else {
                othersIDType.add(it)
            }
        }
        window?.let { w ->
            w.attributes.width = -1
            w.attributes.height = -2
            w.setGravity(Gravity.BOTTOM)
            w.setWindowAnimations(R.style.AnimBottom)

            w.setBackgroundDrawable(DrawableCreatorUtils.getCommonBackgroundStyle(
                leftTopCornerRadius = 16,
                rightTopCornerRadius = 16,
                solidColor = R.color.white,
                strokeWidth = 0
            ))
        }

        binding.rcv.setLinearLayoutManager()
        binding.rcv.addItemDecoration(RCVDecoration()
            .setColor(context.getColor(R.color.color_dbdeeb))
            .setRightMargin(12.dp.toFloat()))
        binding.rcv.adapter = idAdapter
        idAdapter.setNewInstance(recomdeIDType)
        binding.tabNews.addOnTabSelectedListener(TabSelectedAdapter { tab, _ ->
            if (tab.position == 0) {
                idAdapter.setNewInstance(recomdeIDType)
            } else {
                idAdapter.setNewInstance(othersIDType)
            }
        })
    }


    private class IDTypeAdapter: BaseQuickAdapter<IdentityType, BaseViewHolder>(0) {

        val nameId = View.generateViewId()
        override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val lin = LinearLayout(parent.context)
            lin.layoutParams = LinearLayout.LayoutParams(-1, 52.dp)
            lin.gravity = Gravity.CENTER_VERTICAL
            lin.setPadding(21.dp, 0, 18.dp, 0)
            lin.foreground = parent.context.getDrawable(R.drawable.fg_ripple)
            val text = AppCompatTextView(parent.context)
            text.id = nameId
            text.textSize = 16f
            text.setTextColor(parent.context.getColor(R.color.color_414655))
            lin.addView(text, LinearLayout.LayoutParams(0, -2, 1f))

            val iv = ImageView(context)
            val drawable = context.getDrawable(R.drawable.icon_arrow07)!!.mutate()
            drawable.setTint(context.getColor(R.color.color_d6dbe3))
            iv.setImageDrawable(drawable)
            lin.addView(iv, 16.dp.let { LinearLayout.LayoutParams(it, it) })
            return BaseViewHolder(lin)
        }

        override fun convert(holder: BaseViewHolder, item: IdentityType) {
            holder.setText(nameId, item.name)
        }

    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        RxPermissions(context).request(Manifest.permission.CAMERA).subscribe { onNext ->
            if (onNext) {
                val item = idAdapter.getItemOrNull(position) ?: return@subscribe
                TakeIDPhotoActivity.start(context, item.id, item.type, item.name)
            } else {
                ToastUtil.showToast(context, context.getString(R.string.N980))
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            dismiss()
        }
    }
}