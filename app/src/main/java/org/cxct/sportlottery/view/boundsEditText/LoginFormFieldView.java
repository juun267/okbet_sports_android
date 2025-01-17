package org.cxct.sportlottery.view.boundsEditText;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;

import org.cxct.sportlottery.R;


/**
 * Text Field Boxes
 * Created by CarbonylGroup on 2017/08/25
 */
@SuppressWarnings("unused")
public class LoginFormFieldView extends FrameLayout {

    /**
     * all the default colors to be used on light or dark themes.
     */
    public int DEFAULT_ERROR_COLOR;
    public int DEFAULT_PRIMARY_COLOR;
    public int DEFAULT_TEXT_COLOR;
    public int DEFAULT_DISABLED_TEXT_COLOR;
    public int DEFAULT_BG_COLOR;
    public int DEFAULT_FG_COLOR;

    /**
     * whether the text field is enabled. True by default.
     */
    protected boolean enabled;

    /**
     * labelText text at the top.
     */
    protected String labelText;

    /**
     * labelText text style
     */
    protected int labelTextStyle;

    /**
     * subLabelText text at the top.
     */
    protected String subLabelText;

    /**
     * hintText text at the top.
     */
    protected String hintText;

    /**
     * singleText text at the top.
     */
    protected String singleText;

    /**
     * singleText text style
     */
    protected int singleTextStyle;

    /**
     * singleText text color
     */
    protected int singleTextColor;

    /**
     * singleText text start padding
     */
    protected int singleTextStartPadding;

    /**
     * helper Label text at the bottom.
     */
    protected String helperText;

    /**
     * max characters count limit. 0 means no limit. 0 by default.
     */
    protected int maxCharacters;

    /**
     * min characters count limit. 0 means no limit. 0 by default.
     */
    protected int minCharacters;

    /**
     * the text color for the helperLabel text. DEFAULT_TEXT_COLOR by default.
     */
    protected int helperTextColor;

    /**
     * the text color for the counterLabel text. DEFAULT_TEXT_COLOR by default.
     */
    protected int mCounterTextColor;

    /**
     * the text color for when something is wrong (e.g. exceeding max characters, setError()).
     * DEFAULT_ERROR_COLOR by default.
     */
    protected int errorColor;

    /**
     * the color for the underline, the floating label text and the icon signifier tint when HAVING focus.
     * Current theme primary color by default.
     */
    protected int primaryColor;

    /**
     * the color for the underline, the floating label text and the icon signifier tints when NOT HAVING focus.
     * DEFAULT_TEXT_COLOR by default.
     */
    protected int secondaryColor;

    /**
     * the color for panel at the back. DEFAULT_BG_COLOR by default.
     */
    protected int panelBackgroundColor;

    /**
     * the resource ID of the icon signifier. 0 by default.
     */
    protected int iconSignifierResourceId;

    /**
     * the resource ID of the icon at the end. 0 by default.
     */
    protected int endIconResourceId;

    /**
     * whether the icon signifier will change its color when gaining or losing focus
     * as the label and the bottomLine do. True by default.
     */
    protected boolean isResponsiveIconColor;

    /**
     * whether to show the clear button at the end of the EditText. False by default.
     */
    protected boolean hasClearButton;

    /**
     * whether the EditText is having the focus. False by default.
     */
    protected boolean hasFocus;

    /**
     * Whether the label is fixed at top when there's a hint. False by default.
     */
    protected boolean alwaysShowHint;

    /**
     * whether the field uses a dense spacing between its elements.
     * Usually useful in a multi-field form. False by default.
     */
    protected boolean useDenseSpacing;

    /**
     * 被點擊後要不要顯示Title
     */
    protected boolean hideSelectedTitle = false;
    /**
     * 要不要显示副标题后的横线
     */
    protected boolean hideSubView = false;
    /**
     * whether the field uses a rtl direction for 'Persian (Farsi)' and 'Arabic' languages
     * False by default.
     */
    protected boolean rtl;

    protected int labelColor = -1;
    protected int labelTopMargin = -1;
    protected int ANIMATION_DURATION = 100;
    protected boolean onError = false;
    protected boolean activated = false;
    /**
     * See {@link #setManualValidateError(boolean)}
     */
    protected boolean isManualValidateError = false;

    protected View panel;
    protected View bottomLine;
    protected ViewGroup editTextLayout;
    protected ExtendedEditText editText;
    protected RelativeLayout rightShell;
    protected RelativeLayout bottomPart;
    protected RelativeLayout inputLayout;
    protected RelativeLayout mainLayout;
    protected AppCompatTextView helperLabel;
    protected AppCompatTextView counterLabel;
    protected AppCompatImageButton clearButton;
    protected AppCompatImageButton iconImageButton;
    protected AppCompatImageButton endIconImageButton;
    protected InputMethodManager inputMethodManager;
    protected SimpleTextChangedWatcher textChangeListener;
    private ColorDrawable mPasswordToggleDummyDrawable;
    private Drawable mOriginalEditTextEndDrawable;

