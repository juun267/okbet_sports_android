package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_number_keyboard_layout2.view.*
import kotlinx.android.synthetic.main.snackbar_login_notify.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import java.lang.reflect.Method

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context, attrs, defStyleAttr
) {

    private val view: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.item_number_keyboard_layout2, null, false)
    }

    /**键盘点击事件*/
    private var numCLick: ((number: String) -> Unit)? = null

    init {
        removeAllViews()
        addView(view, 0)
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        //20220610 預設下注金額, 改為三個按鈕顯示 (順序依照後台設置)
        sConfigData?.presetBetAmount?.let {
            it.forEachIndexed { index, i ->
                if (index == 0) {
                    tvPlus1.text = "+$i"
                    tvPlus1.visibility = View.VISIBLE
                    tvPlus1.setOnClickListener {
                        plus(i.toDouble())
                    }
                }
                if (index == 1) {
                    tvPlus2.text = "+$i"
                    tvPlus2.visibility = View.VISIBLE
                    tvPlus2.setOnClickListener {
                        plus(i.toDouble())
                    }
                }
                if (index == 2) {
                    tvPlus3.text = "+$i"
                    tvPlus3.visibility = View.VISIBLE
                    tvPlus3.setOnClickListener {
                        plus(i.toDouble())
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
        flDot.setOnClickListener {
            insertDot()
        }
//        tvClear.setOnClickListener {
//            //清除
//            mEditText.text.clear()
//        }
//        ivClose.setOnClickListener {
//            //關閉鍵盤
//            hideKeyboard()
//        }
        tvMax.setOnClickListener {
            if (isLogin) {
                plusAll(maxBetMoney)
            } else {
                setSnackBarNotify()
            }
        }
        setOnClickListener { /*这里加个点击事件空实现，为了防止点击到间隔处把键盘消失*/ }
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
    private var mPosition: Int? = 0
    private var isParlay: Boolean = false
    //最大限額
    private var _maxBetMoney: String = "9999999"
    private val maxBetMoney: String
        get() = _maxBetMoney

    private var isShow = false

    //是否登入
    private val isLogin: Boolean
        get() = LoginRepository.isLogin.value == true

    //提示未登入
    private var snackBarNotify: Snackbar? = null

    fun setupMaxBetMoney(max: Double) {
        _maxBetMoney = TextUtil.formatInputMoney(max)
    }

    fun showKeyboard(
        editText: EditText,
        position: Int?,
        isParlay: Boolean = false
    ) {
        this.mEditText = editText
        this.mPosition = position
        this.isParlay = isParlay
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

    //提示未登入
    private fun setSnackBarNotify() {
        val title = context.getString(R.string.login_notify)

        val layout = R.layout.snackbar_login_notify

        snackBarNotify =
            Snackbar.make(
                mEditText,
                title,
                Snackbar.LENGTH_LONG
            ).apply {
                val snackView: View = (context as Activity).layoutInflater.inflate(
                    layout,
                    (context as Activity).findViewById(android.R.id.content),
                    false
                )
                snackView.tvNotify.text = title

                (this.view as Snackbar.SnackbarLayout).apply {
                    findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                        visibility = View.INVISIBLE
                    }
                    background.alpha = 0
                    addView(snackView, 0)
                    setPadding(0, 0, 0, 0)
                }
            }
        snackBarNotify?.show()
    }

    private fun disableKeyboard() {

        var cls: Class<EditText> = EditText::class.java
        var method: Method
        try {
            method = cls.getMethod("setShowSoftInputOnFocus", Boolean::class.java)
            method.setAccessible(true);
            method.invoke(mEditText, false)
        } catch (e: Exception) {//TODO: handle exception
        }
        try {
            method = cls.getMethod("setSoftInputShownOnFocus", Boolean::class.java)
            method.setAccessible(true)
            method.invoke(mEditText, false)
        } catch (e: Exception) {//TODO: handle exception
        }
    }

    private fun plus(count: Double) {
        if (!this::mEditText.isInitialized){
            return
        }
        val input = if (mEditText.text.toString() == "") "0" else mEditText.text.toString()
        val tran = if (input.contains(".")) {
            input.toDouble() + count
        } else input.toLong() + count.toLong()
        mEditText.setText(tran.toString())
        mEditText.setSelection(mEditText.text.length)
    }

    private fun plusAll(all: String) {
        if (!this::mEditText.isInitialized){
            return
        }
        mEditText.setText(all)
        mEditText.setSelection(mEditText.text.length)

    }

    private fun insert(count: Long) {
        if (!this::mEditText.isInitialized){
            return
        }
        val editable = mEditText.text
        val start = mEditText.selectionStart
        editable.insert(start, count.toString())

    }

    private fun insertDot() {
        if (!this::mEditText.isInitialized){
            return
        }
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
        if (!this::mEditText.isInitialized){
            return
        }
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
