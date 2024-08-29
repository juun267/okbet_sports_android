package org.cxct.sportlottery.view.boundsEditText

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Space
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.TextFormFieldBoxesLayoutBinding
import org.cxct.sportlottery.util.MetricsUtil.convertDpToPixel
import splitties.systemservices.layoutInflater

/**
 * Text Field Boxes
 * Created by CarbonylGroup on 2017/08/25
 */
class TextFormFieldBoxes : FrameLayout {
    /**
     * all the default colors to be used on light or dark themes.
     */
    var DEFAULT_ERROR_COLOR = 0
    var DEFAULT_PRIMARY_COLOR = 0
    var DEFAULT_TEXT_COLOR = 0
    var DEFAULT_DISABLED_TEXT_COLOR = 0
    var DEFAULT_BG_COLOR = 0
    var DEFAULT_FG_COLOR = 0

    /**
     * whether the text field is enabled. True by default.
     */
    private var enabled: Boolean = false

    /**
     * labelText text at the top.
     */
    private var labelText: String? = null

    /**
     * labelText text style
     */
    private var labelTextStyle = 0

    /**
     * subLabelText text at the top.
     */
    private var subLabelText: String? = null

    /**
     * hintText text at the top.
     */
    private var hintText: String? = null

    /**
     * singleText text at the top.
     */
    private var singleText: String? = null

    /**
     * singleText text style
     */
    private var singleTextStyle = 0

    /**
     * singleText text color
     */
    private var singleTextColor = 0

    /**
     * singleText text start padding
     */
    private var singleTextStartPadding = 0

    /**
     * helper Label text at the bottom.
     */
    private var helperText: String? = null

    /**
     * max characters count limit. 0 means no limit. 0 by default.
     */
    private var maxCharacters = 0

    /**
     * min characters count limit. 0 means no limit. 0 by default.
     */
    private var minCharacters = 0

    /**
     * the text color for the helperLabel text. DEFAULT_TEXT_COLOR by default.
     */
    private var helperTextColor = 0

    /**
     * the text color for the counterLabel text. DEFAULT_TEXT_COLOR by default.
     */
    private var mCounterTextColor = 0

    /**
     * the text color for when something is wrong (e.g. exceeding max characters, setError()).
     * DEFAULT_ERROR_COLOR by default.
     */
    var errorColor = 0

    /**
     * the color for the underline, the floating label text and the icon signifier tint when HAVING focus.
     * Current theme primary color by default.
     */
    private var primaryColor = 0

    /**
     * the color for the underline, the floating label text and the icon signifier tints when NOT HAVING focus.
     * DEFAULT_TEXT_COLOR by default.
     */
    private var secondaryColor = 0
    //        this.panel.getBackground()
//                .setColorFilter(new PorterDuffColorFilter(colorRes, PorterDuff.Mode.SRC_IN));
    /**
     * the color for panel at the back. DEFAULT_BG_COLOR by default.
     */
    var panelBackgroundColor = 0

    /**
     * the resource ID of the icon signifier. 0 by default.
     */
    var iconSignifierResourceId = 0
        private set

    /**
     * the resource ID of the icon at the end. 0 by default.
     */
    var endIconResourceId = 0
        private set

    /**
     * whether the icon signifier will change its color when gaining or losing focus
     * as the label and the bottomLine do. True by default.
     */
    private var isResponsiveIconColor = false

    /**
     * whether to show the clear button at the end of the EditText. False by default.
     */
    private var hasClearButton = false

    /**
     * whether the EditText is having the focus. False by default.
     */
    private var hasFocus = false

    /**
     * Whether the label is fixed at top when there's a hint. False by default.
     */
    var alwaysShowHint = false

    /**
     * whether the field uses a dense spacing between its elements.
     * Usually useful in a multi-field form. False by default.
     */
    var useDenseSpacing = false

    /**
     * 被點擊後要不要顯示Title
     */
    private var hideSelectedTitle = false

    /**
     * 要不要显示副标题后的横线
     */
    private var hideSubView = false

    /**
     * whether the field uses a rtl direction for 'Persian (Farsi)' and 'Arabic' languages
     * False by default.
     */
    private var rtl = false
    private var labelColor = -1
    private var labelTopMargin = -1
    private var ANIMATION_DURATION = 100
    var isOnError = false
        private set

    private var activatedState = false

    /**
     * See [.setManualValidateError]
     */
    private var isManualValidateError = false
    /* View Getters */  var panel: View? = null
        private set
    private var bottomLine: View? = null
    private var labelSpace: Space? = null
    private var labelSpaceBelow: Space? = null
    private var editTextLayout: ViewGroup? = null
    private var editText: ExtendedEditText? = null
    private var rightShell: RelativeLayout? = null
    private var upperPanel: RelativeLayout? = null
    private var bottomPart: RelativeLayout? = null
    private var inputLayout: RelativeLayout? = null
    private var mainLayout: RelativeLayout? = null
    var helperLabel: AppCompatTextView? = null
        private set
    var counterLabel: AppCompatTextView? = null
        private set
    var floatingLabel: AppCompatTextView? = null
        private set
    private var floatingSubLabel: AppCompatTextView? = null
    private var floatingHintLabel: AppCompatTextView? = null
    private var floatingSingle: AppCompatTextView? = null
    private var clearButton: AppCompatImageButton? = null
    var iconImageButton: AppCompatImageButton? = null
        private set
    lateinit var endIconImageButton: AppCompatImageButton
        private set
    private var inputMethodManager: InputMethodManager? = null
    private var viewSub: View? = null
    private var textChangeListener: SimpleTextChangedWatcher? = null
    private var mPasswordToggleDummyDrawable: ColorDrawable? = null
    private var mOriginalEditTextEndDrawable: Drawable? = null