    public LoginFormFieldView(Context context) {

        super(context);
        init();
    }

    public LoginFormFieldView(Context context, AttributeSet attrs) {

        super(context, attrs);
        init();
        handleAttributes(context, attrs);
    }

    public LoginFormFieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        handleAttributes(context, attrs);
    }

    protected void init() {
        initDefaultColor();
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    protected void initDefaultColor() {

        Resources.Theme theme = getContext().getTheme();
        TypedArray themeArray;

        /* Get Default Error Color From Theme */
        DEFAULT_ERROR_COLOR = ContextCompat.getColor(getContext(), R.color.color_F75452_E23434);

        /* Get Default Background Color From Theme */
        themeArray = theme.obtainStyledAttributes(new int[]{android.R.attr.colorForeground});
        DEFAULT_BG_COLOR = adjustAlpha(themeArray.getColor(0, 0), 0.06f);

        /* Get Default Foreground Color From Theme */
        themeArray = theme.obtainStyledAttributes(new int[]{android.R.attr.colorBackground});
        DEFAULT_FG_COLOR = themeArray.getColor(0, 0);

        /* Get Default Primary Color From Theme */
        themeArray = theme.obtainStyledAttributes(new int[]{R.attr.colorPrimary});
        if (isLight(DEFAULT_BG_COLOR))
            DEFAULT_PRIMARY_COLOR = lighter(themeArray.getColor(0, 0), 0.2f);
        else DEFAULT_PRIMARY_COLOR = themeArray.getColor(0, 0);

        /* Get Default Text Color From Theme */
        themeArray = theme.obtainStyledAttributes(new int[]{android.R.attr.textColorTertiary});
        DEFAULT_TEXT_COLOR = themeArray.getColor(0, 0);

        /* Get Default Disabled Text Color From Theme */
        themeArray = theme.obtainStyledAttributes(new int[]{android.R.attr.disabledAlpha});
        float disabledAlpha = themeArray.getFloat(0, 0);
        themeArray = theme.obtainStyledAttributes(new int[]{android.R.attr.textColorTertiary});
        DEFAULT_DISABLED_TEXT_COLOR = adjustAlpha(themeArray.getColor(0, 0), disabledAlpha);

        themeArray.recycle();
    }

    protected ExtendedEditText findEditTextChild() {

        if (getChildCount() > 0 && getChildAt(0) instanceof ExtendedEditText)
            return (ExtendedEditText) getChildAt(0);
        return null;
    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();
        initViews();
        triggerSetters();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {

            /* match_parent or specific value */
            this.mainLayout.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            this.inputLayout.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            this.editTextLayout.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

        } else if (widthMode == MeasureSpec.AT_MOST) {

            /* wrap_content */
            this.mainLayout.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            this.inputLayout.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            this.editTextLayout.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        if (heightMode == MeasureSpec.EXACTLY) {

            /* match_parent or specific value */
            this.mainLayout.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            this.panel.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            this.rightShell.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

            ((RelativeLayout.LayoutParams) this.bottomPart.getLayoutParams())
                    .addRule(RelativeLayout.BELOW, 0);
            ((RelativeLayout.LayoutParams) this.bottomPart.getLayoutParams())
                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            ((RelativeLayout.LayoutParams) this.panel.getLayoutParams())
                    .addRule(RelativeLayout.ABOVE, R.id.text_field_boxes_bottom);

        } else if (heightMode == MeasureSpec.AT_MOST) {

            /* wrap_content */
            this.mainLayout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            this.panel.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            this.rightShell.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

            ((RelativeLayout.LayoutParams) this.bottomPart.getLayoutParams())
                    .addRule(RelativeLayout.BELOW, R.id.text_field_boxes_panel);
            ((RelativeLayout.LayoutParams) this.bottomPart.getLayoutParams())
                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            ((RelativeLayout.LayoutParams) this.panel.getLayoutParams())
                    .addRule(RelativeLayout.ABOVE, 0);
        }

        updateClearAndEndIconLayout();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void updateClearAndEndIconLayout() {

        if ((endIconImageButton != null && endIconImageButton.getDrawable() != null) || hasClearButton) {

            int clearButtonW = hasClearButton ? clearButton.getMeasuredWidth() : 0;
            int endIconW = (endIconImageButton != null && endIconImageButton.getDrawable() != null) ?
                    endIconImageButton.getMeasuredWidth() : 0;
            if (mPasswordToggleDummyDrawable == null)
                mPasswordToggleDummyDrawable = new ColorDrawable();


            // We add a fake drawableRight to EditText so it will have padding on the right side and text will not go
            // under the icons.
            if (!rtl)
                mPasswordToggleDummyDrawable.setBounds(0, 0, endIconW + clearButtonW, 0);

            final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
            // Store the user defined end compound drawable so that we can restore it later
            if (compounds[2] != mPasswordToggleDummyDrawable) {
                mOriginalEditTextEndDrawable = compounds[2];
            }
            TextViewCompat.setCompoundDrawablesRelative(editText, compounds[0], compounds[1],
                    mPasswordToggleDummyDrawable, compounds[3]);

        } else {

            if (mPasswordToggleDummyDrawable != null) {
                // Make sure that we remove the dummy end compound drawable if it exists, and then
                // clear it
                final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
                if (compounds[2] == mPasswordToggleDummyDrawable) {
                    TextViewCompat.setCompoundDrawablesRelative(editText, compounds[0],
                            compounds[1], mOriginalEditTextEndDrawable, compounds[3]);
                    mPasswordToggleDummyDrawable = null;
                }
            }
        }
        if (this.hideSubView){
            this.editTextLayout.setPadding(16,0,0,0);
        }
    }

    private void initViews() {

        this.editText = findEditTextChild();
        if (editText == null) return;
        this.addView(LayoutInflater.from(getContext()).inflate(R.layout.view_login_edittext_layout,this, false));
        removeView(this.editText);

        this.editText.setBackgroundColor(Color.TRANSPARENT);
        this.editText.setDropDownBackgroundDrawable(new ColorDrawable(DEFAULT_FG_COLOR));
        this.editText.setMinimumWidth(10);
        this.inputLayout = this.findViewById(R.id.text_field_boxes_input_layout);
        this.mainLayout = findViewById(R.id.rl_main);
        this.panel = findViewById(R.id.text_field_boxes_panel);
        this.rightShell = findViewById(R.id.text_field_boxes_right_shell);
        this.bottomPart = findViewById(R.id.text_field_boxes_bottom);
        this.bottomLine = findViewById(R.id.bottom_line);

        this.clearButton = findViewById(R.id.text_field_boxes_clear_button);
        this.endIconImageButton = findViewById(R.id.text_field_boxes_end_icon_button);
        this.helperLabel = findViewById(R.id.text_field_boxes_helper);
        this.counterLabel = findViewById(R.id.text_field_boxes_counter);
        this.iconImageButton = findViewById(R.id.text_field_boxes_imageView);
        this.editTextLayout = findViewById(R.id.text_field_boxes_editTextLayout);
        this.inputLayout.addView(this.editText);
        this.clearButton.setColorFilter(DEFAULT_TEXT_COLOR);
        this.clearButton.setAlpha(0.35f);
        //this.endIconImageButton.setColorFilter(DEFAULT_TEXT_COLOR);
        //this.endIconImageButton.setAlpha(0.54f);

        initOnClick();

        // Have to update useDenseSpacing then the dimensions before the first activation
        setUseDenseSpacing(this.useDenseSpacing);
        updateDimens(this.useDenseSpacing);
        if (!this.editText.getText().toString().isEmpty() || this.hasFocus)
            activate(false);


    }

    public void setBottomLineLeftMargin(int margin) {
        ((MarginLayoutParams) bottomLine.getLayoutParams()).leftMargin = margin;
    }

    private void initOnClick() {

        final FrameLayout mainBody = this;

        this.panel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivated()) activate(true);
                setHasFocus(true);
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                mainBody.performClick();
            }
        });

        this.iconImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivated()) activate(true);
                setHasFocus(true);
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                mainBody.performClick();
            }
        });

        this.editText.setDefaultOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) setHasFocus(true);
                else setHasFocus(false);
            }
        });

        this.editText.addTextChangedListener(new TextWatcher() {

            private String lastValue = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!activated && !editable.toString().isEmpty()) activate(true);
                if (activated && editable.toString().isEmpty() && !hasFocus) deactivate();
                if (isManualValidateError) {
                    updateCounterText(false);
                } else {
                    validate(); //this will call updateCounterText(true);
                }

                // Only trigger simple watcher when the String actually changed

                if (!lastValue.equals(editable.toString())) {
                    lastValue = editable.toString();
                    if (textChangeListener != null) {
                        textChangeListener.onTextChanged(editable.toString(), onError);
                    }
                }

            }
        });

        this.clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });

        this.editText.setDefaultOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    setHasFocus(true);
                    inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    mainBody.performClick();
                } else setHasFocus(false);
            }
        });
    }

    protected void handleAttributes(Context context, AttributeSet attrs) {

        try {

            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TextFieldBoxes);

            /* Texts */
            this.labelText = styledAttrs.getString(R.styleable.TextFieldBoxes_labelText)
                    == null ? "" : styledAttrs.getString(R.styleable.TextFieldBoxes_labelText);
            this.labelTextStyle = styledAttrs.getInt(R.styleable.TextFieldBoxes_labelTextStyle, Typeface.BOLD);
            this.subLabelText = styledAttrs.getString(R.styleable.TextFieldBoxes_subLabelText)
                    == null ? "" : styledAttrs.getString(R.styleable.TextFieldBoxes_subLabelText);
            this.hintText = styledAttrs.getString(R.styleable.TextFieldBoxes_hintText)
                    == null ? "" : styledAttrs.getString(R.styleable.TextFieldBoxes_hintText);
            this.singleText = styledAttrs.getString(R.styleable.TextFieldBoxes_singleText)
                    == null ? "" : styledAttrs.getString(R.styleable.TextFieldBoxes_singleText);
            this.singleTextStyle = styledAttrs.getInt(R.styleable.TextFieldBoxes_singleTextStyle, Typeface.BOLD);
            this.singleTextColor = styledAttrs.getInt(R.styleable.TextFieldBoxes_singleTextColor, DEFAULT_TEXT_COLOR);
            this.singleTextStartPadding = styledAttrs.getInt(R.styleable.TextFieldBoxes_singleTextStartPadding, 0);
            this.helperText = styledAttrs.getString(R.styleable.TextFieldBoxes_helperText)
                    == null ? "" : styledAttrs.getString(R.styleable.TextFieldBoxes_helperText);

            /* Colors */
            this.helperTextColor = styledAttrs
                    .getInt(R.styleable.TextFieldBoxes_helperTextColor, DEFAULT_TEXT_COLOR);
            this.mCounterTextColor = styledAttrs
                    .getInt(R.styleable.TextFieldBoxes_mCounterTextColor, DEFAULT_TEXT_COLOR);
            this.errorColor = styledAttrs
                    .getInt(R.styleable.TextFieldBoxes_errorColor, DEFAULT_ERROR_COLOR);
            this.primaryColor = styledAttrs
                    .getColor(R.styleable.TextFieldBoxes_primaryColor, DEFAULT_PRIMARY_COLOR);
            this.secondaryColor = styledAttrs
                    .getColor(R.styleable.TextFieldBoxes_secondaryColor, DEFAULT_TEXT_COLOR);
//            this.panelBackgroundColor = styledAttrs
//                    .getColor(R.styleable.TextFieldBoxes_panelBackgroundColor, DEFAULT_BG_COLOR);

            /* Characters counter */
            this.maxCharacters = styledAttrs.getInt(R.styleable.TextFieldBoxes_maxCharacters, 0);
            this.minCharacters = styledAttrs.getInt(R.styleable.TextFieldBoxes_minCharacters, 0);

            /* Others */
            this.isManualValidateError = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_manualValidateError, false);
            this.hideSubView = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_hideSubView,false);
            this.enabled = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_enabled, true);
            this.iconSignifierResourceId = styledAttrs.
                    getResourceId(R.styleable.TextFieldBoxes_iconSignifier, 0);
            this.endIconResourceId = styledAttrs.
                    getResourceId(R.styleable.TextFieldBoxes_endIcon, 0);
            this.isResponsiveIconColor = styledAttrs
                    .getBoolean(R.styleable.TextFieldBoxes_isResponsiveIconColor, true);
            this.hasClearButton = styledAttrs
                    .getBoolean(R.styleable.TextFieldBoxes_hasClearButton, false);
            this.hasFocus = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_hasFocus, false);
            this.alwaysShowHint = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_alwaysShowHint, false);
            this.useDenseSpacing = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_useDenseSpacing, false);
            this.rtl = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_rtl, false);
            this.hideSelectedTitle = styledAttrs.getBoolean(R.styleable.TextFieldBoxes_hideSelectedTitle, false);

            styledAttrs.recycle();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSimpleTextChangeWatcher(SimpleTextChangedWatcher textChangeListener) {
        this.textChangeListener = textChangeListener;
    }

    /**
     * lower the labelText labelText Label when there is no text at losing focus
     */
    protected void deactivate() {
        //this.mainLayout.setBackgroundResource(R.drawable.bg_bounds_edittext);
        this.bottomLine.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.color_E3E8EE));
        if (this.editText.getText().toString().isEmpty()) {

            if (this.alwaysShowHint && !this.editText.getHint().toString().isEmpty()) {

                // If alwaysShowHint, and the hint is not empty,
                // keep the label on the top and EditText visible.
                this.editTextLayout.setAlpha(1f);

            } else {

            }

            if (this.editText.hasFocus()) {
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                this.editText.clearFocus();
            }
        }
        this.activated = false;
    }

    /**
     * raise the labelText labelText Label when gaining focus
     */
    public void activate(boolean animated) {

        this.editText.setAlpha(1);
        //final boolean keepHint = this.alwaysShowHint && !this.editText.getHint().toString().isEmpty();
        if (animated) {
            //this.mainLayout.setBackgroundResource(R.drawable.bg_bounds_edittext_h);
            this.bottomLine.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.color_317FFF_0760D4));
            ViewCompat.animate(this.editTextLayout)
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION);

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

            //驗證碼要特殊處理 by bill
            if (hideSelectedTitle) {
                labelTopMargin = 0;
            }

        } else {
            this.editTextLayout.setAlpha(1f);

        }
        activated = true;
    }

    protected void makeCursorBlink() {

        CharSequence hintCache = this.editText.getHint();
        this.editText.setHint(" ");
        this.editText.setHint(hintCache);
    }

    /**
     * set the color of the labelText Label, EditText cursor, icon signifier and the underline
     *
     * @param colorRes color resource
     */
    protected void setHighlightColor(int colorRes) {

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
     * Set this value to true to validate only when {@link #validate()} is called.
     *
     * @param isManualValidateError the new value
     */
    protected void setManualValidateError(boolean isManualValidateError) {
        this.isManualValidateError = isManualValidateError;
    }
    /**
     * Update the onError state of this component
     *
     * @return true if valid (the inverse value of onError)
     */
    public boolean validate() { //Reverted: "validateError" has the opposite meaning and is incorrect and does not follow conventions
        removeError();
        updateCounterText(true);
        if (onError) {
            setError(null, false);
        }
        return !onError;
    }

    /**
     * @deprecated Pseudonym for {@link #validate()} to provide legacy support for
     * a bad PR.
     * <p>
     * Note: This does NOT validate that there is an error, it does the opposite
     */
    @Deprecated
    public boolean validateError() {
        return validate();
    }

    /**
     * check if the TextFieldBox should use a dense spacing,
     * then change the layout dimens accordingly
     */
    protected void updateDimens(boolean useDenseSpacing) {

        final Resources res = getContext().getResources();

        /* Floating Label */

        /* EditText Layout */

        /* End Icon */
        this.endIconImageButton.setMinimumHeight(
                res.getDimensionPixelOffset(
                        useDenseSpacing ?
                                R.dimen.end_icon_min_height :
                                R.dimen.dense_end_icon_min_height
                )
        );
        this.endIconImageButton.setMinimumWidth(
                res.getDimensionPixelOffset(
                        useDenseSpacing ?
                                R.dimen.end_icon_min_width :
                                R.dimen.dense_end_icon_min_width
                )
        );

        /* Clear Icon */
        this.clearButton.setMinimumHeight(
                res.getDimensionPixelOffset(
                        useDenseSpacing ?
                                R.dimen.clear_button_min_height :
                                R.dimen.dense_clear_button_min_height
                )
        );
        this.clearButton.setMinimumWidth(
                res.getDimensionPixelOffset(
                        useDenseSpacing ?
                                R.dimen.clear_button_min_width :
                                R.dimen.dense_clear_button_min_width
                )
        );

        /* Bottom View */
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.bottomPart.getLayoutParams();
        lp.topMargin = res.getDimensionPixelOffset(
                useDenseSpacing ?
                        R.dimen.dense_bottom_marginTop :
                        R.dimen.bottom_marginTop
        );
        this.bottomPart.setLayoutParams(lp);

        /* EditText */
        this.editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(
                useDenseSpacing ?
                        R.dimen.dense_edittext_text_size :
                        R.dimen.edittext_text_size
        ));

        this.requestLayout();
    }

    /**
     * check if the character count meets the upper or lower limits,
     * <p>
     * if performValidation and exceeds limit, setCounterError()
     * otherwise removeCounterError()
     * <p>
     * <p>
     *
     * @param performValidation - true if error state should be applied or removed by this calls See {@link
     *                          #setManualValidateError(boolean)} </p> <i>NOTE: SPACE AND LINE FEED WILL NOT COUNT</i>
     */
    protected void updateCounterText(boolean performValidation) {

        /* Show clear button if there is anything */
        if (hasClearButton) {
            if (this.editText.getText().toString().length() == 0) {
                showClearButton(false);
            } else {
                showClearButton(true);
            }
        }

        /* Don't Count Space & Line Feed */
        int length = this.editText.getText().toString().replaceAll(" ", "")
                .replaceAll("\n", "").length();
        String lengthStr = Integer.toString(length) + " / ";
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
    protected void updateBottomViewVisibility() {

        if (this.helperLabel.getText().toString().isEmpty() &&
                this.counterLabel.getText().toString().isEmpty())
            //this.bottomPart.setVisibility(View.GONE);
            this.helperLabel.setVisibility(View.GONE);
        else this.helperLabel.setVisibility(View.VISIBLE);
    }

    /**
     * set highlight color and counter Label text color to error color
     */
    protected void setCounterError() {
        this.onError = true;
        setHighlightColor(this.errorColor);
        this.counterLabel.setTextColor(this.errorColor);
    }

    /**
     * set highlight color to primary color if having focus,
     * otherwise set to secondaryColor
     * set counterLabel Label text color to DEFAULT_TEXT_COLOR
     */
    protected void removeCounterError() {
        this.onError = false;
        if (this.hasFocus) setHighlightColor(this.primaryColor);
        else setHighlightColor(this.secondaryColor);
        this.counterLabel.setTextColor(this.mCounterTextColor);
    }

    /**
     * set highlight color and helperLabel Label text color to errorColor
     * set helperLabel Label text to error message
     *
     * @param errorText optional error message
     * @param giveFocus whether the field will gain focus when set error on
     */
    public void setError(@Nullable String errorText, boolean giveFocus) {
        if (this.enabled && errorText != null && !errorText.isEmpty()) {
            //this.mainLayout.setBackgroundResource(R.drawable.bg_bounds_edittext_error);
            this.bottomLine.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.color_E44438));
            this.onError = true;
            //activate(true);
            setHighlightColor(this.errorColor);
            this.helperLabel.setTextColor(this.errorColor);
            if (giveFocus) setHasFocus(true);
            makeCursorBlink();
        }

        this.helperLabel.setText(errorText);
        updateBottomViewVisibility();

        if (errorText == null && hasFocus) {
            activate(true);
        } else if (errorText == null) {
            deactivate();
        }

    }

    /**
     * set highlight to primaryColor if having focus,
     * otherwise set to secondaryColor
     * set helperLabel Label text color to DEFAULT_TEXT_COLOR
     * <p>
     * <i>NOTE: WILL BE CALLED WHEN THE EDITTEXT CHANGES
     * UNLESS YOU {@link #setManualValidateError(boolean)} TO TRUE</i>
     */
    public void removeError() {
        this.onError = false;
        //this.mainLayout.setBackgroundResource(R.drawable.bg_bounds_edittext_h);
        this.bottomLine.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.color_317FFF_0760D4));
        if (this.hasFocus) setHighlightColor(this.primaryColor);
        else setHighlightColor(this.secondaryColor);
        this.helperLabel.setTextColor(this.helperTextColor);
        this.helperLabel.setText(this.helperText);
        updateBottomViewVisibility();
    }

    protected void showClearButton(boolean show) {

        if (show) this.clearButton.setVisibility(View.VISIBLE);
        else this.clearButton.setVisibility(View.GONE);
    }

    private void triggerSetters() {
        setHelperText(this.helperText);
        setHintText(this.hintText);
        /* Colors */
        setHelperTextColor(this.helperTextColor);
        setmCounterTextColor(this.mCounterTextColor);
        setErrorColor(this.errorColor);
        setPrimaryColor(this.primaryColor);
        setSecondaryColor(this.secondaryColor);
        //setPanelBackgroundColor(this.panelBackgroundColor);

        /* Characters counter */
        setMaxCharacters(this.maxCharacters);
        setMinCharacters(this.minCharacters);

        /* Others */
        setEnabled(this.enabled);
        setIconSignifier(this.iconSignifierResourceId);
        setEndIcon(this.endIconResourceId);
        setIsResponsiveIconColor(this.isResponsiveIconColor);
        setHasClearButton(this.hasClearButton);
        setHasFocus(this.hasFocus);
        setAlwaysShowHint(this.alwaysShowHint);
        updateCounterText(!isManualValidateError);
        updateBottomViewVisibility();
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
        editText.setHint(hintText);
    }
    public void setHelperText(String helperText) {

        this.helperText = helperText;
        this.helperLabel.setText(this.helperText);
    }

    /* Color Setters */
    public void setHelperTextColor(int colorRes) {

        this.helperTextColor = colorRes;
        this.helperLabel.setTextColor(this.helperTextColor);
    }

    public void setmCounterTextColor(int colorRes) {

        this.mCounterTextColor = colorRes;
        this.counterLabel.setTextColor(this.mCounterTextColor);
    }

    public void setErrorColor(int colorRes) {
        this.errorColor = colorRes;
    }

    /**
     * <i>NOTE: the color will automatically be made lighter by 20% if it's on the DARK theme</i>
     */
    public void setPrimaryColor(int colorRes) {

        this.primaryColor = colorRes;
        if (this.hasFocus) setHighlightColor(this.primaryColor);
    }

    public void setSecondaryColor(int colorRes) {

        this.secondaryColor = colorRes;
        if (!this.hasFocus) setHighlightColor(this.secondaryColor);
    }

    public void setPanelBackgroundColor(int colorRes) {

        this.panelBackgroundColor = colorRes;
//        this.panel.getBackground()
//                .setColorFilter(new PorterDuffColorFilter(colorRes, PorterDuff.Mode.SRC_IN));
    }

    /* Characters Counter Setters */
    public void setMaxCharacters(int maxCharacters) {
        this.maxCharacters = maxCharacters;
        updateCounterText(!isManualValidateError);
    }

    /**
     * remove the max character count limit by setting it to 0
     */
    public void removeMaxCharacters() {
        this.maxCharacters = 0;
        updateCounterText(!isManualValidateError);
    }

    public void setMinCharacters(int minCharacters) {
        this.minCharacters = minCharacters;
        updateCounterText(!isManualValidateError);
    }

    /**
     * remove the min character count limit by setting it to 0
     */
    public void removeMinCharacters() {
        this.minCharacters = 0;
        updateCounterText(!isManualValidateError);
    }

    /* Other Setters */
    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
        if (this.enabled) {
            this.editText.setEnabled(true);
            this.editText.setFocusableInTouchMode(true);
            this.editText.setFocusable(true);
            this.bottomPart.setVisibility(View.VISIBLE);
            this.helperLabel.setVisibility(View.VISIBLE);
            this.counterLabel.setVisibility(View.GONE);
            this.panel.setEnabled(true);
            this.iconImageButton.setEnabled(true);
            this.iconImageButton.setClickable(true);
            setHighlightColor(secondaryColor);
            updateCounterText(!isManualValidateError);

        } else {
            removeError();
            setHasFocus(false);
            this.editText.setEnabled(false);
            this.editText.setFocusableInTouchMode(false);
            this.editText.setFocusable(false);
            this.iconImageButton.setClickable(false);
            this.iconImageButton.setEnabled(false);
            this.bottomPart.setVisibility(View.INVISIBLE);
            this.helperLabel.setVisibility(View.INVISIBLE);
            this.counterLabel.setVisibility(View.GONE);
            this.panel.setEnabled(false);
            setHighlightColor(DEFAULT_DISABLED_TEXT_COLOR);
        }
    }

    public void setIconSignifier(int resourceID) {

        this.iconSignifierResourceId = resourceID;
        if (this.iconSignifierResourceId != 0) {
            this.iconImageButton.setImageResource(this.iconSignifierResourceId);
            this.iconImageButton.setVisibility(View.VISIBLE);
        } else removeIconSignifier();
    }

    public void setIconSignifier(Drawable drawable) {

        removeIconSignifier();
        this.iconImageButton.setImageDrawable(drawable);
        this.iconImageButton.setVisibility(View.VISIBLE);

    }

    /**
     * remove the icon by setting the visibility of the image view to View.GONE
     */
    public void removeIconSignifier() {

        this.iconSignifierResourceId = 0;
        this.iconImageButton.setVisibility(View.GONE);
    }

    public void setEndIcon(int resourceID) {

        this.endIconResourceId = resourceID;
        if (this.endIconResourceId != 0) {
            this.endIconImageButton.setImageResource(this.endIconResourceId);
            this.endIconImageButton.setVisibility(View.VISIBLE);
        } else removeEndIcon();

        updateClearAndEndIconLayout();
    }

    public void setEndIcon(Drawable drawable) {

        removeEndIcon();
        this.endIconImageButton.setImageDrawable(drawable);
        this.endIconImageButton.setVisibility(View.VISIBLE);

        updateClearAndEndIconLayout();
    }

    /**
     * remove the end icon by setting the visibility of the end image view to View.GONE
     */
    public void removeEndIcon() {
        this.endIconResourceId = 0;
        this.endIconImageButton.setImageDrawable(null);
        this.endIconImageButton.setVisibility(View.GONE);
        updateClearAndEndIconLayout();
    }

    /**
     * set whether the icon signifier will change its color when gaining or losing focus
     * as the label and the bottomLine do.
     *
     * @param isResponsiveIconColor if true, the icon's color will always be HighlightColor (the same as the bottomLine)
     *                              if false, the icon will always be in primaryColor
     */
    public void setIsResponsiveIconColor(boolean isResponsiveIconColor) {

        this.isResponsiveIconColor = isResponsiveIconColor;
        if (this.isResponsiveIconColor) {
            if (this.hasFocus) {
                this.iconImageButton.setColorFilter(primaryColor);
                this.iconImageButton.setAlpha(1f);
            } else {
                this.iconImageButton.setColorFilter(secondaryColor);
                this.iconImageButton.setAlpha(0.54f);
            }
        } else {
            this.iconImageButton.setColorFilter(primaryColor);
            this.iconImageButton.setAlpha(1f);
        }
    }

    public void setHasClearButton(boolean hasClearButton) {
        this.hasClearButton = hasClearButton;
        showClearButton(hasClearButton);
        updateClearAndEndIconLayout();
    }

    /**
     * set if the EditText is having focus
     *
     * @param hasFocus gain focus if true, lose if false
     */
    public void setHasFocus(boolean hasFocus) {

        this.hasFocus = hasFocus;
        if (this.hasFocus) {
            activate(true);
            this.editText.requestFocus();
            makeCursorBlink();

            /* if there's an error, keep the error color */
            if (!this.onError && this.enabled) setHighlightColor(this.primaryColor);

        } else {
            deactivate();

            /* if there's an error, keep the error color */
            if (!this.onError && this.enabled) setHighlightColor(this.secondaryColor);
        }
    }

    /**
     * only gain or lose focus at TextFieldBoxes or not by params focusEditText
     * @param hasFocus gain focus if true, lose if false
     * @param focusEditText only focus TextFieldBoxes if false, both TextFieldBoxes and EditText if true
     */
    public void setHasFocus(boolean hasFocus, boolean focusEditText) {

        this.hasFocus = hasFocus;
        if (this.hasFocus) {
            activate(true);
            if (focusEditText) {
                this.editText.requestFocus();
            }
            makeCursorBlink();

            /* if there's an error, keep the error color */
            if (!this.onError && this.enabled) setHighlightColor(this.primaryColor);

        } else {
            deactivate();

            /* if there's an error, keep the error color */
            if (!this.onError && this.enabled) setHighlightColor(this.secondaryColor);
        }
    }

    public void setAlwaysShowHint(boolean alwaysShowHint) {
        this.alwaysShowHint = alwaysShowHint;
    }

    public void setUseDenseSpacing(boolean useDenseSpacing) {
        this.useDenseSpacing = useDenseSpacing;
    }

    /* Text Getters */
    public String getLabelText() {
        return this.labelText;
    }

    public String getHelperText() {
        return this.helperText;
    }

    public String getCounterText() {
        return this.counterLabel.getText().toString();
    }

    /* Color Getters */
    public int getHelperTextColor() {
        return this.helperTextColor;
    }

    public int getmCounterTextColor() {
        return this.mCounterTextColor;
    }

    public int getErrorColor() {
        return this.errorColor;
    }

    public int getPrimaryColor() {
        return this.primaryColor;
    }

    public int getSecondaryColor() {
        return this.secondaryColor;
    }

    public int getPanelBackgroundColor() {
        return this.panelBackgroundColor;
    }

    /* Characters Counter Getters */
    public int getMaxCharacters() {
        return this.maxCharacters;
    }

    public int getMinCharacters() {
        return this.minCharacters;
    }

    /* View Getters */
    public View getPanel() {
        return this.panel;
    }


    public AppCompatTextView getHelperLabel() {
        return this.helperLabel;
    }

    public View getBottomPart() {
        return bottomPart;
    }

    public AppCompatTextView getCounterLabel() {
        return this.counterLabel;
    }

    public AppCompatImageButton getIconImageButton() {
        return this.iconImageButton;
    }

    public AppCompatImageButton getEndIconImageButton() {
        return this.endIconImageButton;
    }

    /* Other Getters */
    public boolean isActivated() {
        return this.activated;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isOnError() {
        return this.onError;
    }

    public int getIconSignifierResourceId() {
        return this.iconSignifierResourceId;
    }

    public int getEndIconResourceId() {
        return this.endIconResourceId;
    }

    public boolean getIsResponsiveIconColor() {
        return this.isResponsiveIconColor;
    }

    public boolean getHasClearButton() {
        return this.hasClearButton;
    }

    public boolean getHasFocus() {
        return this.hasFocus;
    }

    public boolean getAlwaysShowHint() {
        return this.alwaysShowHint;
    }

    public boolean getUseDenseSpacing() {
        return this.useDenseSpacing;
    }

    /**
     * set EditText cursor color
     */
//    protected static void setCursorDrawableColor(EditText _editText, int _colorRes) {
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
    protected static int lighter(int color, float factor) {

        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

    protected static boolean isLight(int color) {
        return Math.sqrt(
                Color.red(color) * Color.red(color) * .241 +
                        Color.green(color) * Color.green(color) * .691 +
                        Color.blue(color) * Color.blue(color) * .068) > 130;
    }

    /**
     * adjust the alpha value of the color
     *
     * @return the color after adjustment
     */
    protected static int adjustAlpha(int color, float _toAlpha) {

        int alpha = Math.round(255 * _toAlpha);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
