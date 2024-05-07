package org.cxct.sportlottery.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.view.EmptyView
import java.lang.reflect.ParameterizedType

abstract class BindingAdapter<T, VB : ViewBinding> (data: MutableList<T>? = null) :
    BaseQuickAdapter<T, BindingVH<VB>>(0, data) {

    constructor(mContext: Context): this() {
        enableDefaultEmptyView(mContext)
    }

    var _emptyView: EmptyView? = null


    fun enableDefaultEmptyView(context: Context,
                               imgResId: Int = R.drawable.ic_no_data_img,
                               text: Int = R.string.finance_list_no_record): EmptyView {
        _emptyView = EmptyView(context).apply {
            if (imgResId > 0) {
                setEmptyImg(imgResId)
            }
            setEmptyText(context.getString(text))
        }

        setEmptyView(_emptyView!!)
        return _emptyView!!
    }

    fun setEmptyImg(imgResId: Int) = _emptyView?.setEmptyImg(imgResId)

    fun setEmptyText(text: String) = _emptyView?.setEmptyText(text)

    fun positionOf(bean: T): Int {
        if (data.isNullOrEmpty()) {
            return -1
        }
        return data.indexOf(bean)
    }

    fun removeItem(bean: T) {
        val position = positionOf(bean)
        if (position >= 0) {
            removeAt(position)
        }
    }

    fun dataCount() = getDefItemCount()

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BindingVH<VB> {
        val vbClass: Class<VB> = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VB>
        val inflate = vbClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java)
        return BindingVH.of(inflate.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB)
    }

    override fun convert(helper: BindingVH<VB>, item: T) {
        onBinding(positionOf(item), helper.vb, item)
    }

    override fun convert(holder: BindingVH<VB>, item: T, payloads: List<Any>) {
        onBinding(positionOf(item), holder.vb, item, payloads)
    }

    abstract fun onBinding(position: Int, binding: VB, item: T)

    open fun onBinding(position: Int, binding: VB, item: T, payloads: List<Any>) { }
}

class BindingVH<VB : ViewBinding> (view: View) : BaseViewHolder(view) {
    lateinit var vb: VB
    private set

    companion object {

        fun <VB : ViewBinding> of(binding: VB): BindingVH<VB> {
            val vh = BindingVH<VB>(binding.root)
            vh.vb = binding
            return vh
        }
    }
}
