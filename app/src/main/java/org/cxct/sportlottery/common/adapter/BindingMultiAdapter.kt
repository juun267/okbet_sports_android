package org.cxct.sportlottery.common.adapter

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.view.EmptyView

abstract class BindingMutilAdapter<T : MultiItemEntity> (data: MutableList<T>? = null) :
    BaseQuickAdapter<T, BindingMultiVH<ViewBinding>>(0, data) {

    private val layouts by lazy(LazyThreadSafetyMode.NONE) { SparseArray<Class<ViewBinding>>() }
    
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
    
    override fun getDefItemViewType(position: Int): Int {
        return data[position].itemType
    }
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BindingMultiVH<ViewBinding> {
        val vbClass = layouts.get(viewType)
        val inflate = vbClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java)
        return BindingMultiVH.of(inflate.invoke(null, LayoutInflater.from(parent.context), parent, false) as ViewBinding)
    }

    override fun convert(helper: BindingMultiVH<ViewBinding>, item: T) {
        onBinding(positionOf(item), helper.vb, item)
    }

    override fun convert(holder: BindingMultiVH<ViewBinding>, item: T, payloads: List<Any>) {
        onBinding(positionOf(item), holder.vb, item, payloads)
    }

    abstract fun onBinding(position: Int, binding: ViewBinding, item: T)

    open fun onBinding(position: Int, binding: ViewBinding, item: T, payloads: List<Any>) { }

    /**
     * 调用此方法，设置多布局
     * @param type Int
     * @param layoutResId Int
     */
    protected fun <VB : ViewBinding> addItemType(type: Int,vb: VB) {
        layouts.put(type, vb.javaClass)
    }
}

class BindingMultiVH<VB : ViewBinding>(view: View) : BaseViewHolder(view) {
    lateinit var vb: VB
    private set

    companion object {
        fun <VB : ViewBinding> of(binding: VB): BindingMultiVH<VB> {
            val vh = BindingMultiVH<VB>(binding.root)
            vh.vb = binding
            return vh
        }
    }

}


