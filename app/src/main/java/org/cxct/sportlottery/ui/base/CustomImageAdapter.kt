package org.cxct.sportlottery.ui.base

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
 * @param bankIcon:spinner item 要顯示的圖片id
 * @param bankName:spinner item 要顯示的名稱
 */

class CustomImageAdapter (context: Context?, bankIcon: MutableList<Int>, bankName: MutableList<String>) : BaseAdapter() {
    private var mContext = context
    private var mBankIconList: MutableList<Int> = bankIcon
    private var mBankNameList: MutableList<String> = bankName
    var mCurrentSelectIndex: Int = -1

    data class SelectBank(var bankName: String?, var bankIcon: Int?)


    //下拉要顯示顏色變化
    fun setDefaultSelect(selectIndex: Int) {
        mCurrentSelectIndex = selectIndex
    }

    override fun getItem(position: Int): Any? {
        return SelectBank(mBankNameList[position], mBankIconList[position] ?: 0)
    }

    override fun getItemId(position: Int): Long {
        return -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(mContext).inflate(R.layout.content_spinner_image_item, parent, false)
        val ivBankIcon: ImageView? = view?.findViewById(R.id.ic_bank)
        val tvBankName: TextView? = view?.findViewById(R.id.tv_bank_name)
        ivBankIcon?.visibility = if (mBankIconList[position] == R.drawable.bg_transparent)
            View.GONE
        else {
            ivBankIcon?.setImageResource(mBankIconList[position])
            View.VISIBLE
        }
        tvBankName?.text = mBankNameList[position]

        if (position == mCurrentSelectIndex) {
            tvBankName?.setTextColor(ContextCompat.getColor(MultiLanguagesApplication.appContext, R.color.textColorDark))
        }

        return view    }

    override fun getCount(): Int {
        return mBankIconList.size }
}