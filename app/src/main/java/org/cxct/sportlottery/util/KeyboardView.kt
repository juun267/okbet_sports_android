package org.cxct.sportlottery.util

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.item_number_keyboard_layout.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import java.lang.reflect.Method

class KeyboardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(
        context, attrs, defStyleAttr
) {

    private val view: View by lazy { LayoutInflater.from(context).inflate(R.layout.item_number_keyboard_layout, null, false) }

    /**键盘点击事件*/
    private var numCLick: ((number: String) -> Unit)? = null

    init {
        removeAllViews()
        addView(view, 0)
        initView()
    }

    private fun initView() {
        sConfigData?.presetBetAmount?.let {
            it.forEachIndexed { index, i ->
                if(index == 0){
                    tvPlus1.text = "+ ${it[index]}"
                    tvPlus1.visibility = View.VISIBLE
                    tvPlus1.setOnClickListener { v ->
                        plus(it[index].toLong())
                    }
                }
                if(index == 1){
                    tvPlus2.text = "+ ${it[index]}"
                    tvPlus2.visibility = View.VISIBLE
                    tvPlus2.setOnClickListener { v ->
                        plus(it[index].toLong())
                    }
                }
                if(index == 2){
                    tvPlus3.text = "+ ${it[index]}"
                    tvPlus3.visibility = View.VISIBLE
                    tvPlus3.setOnClickListener { v ->
                        plus(it[index].toLong())
                    }
                }
                if(index == 3){
                    tvPlus4.text = "+ ${it[index]}"
                    tvPlus4.visibility = View.VISIBLE
                    tvPlus4.setOnClickListener { v ->
                        plus(it[index].toLong())
                    }
                }
            }
        }
        tvNum0.setOnClickListener {
            insert(0)
        }
        tvNum1.setOnClickListener {
            insert(1)
        }
        tvNum2.setOnClickListener {
            insert(2)
        }
        tvNum3.setOnClickListener {
            insert(3)
        }
        tvNum4.setOnClickListener {
            insert(4)
        }
        tvNum5.setOnClickListener {
            insert(5)
        }
        tvNum6.setOnClickListener {
            insert(6)
        }
        tvNum7.setOnClickListener {
            insert(7)
        }
        tvNum8.setOnClickListener {
            insert(8)
        }
        tvNum9.setOnClickListener {
            insert(9)
        }
        tvDel.setOnClickListener {
            delete()
        }
        tvDot.setOnClickListener {
            insertDot()
        }
        tvMax.setOnClickListener {
            plusAll(maxBetMoney)
        }
    }

    private fun numberClick(number: String) {
        numCLick?.let { it(number) }
    }

    /**
     * 键盘点击事件
     */
    fun setNumberClick(click: ((number: String) -> Unit)?) {
        this.numCLick = click
    }

    private lateinit var mEditText: EditText
    private var maxBetMoney: String = "0"
    private var isShow = false

    fun showKeyboard(editText: EditText, position: Int?, maxBetMoney: Double, minBetMoney: Long) {
        this.mEditText = editText
        this.maxBetMoney = TextUtil.formatInputMoney(maxBetMoney)
        //InputType.TYPE_NULL 禁止彈出系統鍵盤
        mEditText.apply {
            //inputType = InputType.TYPE_NULL
            isFocusable = true
            isFocusableInTouchMode = true
        }
        disableKeyboard()
        this.visibility = View.VISIBLE
        //parent?.visibility = View.VISIBLE
        isShow = true

        //keyBoardViewListener.showOrHideKeyBoardBackground(true, position)
    }

    fun setMaxBetMoney(maxBetMoney: Double) {
        this.maxBetMoney = TextUtil.formatInputMoney(maxBetMoney)
    }

    private fun disableKeyboard(){
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            mEditText.setInputType(InputType.TYPE_NULL);
        } else {
            var cls:Class<EditText> = EditText::class.java
            var method:Method
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", Boolean::class.java)
                method.setAccessible(true);
                method.invoke(mEditText, false)
            } catch (e: Exception) {//TODO: handle exception
            }
            try {
                method = cls.getMethod("setSoftInputShownOnFocus",Boolean::class.java)
                method.setAccessible(true)
                method.invoke(mEditText, false)
            } catch (e: Exception) {//TODO: handle exception
            }
        }
    }

    private fun plus(count: Long) {
        val input = if (mEditText.text.toString() == "") "0" else mEditText.text.toString()
        val tran = if (input.contains(".")) {
            input.toDouble() + count
        } else input.toDouble() + count
        mEditText.setText(tran.toLong().toString())
        mEditText.setSelection(mEditText.text.length)
    }
    private fun plusAll(all: String) {
        mEditText.setText(all)
        mEditText.setSelection(mEditText.text.length)

    }
    private fun insert(count: Long) {
        val editable = mEditText.text
        val start = mEditText.selectionStart
        editable.insert(start, count.toString())

    }
    private fun insertDot() {
        val editable = mEditText.text
        val start = mEditText.selectionStart
        editable.apply {
            insert(
                start,
                if (isNotEmpty()) {
                    if (!this.toString().contains(".")) "." else return
                } else {
                    "0."
                }
            )
        }
    }

    private fun delete() {
        val editable = mEditText.text
        val start = mEditText.selectionStart
        if (start > 0) {
            editable.delete(start - 1, start)
        }

    }

    fun hideKeyboard() {
        this.visibility = View.GONE
        if (isShow) mEditText.isFocusable = false

        isShow = false
    }

}