    val binding by lazy { TextFormFieldBoxesLayoutBinding.inflate(layoutInflater,this,false) }

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
        handleAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
        handleAttributes(context, attrs)
    }

    private fun init() {
        initDefaultColor()
        inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initDefaultColor() {
        val theme = context.theme
        var themeArray: TypedArray

        /* Get Default Error Color From Theme */DEFAULT_ERROR_COLOR =
            ContextCompat.getColor(context, R.color.color_F75452_E23434)

        /* Get Default Background Color From Theme */themeArray =
            theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorForeground))
        DEFAULT_BG_COLOR = adjustAlpha(themeArray.getColor(0, 0), 0.06f)

        /* Get Default Foreground Color From Theme */themeArray =
            theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorBackground))
        DEFAULT_FG_COLOR = themeArray.getColor(0, 0)

        /* Get Default Primary Color From Theme */themeArray =
            theme.obtainStyledAttributes(intArrayOf(
                R.attr.colorPrimary))
        DEFAULT_PRIMARY_COLOR =
            if (isLight(DEFAULT_BG_COLOR)) lighter(
                themeArray.getColor(0, 0),
                0.2f) else themeArray.getColor(0, 0)

        /* Get Default Text Color From Theme */themeArray =
            theme.obtainStyledAttributes(intArrayOf(android.R.attr.textColorTertiary))
        DEFAULT_TEXT_COLOR = themeArray.getColor(0, 0)

        /* Get Default Disabled Text Color From Theme */themeArray =
            theme.obtainStyledAttributes(intArrayOf(android.R.attr.disabledAlpha))
        val disabledAlpha = themeArray.getFloat(0, 0f)
        themeArray = theme.obtainStyledAttributes(intArrayOf(android.R.attr.textColorTertiary))
        DEFAULT_DISABLED_TEXT_COLOR = adjustAlpha(themeArray.getColor(0, 0), disabledAlpha)
        themeArray.recycle()
    }

    private fun findEditTextChild(): ExtendedEditText? {
        return if (childCount > 0 && getChildAt(0) is ExtendedEditText) getChildAt(0) as ExtendedEditText else null
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initViews()
        triggerSetters()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY) {

            /* match_parent or specific value */
            mainLayout!!.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            inputLayout!!.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            upperPanel!!.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            editTextLayout!!.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        } else if (widthMode == MeasureSpec.AT_MOST) {

            /* wrap_content */
            mainLayout!!.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            inputLayout!!.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            upperPanel!!.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            editTextLayout!!.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        if (heightMode == MeasureSpec.EXACTLY) {

            /* match_parent or specific value */
            mainLayout!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            panel!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            rightShell!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            upperPanel!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            (bottomPart!!.layoutParams as RelativeLayout.LayoutParams)
                .addRule(RelativeLayout.BELOW, 0)
            (bottomPart!!.layoutParams as RelativeLayout.LayoutParams)
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            (panel!!.layoutParams as RelativeLayout.LayoutParams)
                .addRule(RelativeLayout.ABOVE, R.id.text_field_boxes_bottom)
        } else if (heightMode == MeasureSpec.AT_MOST) {

            /* wrap_content */
            mainLayout!!.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            panel!!.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            rightShell!!.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            upperPanel!!.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            (bottomPart!!.layoutParams as RelativeLayout.LayoutParams)
                .addRule(RelativeLayout.BELOW, R.id.text_field_boxes_panel)
            (bottomPart!!.layoutParams as RelativeLayout.LayoutParams)
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
            (panel!!.layoutParams as RelativeLayout.LayoutParams)
                .addRule(RelativeLayout.ABOVE, 0)
        }
        updateClearAndEndIconLayout()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun updateClearAndEndIconLayout() {
        if (endIconImageButton != null && endIconImageButton!!.drawable != null || hasClearButton) {
            val clearButtonW = if (hasClearButton) clearButton!!.measuredWidth else 0
            val endIconW =
                if (endIconImageButton != null && endIconImageButton!!.drawable != null) endIconImageButton!!.measuredWidth else 0
            if (mPasswordToggleDummyDrawable == null) mPasswordToggleDummyDrawable = ColorDrawable()
            upperPanel!!.setPadding(resources.getDimensionPixelOffset(R.dimen.upper_panel_paddingStart),
                0,
                resources.getDimensionPixelOffset(
                    R.dimen.upper_panel_paddingEnd_small),
                0)

            // We add a fake drawableRight to EditText so it will have padding on the right side and text will not go
            // under the icons.
            if (!rtl) mPasswordToggleDummyDrawable!!.setBounds(0, 0, endIconW + clearButtonW, 0)
            val compounds = TextViewCompat.getCompoundDrawablesRelative(
                editText!!)
            // Store the user defined end compound drawable so that we can restore it later
            if (compounds[2] !== mPasswordToggleDummyDrawable) {
                mOriginalEditTextEndDrawable = compounds[2]
            }
            TextViewCompat.setCompoundDrawablesRelative(editText!!, compounds[0], compounds[1],
                mPasswordToggleDummyDrawable, compounds[3])
        } else {
            upperPanel!!.setPadding(resources.getDimensionPixelOffset(R.dimen.upper_panel_paddingStart),
                0,
                resources.getDimensionPixelOffset(
                    R.dimen.upper_panel_paddingEnd),
                0)
            if (mPasswordToggleDummyDrawable != null) {
                // Make sure that we remove the dummy end compound drawable if it exists, and then
                // clear it
                val compounds = TextViewCompat.getCompoundDrawablesRelative(
                    editText!!)
                if (compounds[2] === mPasswordToggleDummyDrawable) {
                    TextViewCompat.setCompoundDrawablesRelative(editText!!, compounds[0],
                        compounds[1], mOriginalEditTextEndDrawable, compounds[3])
                    mPasswordToggleDummyDrawable = null
                }
            }
        }
        if (hideSubView) {
            editTextLayout!!.setPadding(16, 0, 0, 0)
            floatingHintLabel!!.setPadding(16, 0, 0, 0)
        }
    }
    private fun initViews() {
        editText = findEditTextChild()
        if (editText == null) return
        addView(binding.root)
        removeView(editText)
        editText!!.setBackgroundColor(Color.TRANSPARENT)
        editText!!.setDropDownBackgroundDrawable(ColorDrawable(DEFAULT_FG_COLOR))
        editText!!.minimumWidth = 10
        inputLayout = findViewById(R.id.text_field_boxes_input_layout)
        mainLayout = findViewById(R.id.rl_main)
        floatingLabel = findViewById(R.id.text_field_boxes_label)
        floatingSubLabel = findViewById(R.id.text_field_boxes_sub_label)
        floatingHintLabel = findViewById(R.id.text_field_boxes_hint_label)
        floatingSingle = findViewById(R.id.text_field_boxes_single_label)
        panel = findViewById(R.id.text_field_boxes_panel)
        labelSpace = findViewById(R.id.text_field_boxes_label_space)
        labelSpaceBelow = findViewById(R.id.text_field_boxes_label_space_below)
        rightShell = findViewById(R.id.text_field_boxes_right_shell)
        upperPanel = findViewById(R.id.text_field_boxes_upper_panel)
        bottomPart = findViewById(R.id.text_field_boxes_bottom)
        bottomLine = findViewById(R.id.bottom_line)
        clearButton = findViewById(R.id.text_field_boxes_clear_button)
        endIconImageButton = findViewById(R.id.text_field_boxes_end_icon_button)
        helperLabel = findViewById(R.id.text_field_boxes_helper)
        counterLabel = findViewById(R.id.text_field_boxes_counter)
        iconImageButton = findViewById(R.id.text_field_boxes_imageView)
        editTextLayout = findViewById(R.id.text_field_boxes_editTextLayout)
        viewSub = findViewById(R.id.view_sub)
        inputLayout?.addView(editText)
        //        this.editTextLayout.setAlpha(0f);
        floatingLabel?.setPivotX(0f)
        floatingLabel?.setPivotY(0f)
        floatingSubLabel?.setPivotX(0f)
        floatingSubLabel?.setPivotY(0f)
        floatingHintLabel?.setPivotX(0f)
        floatingHintLabel?.setPivotY(0f)
        floatingSingle?.setPivotX(0f)
        floatingSingle?.setPivotX(0f)
        labelColor = floatingLabel!!.getCurrentTextColor()
        clearButton?.setColorFilter(DEFAULT_TEXT_COLOR)
        clearButton?.setAlpha(0.35f)
        //this.endIconImageButton.setColorFilter(DEFAULT_TEXT_COLOR);
        //this.endIconImageButton.setAlpha(0.54f);
        labelTopMargin = RelativeLayout.LayoutParams::class.java
            .cast(floatingLabel?.getLayoutParams()).topMargin
        initOnClick()

        // Have to update useDenseSpacing then the dimensions before the first activation
        useDenseSpacing = useDenseSpacing
        updateDimens(useDenseSpacing)
        if (!editText!!.text.toString().isEmpty() || hasFocus) activate(false)
    }

    private fun initOnClick() {
        val mainBody: FrameLayout = this
        panel!!.setOnClickListener {
            if (!isActivated) activate(true)
            setHasFocus(true)
            inputMethodManager!!.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            mainBody.performClick()
        }
        iconImageButton!!.setOnClickListener {
            if (!isActivated) activate(true)
            setHasFocus(true)
            inputMethodManager!!.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            mainBody.performClick()
        }
        editText!!.setDefaultOnFocusChangeListener { view, b ->
            if (b) setHasFocus(true) else setHasFocus(false)
        }
        editText!!.addTextChangedListener(object : TextWatcher {
            private var lastValue = ""
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //do nothing
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //do nothing
            }

            override fun afterTextChanged(editable: Editable) {
                if (!activatedState && !editable.toString().isEmpty()) activate(true)
                if (activatedState && editable.toString().isEmpty() && !hasFocus) deactivate()
                if (isManualValidateError) {
                    updateCounterText(false)
                } else {
                    validate() //this will call updateCounterText(true);
                }

                // Only trigger simple watcher when the String actually changed
                if (lastValue != editable.toString()) {
                    lastValue = editable.toString()
                    if (textChangeListener != null) {
                        textChangeListener!!.onTextChanged(editable.toString(), isOnError)
                    }
                }
            }
        })
        clearButton!!.setOnClickListener { editText!!.setText("") }
        editText!!.setDefaultOnFocusChangeListener { view, b ->
            if (b) {
                setHasFocus(true)
                inputMethodManager!!.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                mainBody.performClick()
            } else setHasFocus(false)
        }
    }

    private fun handleAttributes(context: Context, attrs: AttributeSet?) {
        try {
            val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TextFieldBoxes)

            /* Texts */labelText = if (styledAttrs.getString(R.styleable.TextFieldBoxes_labelText)
                == null
            ) "" else styledAttrs.getString(R.styleable.TextFieldBoxes_labelText)
            labelTextStyle =
                styledAttrs.getInt(R.styleable.TextFieldBoxes_labelTextStyle, Typeface.BOLD)
            subLabelText = if (styledAttrs.getString(R.styleable.TextFieldBoxes_subLabelText)
                == null
            ) "" else styledAttrs.getString(R.styleable.TextFieldBoxes_subLabelText)
            hintText = if (styledAttrs.getString(R.styleable.TextFieldBoxes_hintText)
                == null
            ) "" else styledAttrs.getString(R.styleable.TextFieldBoxes_hintText)
            singleText = if (styledAttrs.getString(R.styleable.TextFieldBoxes_singleText)
                == null
            ) "" else styledAttrs.getString(R.styleable.TextFieldBoxes_singleText)
            singleTextStyle =
                styledAttrs.getInt(R.styleable.TextFieldBoxes_singleTextStyle, Typeface.BOLD)
            singleTextColor =
                styledAttrs.getInt(R.styleable.TextFieldBoxes_singleTextColor, DEFAULT_TEXT_COLOR)
            singleTextStartPadding =
                styledAttrs.getInt(R.styleable.TextFieldBoxes_singleTextStartPadding, 0)
            helperText = if (styledAttrs.getString(R.styleable.TextFieldBoxes_helperText)
                == null
            ) "" else styledAttrs.getString(R.styleable.TextFieldBoxes_helperText)

            /* Colors */helperTextColor = styledAttrs
                .getInt(R.styleable.TextFieldBoxes_helperTextColor, DEFAULT_TEXT_COLOR)
            mCounterTextColor = styledAttrs
                .getInt(R.styleable.TextFieldBoxes_mCounterTextColor, DEFAULT_TEXT_COLOR)
            errorColor = styledAttrs
                .getInt(R.styleable.TextFieldBoxes_errorColor, DEFAULT_ERROR_COLOR)
            primaryColor = styledAttrs
                .getColor(R.styleable.TextFieldBoxes_primaryColor, DEFAULT_PRIMARY_COLOR)
            secondaryColor = styledAttrs
                .getColor(R.styleable.TextFieldBoxes_secondaryColor, DEFAULT_TEXT_COLOR)
            //            this.panelBackgroundColor = styledAttrs
//                    .getColor(R.styleable.TextFieldBoxes_panelBackgroundColor, DEFAULT_BG_COLOR);

            /* Characters counter */maxCharacters =
                styledAttrs.getInt(R.styleable.TextFieldBoxes_maxCharacters, 0)
            minCharacters = styledAttrs.getInt(R.styleable.TextFieldBoxes_minCharacters, 0)

            /* Others */isManualValidateError =
                styledAttrs.getBoolean(R.styleable.TextFieldBoxes_manualValidateError, false)
            hideSubView = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_hideSubView, false)
            enabled = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_enabled, true)
            iconSignifierResourceId =
                styledAttrs.getResourceId(R.styleable.TextFieldBoxes_iconSignifier, 0)
            endIconResourceId = styledAttrs.getResourceId(R.styleable.TextFieldBoxes_endIcon, 0)
            isResponsiveIconColor = styledAttrs
                .getBoolean(R.styleable.TextFieldBoxes_isResponsiveIconColor, true)
            hasClearButton = styledAttrs
                .getBoolean(R.styleable.TextFieldBoxes_hasClearButton, false)
            hasFocus = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_hasFocus, false)
            alwaysShowHint =
                styledAttrs.getBoolean(R.styleable.TextFieldBoxes_alwaysShowHint, false)
            useDenseSpacing =
                styledAttrs.getBoolean(R.styleable.TextFieldBoxes_useDenseSpacing, false)
            rtl = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_rtl, false)
            hideSelectedTitle =
                styledAttrs.getBoolean(R.styleable.TextFieldBoxes_hideSelectedTitle, false)
            styledAttrs.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSimpleTextChangeWatcher(textChangeListener: SimpleTextChangedWatcher?) {
        this.textChangeListener = textChangeListener
    }

    /**
     * lower the labelText labelText Label when there is no text at losing focus
     */
    private fun deactivate() {
        //this.mainLayout.setBackgroundResource(R.drawable.bg_bounds_edittext);
        bottomLine!!.setBackgroundColor(ContextCompat.getColor(context, R.color.color_E3E8EE))
        if (editText!!.text.toString().isEmpty()) {
            if (alwaysShowHint && !editText!!.hint.toString().isEmpty()) {

                // If alwaysShowHint, and the hint is not empty,
                // keep the label on the top and EditText visible.
                editTextLayout!!.alpha = 1f
                floatingLabel!!.scaleX = 0.75f
                floatingLabel!!.scaleY = 0.75f
            } else {
                // If not, animate the label and hide the EditText.
//                this.editTextLayout.setAlpha(0);
                ViewCompat.animate(floatingLabel!!)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f).duration = ANIMATION_DURATION.toLong()
                //                ViewCompat.animate(floatingHintLabel)
//                        .alpha(1)
//                        .scaleX(1)
//                        .scaleY(1)
//                        .translationX(0)
//                        .translationY(0)
//                        .setDuration(ANIMATION_DURATION);
                floatingHintLabel!!.visibility = VISIBLE
                ViewCompat.animate(floatingSingle!!)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(0f)
                    .translationY(0f).duration = ANIMATION_DURATION.toLong()
            }
            if (editText!!.hasFocus()) {
                inputMethodManager!!.hideSoftInputFromWindow(editText!!.windowToken, 0)
                editText!!.clearFocus()
            }
            if (hideSelectedTitle) {
                floatingSingle!!.visibility = VISIBLE
            }
        }
        activatedState = false
    }

    /**
     * raise the labelText labelText Label when gaining focus
     */
    fun activate(animated: Boolean) {
        editText!!.alpha = 1f
        //this.mainLayout.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.bg2));
        if (editText!!.text.toString().isEmpty() && !isActivated) {

//            this.editTextLayout.setAlpha(0f);
            floatingLabel!!.scaleX = 1f
            floatingLabel!!.scaleY = 1f
            floatingLabel!!.translationY = 0f
        }
        //final boolean keepHint = this.alwaysShowHint && !this.editText.getHint().toString().isEmpty();
        if (animated) {
            //this.mainLayout.setBackgroundResource(R.drawable.bg_bounds_edittext_h);
            bottomLine!!.setBackgroundColor(ContextCompat.getColor(context,
                R.color.color_317FFF_0760D4))
            ViewCompat.animate(editTextLayout!!)
                .alpha(1f).duration = ANIMATION_DURATION.toLong()

//            ViewCompat.animate(this.floatingLabel)
//                    .scaleX(0.85f)
//                    .scaleY(0.85f)
//                    .setDuration(ANIMATION_DURATION);
//            ViewCompat.animate(this.floatingHintLabel)
//                    .scaleX(0.92f)
//                    .scaleY(0.92f)
//                    .translationX(floatingLabel.getWidth()+getContext().getResources().getDimensionPixelOffset(R.dimen.label_active_margin_left))
//                    .translationY(-labelTopMargin +
//                            getContext().getResources().getDimensionPixelOffset(R.dimen.label_active_margin_top)-floatingLabel.getHeight()+2-
//                            getContext().getResources().getDimensionPixelOffset(R.dimen.label2_active_margin_top))
//                    .setDuration(ANIMATION_DURATION);
            floatingHintLabel!!.visibility = GONE

            //驗證碼要特殊處理 by bill
            if (hideSelectedTitle) {
                floatingSingle!!.visibility = GONE
                labelSpace!!.visibility = GONE
                labelTopMargin = 0
            }
            ViewCompat.animate(floatingSingle!!)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .translationY((-labelTopMargin -
                        context.resources.getDimensionPixelOffset(R.dimen.label2_active_margin_top) + 6).toFloat()).duration =
                ANIMATION_DURATION.toLong()
        } else {
            editTextLayout!!.alpha = 1f
            floatingLabel!!.scaleX = 0.85f
            floatingLabel!!.scaleY = 0.85f
            //            this.floatingHintLabel.setScaleX(0.92f);
//            this.floatingHintLabel.setScaleY(0.92f);
//            this.floatingHintLabel.setTranslationX(floatingLabel.getWidth()+getContext().getResources().getDimensionPixelOffset(R.dimen.label_active_margin_left));
//            this.floatingHintLabel.setTranslationY(-labelTopMargin +
//                    getContext().getResources().getDimensionPixelOffset(R.dimen.label_active_margin_top)-floatingLabel.getHeight()+2-
//                    getContext().getResources().getDimensionPixelOffset(R.dimen.label2_active_margin_top));
            floatingHintLabel!!.visibility = GONE
            floatingSingle!!.scaleX = 0.85f
            floatingSingle!!.scaleY = 0.85f
            floatingSingle!!.translationX = (-labelTopMargin -
                    context.resources.getDimensionPixelOffset(R.dimen.label2_active_margin_top)).toFloat()
        }
        activatedState = true
    }

    private fun makeCursorBlink() {
        val hintCache = editText!!.hint
        editText!!.hint = " "
        editText!!.hint = hintCache
    }

    /**
     * set the color of the labelText Label, EditText cursor, icon signifier and the underline
     *
     * @param colorRes color resource
     */
    private fun setHighlightColor(colorRes: Int) {

//        this.floatingLabel.setTextColor(colorRes);
//        setCursorDrawableColor(this.editText, colorRes);
//
//        if (getIsResponsiveIconColor()) {
//            this.iconImageButton.setColorFilter(colorRes);
//            if (colorRes == secondaryColor) this.iconImageButton.setAlpha(0.54f);
//            else this.iconImageButton.setAlpha(1f);
//        }
//
//        if (colorRes == DEFAULT_DISABLED_TEXT_COLOR) this.iconImageButton.setAlpha(0.35f);
    }

    /**
     * By default the field is validated each time a key is pressed and at construction,
     * this means a field with a minimum length requirement will start in Error state.
     * Set this value to true to validate only when [.validate] is called.
     *
     * @param isManualValidateError the new value
     */
    @JvmName("setManualValidateError1")
    private fun setManualValidateError(isManualValidateError: Boolean) {
        this.isManualValidateError = isManualValidateError
    }

    /**
     * Update the onError state of this component
     *
     * @return true if valid (the inverse value of onError)
     */
    fun validate(): Boolean { //Reverted: "validateError" has the opposite meaning and is incorrect and does not follow conventions
        removeError()
        updateCounterText(true)
        if (isOnError) {
            setError(null, false)
        }
        return !isOnError
    }

    @Deprecated("""Pseudonym for {@link #validate()} to provide legacy support for
      a bad PR.
      <p>
      Note: This does NOT validate that there is an error, it does the opposite""")
    fun validateError(): Boolean {
        return validate()
    }

    /**
     * check if the TextFieldBox should use a dense spacing,
     * then change the layout dimens accordingly
     */
    private fun updateDimens(useDenseSpacing: Boolean) {
        val res = context.resources

        /* Floating Label */
        var lp = floatingLabel!!.layoutParams as RelativeLayout.LayoutParams
        lp.topMargin = res.getDimensionPixelOffset(
            if (useDenseSpacing) R.dimen.dense_label_idle_margin_top else R.dimen.label_idle_margin_top
        )
        floatingLabel!!.layoutParams = lp

        /* EditText Layout */inputLayout!!.setPadding(
            0,
            if (hideSelectedTitle) 0 else res.getDimensionPixelOffset(
                if (useDenseSpacing) R.dimen.dense_editTextLayout_padding_top else R.dimen.editTextLayout_padding_top
            ),
            0,
            if (hideSelectedTitle) 0 else res.getDimensionPixelOffset(R.dimen.editTextLayout_padding_bottom))

        /* End Icon */endIconImageButton!!.minimumHeight = res.getDimensionPixelOffset(
            if (useDenseSpacing) R.dimen.end_icon_min_height else R.dimen.dense_end_icon_min_height
        )
        endIconImageButton!!.minimumWidth = res.getDimensionPixelOffset(
            if (useDenseSpacing) R.dimen.end_icon_min_width else R.dimen.dense_end_icon_min_width
        )

        /* Clear Icon */clearButton!!.minimumHeight = res.getDimensionPixelOffset(
            if (useDenseSpacing) R.dimen.clear_button_min_height else R.dimen.dense_clear_button_min_height
        )
        clearButton!!.minimumWidth = res.getDimensionPixelOffset(
            if (useDenseSpacing) R.dimen.clear_button_min_width else R.dimen.dense_clear_button_min_width
        )

        /* Bottom View */lp = bottomPart!!.layoutParams as RelativeLayout.LayoutParams
        lp.topMargin = res.getDimensionPixelOffset(
            if (useDenseSpacing) R.dimen.dense_bottom_marginTop else R.dimen.bottom_marginTop
        )
        bottomPart!!.layoutParams = lp

        /* EditText */editText!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(
            if (useDenseSpacing) R.dimen.dense_edittext_text_size else R.dimen.edittext_text_size
        ))
        labelTopMargin = RelativeLayout.LayoutParams::class.java
            .cast(floatingLabel!!.layoutParams).topMargin
        requestLayout()
    }

    /**
     * check if the character count meets the upper or lower limits,
     *
     *
     * if performValidation and exceeds limit, setCounterError()
     * otherwise removeCounterError()
     *
     *
     *
     *
     *
     * @param performValidation - true if error state should be applied or removed by this calls See [                          ][.setManualValidateError]  *NOTE: SPACE AND LINE FEED WILL NOT COUNT*
     */
    private fun updateCounterText(performValidation: Boolean) {

        /* Show clear button if there is anything */
        if (hasClearButton) {
            if (editText!!.text.toString().length == 0) {
                showClearButton(false)
            } else {
                showClearButton(true)
            }
        }

        /* Don't Count Space & Line Feed */
        val length = editText!!.text.toString().replace(" ".toRegex(), "")
            .replace("\n".toRegex(), "").length
        val lengthStr = Integer.toString(length) + " / "
        //String counterLabelResourceStr = getResources().getString(R.string.counter_label_text_constructor);
//        if (this.maxCharacters > 0) {
//            if (this.minCharacters > 0) {
//                /* MAX & MIN */
//                this.counterLabel.setText(String.format(counterLabelResourceStr, lengthStr, Integer.toString(this.minCharacters), "-", Integer.toString(this.maxCharacters)));
//                if (performValidation) {
//                    if (length < this.minCharacters || length > this.maxCharacters) {
//                        setCounterError();
//                    } else {
//                        removeCounterError();
//                    }
//                }
//            } else {
//                /* MAX ONLY */
//                this.counterLabel.setText(String.format(counterLabelResourceStr, lengthStr, Integer.toString(this.maxCharacters), "", ""));
//                if (performValidation) {
//                    if (length > this.maxCharacters) {
//                        setCounterError();
//                    } else {
//                        removeCounterError();
//                    }
//                }
//            }
//        } else {
//            if (this.minCharacters > 0) {
//                /* MIN ONLY */
//                this.counterLabel.setText(String.format(counterLabelResourceStr, lengthStr, Integer.toString(this.minCharacters), "+", ""));
//                if (performValidation) {
//                    if (length < this.minCharacters) {
//                        setCounterError();
//                    } else {
//                        removeCounterError();
//                    }
//                }
//            } else {
//                this.counterLabel.setText("");
//                if (performValidation) {
//                    removeCounterError();
//                }
//            }
//        }
    }

    /**
     * check if the helper label and counter are both empty.
     * if true, make the bottom view VISIBLE.
     * otherwise, make it GONE.
     */
    private fun updateBottomViewVisibility() {
        if (helperLabel!!.text.toString().isEmpty() &&
            counterLabel!!.text.toString().isEmpty()
        ) //this.bottomPart.setVisibility(View.GONE);
            helperLabel!!.visibility = GONE else helperLabel!!.visibility = VISIBLE
    }

    /**
     * set highlight color and counter Label text color to error color
     */
    private fun setCounterError() {
        isOnError = true
        setHighlightColor(errorColor)
        counterLabel!!.setTextColor(errorColor)
    }

    /**
     * set highlight color to primary color if having focus,
     * otherwise set to secondaryColor
     * set counterLabel Label text color to DEFAULT_TEXT_COLOR
     */
    private fun removeCounterError() {
        isOnError = false
        if (hasFocus) setHighlightColor(primaryColor) else setHighlightColor(secondaryColor)
        counterLabel!!.setTextColor(mCounterTextColor)
    }

    /**
     * set highlight color and helperLabel Label text color to errorColor
     * set helperLabel Label text to error message
     *
     * @param errorText optional error message
     * @param giveFocus whether the field will gain focus when set error on
     */
    fun setError(errorText: String?, giveFocus: Boolean) {
        if (enabled && errorText != null && !errorText.isEmpty()) {
            //this.mainLayout.setBackgroundResource(R.drawable.bg_bounds_edittext_error);
            bottomLine!!.setBackgroundColor(ContextCompat.getColor(context, R.color.color_E44438))
            isOnError = true
            //activate(true);
            setHighlightColor(errorColor)
            helperLabel!!.setTextColor(errorColor)
            if (giveFocus) setHasFocus(true)
            makeCursorBlink()
        }
        helperLabel!!.text = errorText
        updateBottomViewVisibility()
        if (errorText == null && hasFocus) {
            activate(true)
        } else if (errorText == null) {
            deactivate()
        }
    }

    /**
     * set highlight to primaryColor if having focus,
     * otherwise set to secondaryColor
     * set helperLabel Label text color to DEFAULT_TEXT_COLOR
     *
     *
     * *NOTE: WILL BE CALLED WHEN THE EDITTEXT CHANGES
     * UNLESS YOU [.setManualValidateError] TO TRUE*
     */
    fun removeError() {
        isOnError = false
        //this.mainLayout.setBackgroundResource(R.drawable.bg_bounds_edittext_h);
        bottomLine!!.setBackgroundColor(ContextCompat.getColor(context,
            R.color.color_317FFF_0760D4))
        if (hasFocus) setHighlightColor(primaryColor) else setHighlightColor(secondaryColor)
        helperLabel!!.setTextColor(helperTextColor)
        helperLabel!!.text = helperText
        updateBottomViewVisibility()
    }

    private fun showClearButton(show: Boolean) {
        if (show) clearButton!!.visibility = VISIBLE else clearButton!!.visibility = GONE
    }

    private fun triggerSetters() {

        /* Texts */
        setLabelText(labelText)
        setLableTextStyle(labelTextStyle)
        setSubLabelText(subLabelText)
        setHideSubView(hideSubView)
        setHelperText(helperText)
        setHintText(hintText)
        setSingleText(singleText)
        setSingleTextStyle(singleTextStyle)
        setSingleTextColor(singleTextColor)
        setSingleTextStartPadding(singleTextStartPadding)
        /* Colors */setHelperTextColor(helperTextColor)
        setmCounterTextColor(mCounterTextColor)
        errorColor = errorColor
        setPrimaryColor(primaryColor)
        setSecondaryColor(secondaryColor)
        //setPanelBackgroundColor(this.panelBackgroundColor);

        /* Characters counter */setMaxCharacters(maxCharacters)
        setMinCharacters(minCharacters)

        /* Others */isEnabled = enabled
        setIconSignifier(iconSignifierResourceId)
        setEndIcon(endIconResourceId)
        setIsResponsiveIconColor(isResponsiveIconColor)
        setHasClearButton(hasClearButton)
        setHasFocus(hasFocus)
        alwaysShowHint = alwaysShowHint
        updateCounterText(!isManualValidateError)
        updateBottomViewVisibility()
    }

    private fun setHideSubView(hideSubView: Boolean) {
        this.hideSubView = hideSubView
        if (this.hideSubView) {
            viewSub!!.visibility = VISIBLE
        } else {
            viewSub!!.visibility = GONE
        }
    }

    /* Text Setters */
    fun setLabelText(labelText: String?) {
        this.labelText = labelText
        floatingLabel!!.text = this.labelText
        if (labelText!!.isEmpty()) {
            floatingLabel!!.visibility = INVISIBLE
            labelSpace!!.visibility = INVISIBLE
            labelSpaceBelow!!.visibility = GONE
        } else {
            floatingLabel!!.visibility = VISIBLE
            labelSpace!!.visibility = VISIBLE
            labelSpaceBelow!!.visibility = GONE
        }
    }

    fun setLableTextStyle(textStyle: Int) {
        labelTextStyle = textStyle
        floatingLabel!!.setTypeface(floatingLabel!!.typeface, labelTextStyle)
    }

    fun setSubLabelText(subLabelText: String?) {
        this.subLabelText = subLabelText
        floatingSubLabel!!.text = this.subLabelText
        val layoutParams = editTextLayout!!.layoutParams as RelativeLayout.LayoutParams
        if (subLabelText!!.isEmpty()) {
            floatingSubLabel!!.visibility = GONE
            layoutParams.removeRule(RelativeLayout.ALIGN_TOP)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
            layoutParams.topMargin = 0
        } else {
            floatingSubLabel!!.visibility = VISIBLE
            layoutParams.removeRule(RelativeLayout.CENTER_VERTICAL)
            layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.text_field_boxes_sub_label)
            val topMargin = convertDpToPixel(2f, editTextLayout!!.context).toInt()
            layoutParams.topMargin = -(topMargin + inputLayout!!.paddingTop)
        }
        editTextLayout!!.layoutParams = layoutParams
    }

    fun setHintText(hintText: String?) {
        this.hintText = hintText
        floatingHintLabel!!.text = this.hintText
        if (hintText!!.isEmpty()) {
            floatingHintLabel!!.visibility = GONE
        } else {
            floatingHintLabel!!.visibility = VISIBLE
        }
    }

    fun setSingleText(singleText: String?) {
        this.singleText = singleText
        floatingSingle!!.text = this.singleText
        if (singleText!!.isEmpty()) {
            floatingSingle!!.visibility = GONE
        } else {
            floatingSingle!!.visibility = VISIBLE
        }
    }

    fun setSingleTextStyle(textStyle: Int) {
        singleTextStyle = textStyle
        floatingSingle!!.setTypeface(floatingSingle!!.typeface, singleTextStyle)
    }

    fun setSingleTextColor(colorRes: Int) {
        floatingSingle!!.setTextColor(colorRes)
    }

    fun setSingleTextStartPadding(start: Int) {
        val startPadding = convertDpToPixel(start.toFloat(), floatingSingle!!.context).toInt()
        floatingSingle!!.setPadding(startPadding, 0, 0, 0)
    }

    fun setHelperText(helperText: String?) {
        this.helperText = helperText
        helperLabel!!.text = this.helperText
    }

    /* Color Setters */
    fun setHelperTextColor(colorRes: Int) {
        helperTextColor = colorRes
        helperLabel!!.setTextColor(helperTextColor)
    }

    fun setmCounterTextColor(colorRes: Int) {
        mCounterTextColor = colorRes
        counterLabel!!.setTextColor(mCounterTextColor)
    }

    /**
     * *NOTE: the color will automatically be made lighter by 20% if it's on the DARK theme*
     */
    fun setPrimaryColor(colorRes: Int) {
        primaryColor = colorRes
        if (hasFocus) setHighlightColor(primaryColor)
    }

    fun setSecondaryColor(colorRes: Int) {
        secondaryColor = colorRes
        if (!hasFocus) setHighlightColor(secondaryColor)
    }

    /* Characters Counter Setters */
    fun setMaxCharacters(maxCharacters: Int) {
        this.maxCharacters = maxCharacters
        updateCounterText(!isManualValidateError)
    }

    /**
     * remove the max character count limit by setting it to 0
     */
    fun removeMaxCharacters() {
        maxCharacters = 0
        updateCounterText(!isManualValidateError)
    }

    fun setMinCharacters(minCharacters: Int) {
        this.minCharacters = minCharacters
        updateCounterText(!isManualValidateError)
    }

    /**
     * remove the min character count limit by setting it to 0
     */
    fun removeMinCharacters() {
        minCharacters = 0
        updateCounterText(!isManualValidateError)
    }

    /* Other Setters */
    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (this.enabled) {
            editText!!.isEnabled = true
            editText!!.isFocusableInTouchMode = true
            editText!!.isFocusable = true
            bottomPart!!.visibility = VISIBLE
            helperLabel!!.visibility = VISIBLE
            counterLabel!!.visibility = GONE
            panel!!.isEnabled = true
            iconImageButton!!.isEnabled = true
            iconImageButton!!.isClickable = true
            setHighlightColor(secondaryColor)
            updateCounterText(!isManualValidateError)
        } else {
            removeError()
            setHasFocus(false)
            editText!!.isEnabled = false
            editText!!.isFocusableInTouchMode = false
            editText!!.isFocusable = false
            iconImageButton!!.isClickable = false
            iconImageButton!!.isEnabled = false
            bottomPart!!.visibility = INVISIBLE
            helperLabel!!.visibility = INVISIBLE
            counterLabel!!.visibility = GONE
            panel!!.isEnabled = false
            setHighlightColor(DEFAULT_DISABLED_TEXT_COLOR)
        }
    }

    fun setIconSignifier(resourceID: Int) {
        iconSignifierResourceId = resourceID
        if (iconSignifierResourceId != 0) {
            iconImageButton!!.setImageResource(iconSignifierResourceId)
            iconImageButton!!.visibility = VISIBLE
        } else removeIconSignifier()
    }

    fun setIconSignifier(drawable: Drawable?) {
        removeIconSignifier()
        iconImageButton!!.setImageDrawable(drawable)
        iconImageButton!!.visibility = VISIBLE
    }

    /**
     * remove the icon by setting the visibility of the image view to View.GONE
     */
    fun removeIconSignifier() {
        iconSignifierResourceId = 0
        iconImageButton!!.visibility = GONE
    }

    fun setEndIcon(resourceID: Int) {
        endIconResourceId = resourceID
        if (endIconResourceId != 0) {
            endIconImageButton!!.setImageResource(endIconResourceId)
            endIconImageButton!!.visibility = VISIBLE
        } else removeEndIcon()
        updateClearAndEndIconLayout()
    }

    fun setEndIcon(drawable: Drawable?) {
        removeEndIcon()
        endIconImageButton!!.setImageDrawable(drawable)
        endIconImageButton!!.visibility = VISIBLE
        updateClearAndEndIconLayout()
    }

    /**
     * remove the end icon by setting the visibility of the end image view to View.GONE
     */
    fun removeEndIcon() {
        endIconResourceId = 0
        endIconImageButton!!.setImageDrawable(null)
        endIconImageButton!!.visibility = GONE
        updateClearAndEndIconLayout()
    }

    /**
     * set whether the icon signifier will change its color when gaining or losing focus
     * as the label and the bottomLine do.
     *
     * @param isResponsiveIconColor if true, the icon's color will always be HighlightColor (the same as the bottomLine)
     * if false, the icon will always be in primaryColor
     */
    fun setIsResponsiveIconColor(isResponsiveIconColor: Boolean) {
        this.isResponsiveIconColor = isResponsiveIconColor
        if (this.isResponsiveIconColor) {
            if (hasFocus) {
                iconImageButton!!.setColorFilter(primaryColor)
                iconImageButton!!.alpha = 1f
            } else {
                iconImageButton!!.setColorFilter(secondaryColor)
                iconImageButton!!.alpha = 0.54f
            }
        } else {
            iconImageButton!!.setColorFilter(primaryColor)
            iconImageButton!!.alpha = 1f
        }
    }

    fun setHasClearButton(hasClearButton: Boolean) {
        this.hasClearButton = hasClearButton
        showClearButton(hasClearButton)
        updateClearAndEndIconLayout()
    }

    /**
     * set if the EditText is having focus
     *
     * @param hasFocus gain focus if true, lose if false
     */
    fun setHasFocus(hasFocus: Boolean) {
        this.hasFocus = hasFocus
        if (this.hasFocus) {
            activate(true)
            editText!!.requestFocus()
            makeCursorBlink()

            /* if there's an error, keep the error color */if (!isOnError && enabled) setHighlightColor(
                primaryColor)
        } else {
            deactivate()

            /* if there's an error, keep the error color */if (!isOnError && enabled) setHighlightColor(
                secondaryColor)
        }
    }

    /**
     * only gain or lose focus at TextFieldBoxes or not by params focusEditText
     * @param hasFocus gain focus if true, lose if false
     * @param focusEditText only focus TextFieldBoxes if false, both TextFieldBoxes and EditText if true
     */
    fun setHasFocus(hasFocus: Boolean, focusEditText: Boolean) {
        this.hasFocus = hasFocus
        if (this.hasFocus) {
            activate(true)
            if (focusEditText) {
                editText!!.requestFocus()
            }
            makeCursorBlink()

            /* if there's an error, keep the error color */if (!isOnError && enabled) setHighlightColor(
                primaryColor)
        } else {
            deactivate()

            /* if there's an error, keep the error color */if (!isOnError && enabled) setHighlightColor(
                secondaryColor)
        }
    }

    /* Text Getters */
    fun getLabelText(): String? {
        return labelText
    }

    fun getHelperText(): String? {
        return helperText
    }

    val counterText: String
        get() = counterLabel!!.text.toString()

    /* Color Getters */
    fun getHelperTextColor(): Int {
        return helperTextColor
    }

    fun getmCounterTextColor(): Int {
        return mCounterTextColor
    }

    fun getPrimaryColor(): Int {
        return primaryColor
    }

    fun getSecondaryColor(): Int {
        return secondaryColor
    }

    /* Characters Counter Getters */
    fun getMaxCharacters(): Int {
        return maxCharacters
    }

    fun getMinCharacters(): Int {
        return minCharacters
    }

    /* Other Getters */
    override fun isActivated(): Boolean {
        return activatedState
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    fun getIsResponsiveIconColor(): Boolean {
        return isResponsiveIconColor
    }

    fun getHasClearButton(): Boolean {
        return hasClearButton
    }

    fun getHasFocus(): Boolean {
        return hasFocus
    }

    companion object {
        /**
         * set EditText cursor color
         */
        //    private static void setCursorDrawableColor(EditText _editText, int _colorRes) {
        //
        //        try {
        //            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
        //            fCursorDrawableRes.setAccessible(true);
        //            int mCursorDrawableRes = fCursorDrawableRes.getInt(_editText);
        //            Field fEditor = TextView.class.getDeclaredField("mEditor");
        //            fEditor.setAccessible(true);
        //            Object editor = fEditor.get(_editText);
        //            Class<?> clazz = editor.getClass();
        //            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
        //            fCursorDrawable.setAccessible(true);
        //            Drawable[] drawables = new Drawable[2];
        //            drawables[0] = ContextCompat.getDrawable(_editText.getContext(), mCursorDrawableRes);
        //            drawables[1] = ContextCompat.getDrawable(_editText.getContext(), mCursorDrawableRes);
        //            drawables[0].setColorFilter(_colorRes, PorterDuff.Mode.SRC_IN);
        //            drawables[1].setColorFilter(_colorRes, PorterDuff.Mode.SRC_IN);
        //            fCursorDrawable.set(editor, drawables);
        //        } catch (Throwable ignored) {
        //        }
        //    }
        /**
         * return a lighter color
         *
         * @param factor percentage of light applied
         */
        private fun lighter(color: Int, factor: Float): Int {
            val red = ((Color.red(color) * (1 - factor) / 255 + factor) * 255).toInt()
            val green = ((Color.green(color) * (1 - factor) / 255 + factor) * 255).toInt()
            val blue = ((Color.blue(color) * (1 - factor) / 255 + factor) * 255).toInt()
            return Color.argb(Color.alpha(color), red, green, blue)
        }

        private fun isLight(color: Int): Boolean {
            return Math.sqrt(
                Color.red(color) * Color.red(color) * .241 + Color.green(color) * Color.green(color) * .691 + Color.blue(
                    color) * Color.blue(color) * .068) > 130
        }

        /**
         * adjust the alpha value of the color
         *
         * @return the color after adjustment
         */
        private fun adjustAlpha(color: Int, _toAlpha: Float): Int {
            val alpha = Math.round(255 * _toAlpha)
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            return Color.argb(alpha, red, green, blue)
        }
    }

    /**
     * 根据输入框内容，动态调整高度，以便于显示完整
     */
    fun adjustPanelHeightWrapContet(){
        panel?.apply {
            val lp = layoutParams
            lp.height =ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams = lp
        }
        editText?.apply {
            maxLines = lineCount
        }
    }
}