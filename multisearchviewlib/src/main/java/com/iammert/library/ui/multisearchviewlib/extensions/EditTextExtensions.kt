package com.iammert.library.ui.multisearchviewlib.extensions

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.text.InputFilter
import android.util.TypedValue
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StyleRes

fun EditText.onSearchAction(filter: Boolean = true, onSearchClicked: () -> Unit) {
    setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            if (filter) {
                onSearchClicked.invoke()
            }
            return@OnEditorActionListener true
        }
        false
    })
}

/**
 * Applies a style resource to an [EditText].
 *
 * [TextView.setTextAppearance] only handles text appearance attributes, so this function also
 * reads layout, focus, input and padding attributes. Only the attributes the style actually
 * defines are applied; everything else keeps the value from the layout.
 *
 * Supported attributes:
 * android:layout_width, android:layout_height, android:focusable, android:focusableInTouchMode,
 * android:enabled, android:hint, android:imeOptions, android:maxLength, android:textSize,
 * android:inputType, android:textColorHint, android:textColor, android:textColorLink,
 * android:textColorHighlight, android:textAllCaps, android:padding, android:paddingTop,
 * android:paddingBottom, android:paddingLeft, android:paddingRight, android:textStyle and
 * android:fontFamily.
 *
 * @param context context used to resolve the style
 * @param resId style resource, ignored when 0
 *
 * @author mertceyhan
 */
fun EditText.setStyle(context: Context, @StyleRes resId: Int) {
    if (resId == 0) {
        return
    }

    val typedArray = context.obtainStyledAttributes(resId, STYLE_ATTRIBUTES)
    try {
        applyStyle(typedArray)
    } finally {
        typedArray.recycle()
    }
}

private fun EditText.applyStyle(typedArray: TypedArray) {
    val filters = ArrayList<InputFilter>()
    var fontFamily: String? = null
    var textStyle: Int? = null
    var padding: Int? = null
    var paddingLeft = this.paddingLeft
    var paddingTop = this.paddingTop
    var paddingRight = this.paddingRight
    var paddingBottom = this.paddingBottom

    for (i in 0 until typedArray.indexCount) {
        val index = typedArray.getIndex(i)
        when (STYLE_ATTRIBUTES[index]) {
            android.R.attr.layout_width -> layoutParams?.width = typedArray.getLayoutDimension(index, layoutParams.width)
            android.R.attr.layout_height -> layoutParams?.height = typedArray.getLayoutDimension(index, layoutParams.height)
            android.R.attr.focusable -> isFocusable = typedArray.getBoolean(index, isFocusable)
            android.R.attr.focusableInTouchMode -> isFocusableInTouchMode = typedArray.getBoolean(index, isFocusableInTouchMode)
            android.R.attr.enabled -> isEnabled = typedArray.getBoolean(index, isEnabled)
            android.R.attr.hint -> hint = typedArray.getText(index)
            android.R.attr.imeOptions -> imeOptions = typedArray.getInt(index, imeOptions)
            android.R.attr.maxLength -> filters.add(InputFilter.LengthFilter(typedArray.getInt(index, Int.MAX_VALUE)))
            android.R.attr.textSize -> setTextSize(TypedValue.COMPLEX_UNIT_PX, typedArray.getDimensionPixelSize(index, 0).toFloat())
            android.R.attr.inputType -> inputType = typedArray.getInt(index, inputType)
            android.R.attr.textColorHint -> typedArray.getColorStateList(index)?.let { setHintTextColor(it) }
            android.R.attr.textColor -> typedArray.getColorStateList(index)?.let { setTextColor(it) }
            android.R.attr.textColorLink -> typedArray.getColorStateList(index)?.let { setLinkTextColor(it) }
            android.R.attr.textColorHighlight -> highlightColor = typedArray.getColor(index, highlightColor)
            android.R.attr.textAllCaps -> if (typedArray.getBoolean(index, false)) filters.add(InputFilter.AllCaps())
            android.R.attr.padding -> padding = typedArray.getDimensionPixelSize(index, 0)
            android.R.attr.paddingLeft -> paddingLeft = typedArray.getDimensionPixelSize(index, paddingLeft)
            android.R.attr.paddingTop -> paddingTop = typedArray.getDimensionPixelSize(index, paddingTop)
            android.R.attr.paddingRight -> paddingRight = typedArray.getDimensionPixelSize(index, paddingRight)
            android.R.attr.paddingBottom -> paddingBottom = typedArray.getDimensionPixelSize(index, paddingBottom)
            android.R.attr.fontFamily -> fontFamily = typedArray.getString(index)
            android.R.attr.textStyle -> textStyle = typedArray.getInt(index, Typeface.NORMAL)
        }
    }

    val allPadding = padding
    if (allPadding != null) {
        setPadding(allPadding, allPadding, allPadding, allPadding)
    } else {
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    if (filters.isNotEmpty()) {
        this.filters = filters.toTypedArray()
    }

    if (fontFamily != null || textStyle != null) {
        val style = textStyle ?: typeface?.style ?: Typeface.NORMAL
        typeface = if (fontFamily != null) {
            Typeface.create(fontFamily, style)
        } else {
            Typeface.create(typeface, style)
        }
    }
}

/** Attributes read by [setStyle]. obtainStyledAttributes requires them to be sorted. */
private val STYLE_ATTRIBUTES = intArrayOf(
    android.R.attr.layout_width,
    android.R.attr.layout_height,
    android.R.attr.focusable,
    android.R.attr.focusableInTouchMode,
    android.R.attr.enabled,
    android.R.attr.padding,
    android.R.attr.hint,
    android.R.attr.paddingTop,
    android.R.attr.paddingBottom,
    android.R.attr.paddingRight,
    android.R.attr.paddingLeft,
    android.R.attr.imeOptions,
    android.R.attr.maxLength,
    android.R.attr.textSize,
    android.R.attr.textStyle,
    android.R.attr.fontFamily,
    android.R.attr.inputType,
    android.R.attr.textColorHint,
    android.R.attr.textColorHighlight,
    android.R.attr.textColor,
    android.R.attr.textColorLink,
    android.R.attr.textAllCaps
).apply { sort() }
