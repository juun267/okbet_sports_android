package org.cxct.sportlottery.ui.main.next

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_dz.*
import org.cxct.sportlottery.R

class DZFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dz, container, false)
    }

    private val tabIconList by lazy {
        listOf(R.drawable.ic_image_load, R.drawable.ic_cp, R.drawable.ic_live, R.drawable.ic_qp, R.drawable.ic_dz, R.drawable.ic_by)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view_pager.adapter = DzVpAdapter(tabIconList)

        // attaching tab mediator
        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.setCustomView(R.layout.tab_item_game2)
            tab.customView?.apply {
                findViewById<ImageView>(R.id.iv_icon)?.setImageResource(tabIconList[position])
            }
        }.attach()

    }

}

class DzVpAdapter(private val titleList: List<Int>): RecyclerView.Adapter<DzVpAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.view_dz_adapter, viewGroup, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return titleList.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            itemView.apply {

            }
        }

    }

}
