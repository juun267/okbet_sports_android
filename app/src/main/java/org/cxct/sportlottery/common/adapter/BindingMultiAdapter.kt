package org.cxct.sportlottery.common.adapter

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.containsKey
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BindingMutilAdapter<T : Any>  : BindingAdapter<T, ViewBinding>() {

    private val typeViewHolders = SparseArray<OnMultiItemAdapterListener<T,ViewBinding>>()
    private lateinit var defaultViewHolder: OnMultiItemAdapterListener<T,ViewBinding>
    init {
        initItemType()
    }

    override fun getDefItemViewType(position: Int): Int {
        return onItemType(position)
    }
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BindingVH<ViewBinding> {
        return getViewHolderByViewType(viewType).onCreate(parent)
    }
    override fun convert(helper: BindingVH<ViewBinding>, item: T) {
        onBinding(positionOf(item), helper.vb, item)
    }
    override fun convert(holder: BindingVH<ViewBinding>, item: T, payloads: List<Any>) {
        onBinding(positionOf(item), holder.vb, item, payloads)
    }
    override fun onBinding(position: Int, binding: ViewBinding, item: T) {
        val viewType = onItemType(position)
        getViewHolderByViewType(viewType).onBinding(position, binding, item)
    }
    override fun onBinding(position: Int, binding: ViewBinding, item: T, payloads: List<Any>) {
        val viewType = onItemType(position)
        getViewHolderByViewType(viewType).onBinding(position, binding, item, payloads)
    }
    abstract fun initItemType()
    abstract fun onItemType(position: Int):Int

    fun <VB:ViewBinding> setDefaultItemType(listener: OnMultiItemAdapterListener<T, VB>
    )  {
        defaultViewHolder = listener as OnMultiItemAdapterListener<T,ViewBinding>
    }
    fun <VB:ViewBinding> addItemType(
        itemViewType: Int, listener: OnMultiItemAdapterListener<T, VB>
    )  {
        typeViewHolders.put(itemViewType, listener as OnMultiItemAdapterListener<T,ViewBinding>)
    }
    fun <VB:ViewBinding> addItemTypes(
        itemViewTypes: List<Int>, listener: OnMultiItemAdapterListener<T, VB>
    )  {
        itemViewTypes.forEach {
            typeViewHolders.put(it, listener as OnMultiItemAdapterListener<T,ViewBinding>)
        }
    }
    private fun getViewHolderByViewType(viewType: Int):OnMultiItemAdapterListener<T,ViewBinding>{
        return if (typeViewHolders.containsKey(viewType)){
            typeViewHolders[viewType]
        }else{
            if (defaultViewHolder==null){
                throw Exception("don't have defaultViewHolder")
            }
            defaultViewHolder
        }
    }

abstract class OnMultiItemAdapterListener<T, VB : ViewBinding> {
    fun onCreate(parent: ViewGroup): BindingVH<VB>{
        val vbClass: Class<VB> = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VB>
        val inflate = vbClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java)
        return BindingVH.of(inflate.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB)
    }
     fun onBinding(position: Int, binding: VB, item: T, payloads: List<Any>) { }
     abstract fun onBinding(position: Int, binding: VB, item: T)
   }
}





