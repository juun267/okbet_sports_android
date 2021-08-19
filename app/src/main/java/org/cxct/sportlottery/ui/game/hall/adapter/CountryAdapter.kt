package org.cxct.sportlottery.ui.game.hall.adapter

import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import kotlinx.android.synthetic.main.itemview_country.view.*
import kotlinx.android.synthetic.main.itemview_country_v4.view.*
import kotlinx.android.synthetic.main.itemview_country_v4.view.country_border
import kotlinx.android.synthetic.main.itemview_country_v4.view.country_img
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.league.Row
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.component.VectorDrawableCreator
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.DisplayUtil.px

class CountryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM_PIN, ITEM, NO_DATA
    }

    var data = listOf<Row>()
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    var datePin = listOf<League>()
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    var searchText = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var countryLeagueListener: CountryLeagueListener? = null

    override fun getItemViewType(position: Int): Int {
        return when {
            data.isEmpty() -> ItemType.NO_DATA.ordinal
            (position == 0) -> ItemType.ITEM_PIN.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM_PIN.ordinal -> {
                ItemViewHolderPin.from(parent).apply {
                    this.itemView.country_league_list.apply {
                        this.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                    }
                }
            }
            ItemType.ITEM.ordinal -> {
                ItemViewHolder.from(parent).apply {
                    this.itemView.country_league_list.apply {
                        this.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                    }
                }
            }
            else -> {
                NoDataViewHolder.from(parent, searchText)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolderPin -> {
                holder.bind(datePin, countryLeagueListener)
            }
            is ItemViewHolder -> {
                val item = data[position - 1]

                holder.bind(item, countryLeagueListener)
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        1
    } else {
        data.size + 1
    }

    class ItemViewHolderPin private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            CountryLeagueAdapter()
        }

        fun bind(leagueList: List<League>, countryLeagueListener: CountryLeagueListener?) {
            itemView.country_border.visibility = View.GONE
            itemView.country_text.visibility = View.GONE
            itemView.country_img.visibility = View.GONE
            itemView.country_expand.setExpanded(true, false)

            setupLeagueList(leagueList, countryLeagueListener)
        }

        private fun setupLeagueList(
            leagueList: List<League>,
            countryLeagueListener: CountryLeagueListener?
        ) {
            itemView.country_league_list.apply {
                adapter = countryLeagueAdapter.apply {
                    this.countryLeagueListener = countryLeagueListener

                    data = leagueList
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolderPin {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_country_v4, parent, false)

                return ItemViewHolderPin(view)
            }
        }
    }

    class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            CountryLeagueAdapter()
        }

        fun bind(item: Row, countryLeagueListener: CountryLeagueListener?) {
            itemView.apply {
                country_text.text = item.name

                val data = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\">" +
                        "${item.icon}" +
                        "</svg>"

                Log.e(">>>", "data = $data")
                country_webview.loadDataWithBaseURL(null, data,"text/html", "UTF-8",null)
                country_webview.setBackgroundColor(Color.TRANSPARENT)
//                val uri = Uri.parse("${Constants.getBaseUrl().substring(0, Constants.getBaseUrl().length - 1)}${item.icon}")
                /*
                val paths = mutableListOf<VectorDrawableCreator.PathData>()
                if (item.icon.startsWith("<g")) {
                    Log.e(">>>", "item.icon = ${item.icon}")
                    val pathList = item.icon.split("<path d=\"")
                    pathList.forEach {
                        val path = it.substringBefore("\"></path>")
                        Log.e(">>>", "it = $path")
//                        paths.add(VectorDrawableCreator.PathData(it))
                    }
                }
//                VectorDrawableCreator.getVectorDrawable(this.context, 24, 24, 24, 24, paths)
                val uri = Uri.parse("${item.icon}")
                GlideToVectorYou.init().with(this.context)
                    .setPlaceHolder(R.drawable.ic_image_load, R.drawable.ic_earth)
                    .load(uri, country_img)
*/



            }
            setupLeagueList(item, countryLeagueListener)
            setupCountryExpand(item)
        }

        private fun setupLeagueList(item: Row, countryLeagueListener: CountryLeagueListener?) {
            itemView.country_league_list.apply {
                adapter = countryLeagueAdapter.apply {
                    this.countryLeagueListener = countryLeagueListener

                    data = if (item.searchList.isNotEmpty()) {
                        item.searchList
                    } else {
                        item.list
                    }
                }
            }
        }

        private fun setupCountryExpand(item: Row) {
            itemView.country_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.country_expand.setExpanded(item.isExpand, true)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_country_v4, parent, false)

                return ItemViewHolder(view)
            }
        }
    }

    class NoDataViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup, searchText: String): NoDataViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val noDataLayoutId = if (searchText.isBlank())
                    R.layout.view_no_record
                else
                    R.layout.itemview_game_no_record
                val view = layoutInflater
                    .inflate(noDataLayoutId, parent, false)

                return NoDataViewHolder(view)
            }
        }
    }
}