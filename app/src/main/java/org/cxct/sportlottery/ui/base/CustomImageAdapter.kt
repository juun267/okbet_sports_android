package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R

/***
 * Created by Dean 2020/09/22
 * 選項包含圖片的 Spinner
 * @param listData:spinner item 要顯示的圖片ID及名稱
 */

class CustomImageAdapter(
    val context: Context?,
    listData: MutableList<SelectBank>
) : BaseAdapter() {
    private var mContext = context
    private var mBankListData: MutableList<SelectBank> = listData

    var mCurrentSelectIndex: Int = -1

    data class SelectBank(var bankName: String?, var bankIcon: Int?)

    override fun getItem(position: Int): Any {
        return SelectBank(mBankListData[position].bankName, mBankListData[position].bankIcon)
    }

    override fun getItemId(position: Int): Long {
        return -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (convertView == null) {
            val viewHolder = ViewHolder()
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.content_spinner_image_item, parent, false)

            viewHolder.ivBankIcon = view?.findViewById(R.id.ic_bank)
            viewHolder.tvBankName = view?.findViewById(R.id.tv_bank_name)

            viewHolder.ivBankIcon?.visibility =
                if (mBankListData[position].bankIcon == R.drawable.bg_transparent)
                    View.GONE
                else {
                    mBankListData[position].bankIcon?.let {
                        viewHolder.ivBankIcon?.setImageResource(
                            it
                        )
                    }
                    View.VISIBLE
                }
            viewHolder.tvBankName?.text = mBankListData[position].bankName

            if (position == mCurrentSelectIndex) {
                viewHolder.tvBankName?.setTextColor(
                    ContextCompat.getColor(
                        MultiLanguagesApplication.appContext,
                        R.color.textColorDark
                    )
                )
            }
            return view
        }
        return convertView
    }

    override fun getCount(): Int {
        return mBankListData.size
    }

    class ViewHolder {
        var ivBankIcon: ImageView? = null
        var tvBankName: TextView? = null
    }
}