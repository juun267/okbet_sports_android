package org.cxct.sportlottery.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.widget.EmptyView
import java.lang.reflect.ParameterizedType

abstract class BaseBindingAdapter<T, VB : ViewBinding> (data: MutableList<T>? = null) :
    BaseQuickAdapter<T, VBViewHolder<VB>>(0, data) {

    constructor(mContext: Context): this() {
        enableDefaultEmptyView(mContext)
    }

    var _emptyView: EmptyView? = null


    fun enableDefaultEmptyView(context: Context,
                               imgResId: Int = R.drawable.ic_no_data_img,
                               text: Int = R.string.finance_list_no_record) {
        _emptyView = EmptyView(context).apply {
            if (imgResId > 0) {
                setEmptyImg(imgResId)
            }
            setEmptyText(context.getString(text))
        }

        setEmptyView(_emptyView!!)
    }

    fun setEmptyImg(imgResId: Int) = _emptyView?.setEmptyImg(imgResId)

    fun setEmptyText(text: String) = _emptyView?.setEmptyText(text)

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VBViewHolder<VB> {
        val vbClass: Class<VB> = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VB>
        val inflate = vbClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java)
        return VBViewHolder(inflate.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB)
    }

    override fun convert(helper: VBViewHolder<VB>, item: T) {
        onBinding(helper.vb, item)
    }

    abstract fun onBinding(viewBinding: VB, item: T)
}

class VBViewHolder<VB : ViewBinding>(val vb: VB) : BaseViewHolder(vb.root)
