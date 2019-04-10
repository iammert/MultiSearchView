package com.iammert.library.ui.multisearchviewlib.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.text.InputFilter
import android.util.TypedValue
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StyleRes
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrAllCaps
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrEnabled
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrFocusable
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrFocusableInTouchMode
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrFontFamily
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrHint
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrImeOptions
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrInputType
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrLayoutHeight
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrLayoutWidth
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrMaxLength
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrPadding
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrPaddingBottom
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrPaddingLeft
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrPaddingRight
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrPaddingTop
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrTextColor
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrTextColorHighlight
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrTextColorHint
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrTextColorLink
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrTextSize
import com.iammert.library.ui.multisearchviewlib.extensions.EditTextAttributes.Companion.attrTextStyle

fun EditText.onSearchAction(filter: Boolean = true, onSearchClicked: () -> Unit) {
    setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
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
 *
 * Better solution from TextView.setTextAppearance(context,resId) for EditText
 * @see TextView.setTextAppearance
 *
 * This function sets many attributes to EditText. These styles are as follows;
 * android.R.attr.layout_width
 * android.R.attr.layout_height
 * android.R.attr.focusable
 * android.R.attr.focusableInTouchMode
 * android.R.attr.enabled
 * android.R.attr.hint
 * android.R.attr.imeOptions
 * android.R.attr.maxLength
 * android.R.attr.textSize
 * android.R.attr.inputType
 * android.R.attr.textColorHint
 * android.R.attr.textColor
 * android.R.attr.textAllCaps
 * android.R.attr.padding
 * android.R.attr.paddingTop
 * android.R.attr.paddingBottom
 * android.R.attr.paddingRight
 * android.R.attr.paddingLeft
 * android.R.attr.textColorHighlight
 * android.R.attr.text
 * android.R.attr.textStyle
 * android.R.attr.fontFamily
 *
 * @param context Context parameter used for get typed array with context.obtainStyledAttributes
 * @param resId ResId parameter used for get the style attributes from resource
 *
 * @author mertceyhan
 */
fun EditText.setStyle(context: Context, @StyleRes resId: Int) {

    val attributes = EditTextAttributes.getAttributesList()

    val typedArray = context.obtainStyledAttributes(resId, attributes)

    readStyle(typedArray, attributes)
}

private fun EditText.readStyle(typedArray: TypedArray, attributes: IntArray) {

    val editTextAttributes = EditTextAttributes()

    val typedArraySize = typedArray.length().minus(1)

    for (index in 0..typedArraySize) {

        val attribute = attributes[index]

        when (attribute) {
            attrLayoutWidth -> {
                editTextAttributes.layoutWidth = typedArray.getLayoutDimension(index, -1)
            }
            attrLayoutHeight -> {
                editTextAttributes.layoutHeight = typedArray.getLayoutDimension(index, -1)
            }
            attrFocusable -> {
                editTextAttributes.focusable = typedArray.getBoolean(index, true)
            }
            attrFocusableInTouchMode -> {
                editTextAttributes.focusableInTouchMode = typedArray.getBoolean(index, true)
            }
            attrEnabled -> {
                editTextAttributes.enabled = typedArray.getBoolean(index, true)
            }
            attrHint -> {
                editTextAttributes.hint = typedArray.getString(index)
            }
            attrImeOptions -> {
                editTextAttributes.imeOptions = typedArray.getInt(index, -1)
            }
            attrMaxLength -> {
                editTextAttributes.maxLength = typedArray.getInt(index, -1)
            }
            attrTextSize -> {
                editTextAttributes.textSize = typedArray.getDimensionPixelSize(index, -1)
            }
            attrInputType -> {
                editTextAttributes.inputType = typedArray.getInt(index, EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES)
            }
            attrTextColorHint -> {
                editTextAttributes.textColorHint = typedArray.getColorStateList(index)
            }
            attrTextColor -> {
                editTextAttributes.textColor = typedArray.getColorStateList(index)
            }
            attrTextColorLink -> {
                editTextAttributes.textColorLink = typedArray.getColorStateList(index)

            }
            attrTextColorHighlight -> {
                editTextAttributes.textColorHighlight = typedArray.getColor(index, -1)
            }
            attrTextStyle -> {
                editTextAttributes.textStyle = typedArray.getInt(index, Typeface.NORMAL)
            }
            attrFontFamily -> {
                editTextAttributes.fontFamily = typedArray.getString(index) ?: ""
            }
            attrAllCaps -> {
                editTextAttributes.textAllCaps = typedArray.getBoolean(index, false)
            }
            attrPadding -> {
                editTextAttributes.padding = typedArray.getLayoutDimension(index, -1)
            }
            attrPaddingTop -> {
                editTextAttributes.paddingTop = typedArray.getLayoutDimension(index, 0)

            }
            attrPaddingBottom -> {
                editTextAttributes.paddingBottom = typedArray.getLayoutDimension(index, 0)

            }
            attrPaddingLeft -> {
                editTextAttributes.paddingLeft = typedArray.getLayoutDimension(index, 0)

            }
            attrPaddingRight -> {
                editTextAttributes.paddingRight = typedArray.getLayoutDimension(index, 0)

            }
        }
    }

    this.apply { applyStyle(editTextAttributes) }

    typedArray.recycle()
}

private fun EditText.applyStyle(editTextAttributes: EditTextAttributes) {

    val editTextFilters: ArrayList<InputFilter> = ArrayList()

    this.apply {
        if (editTextAttributes.layoutWidth != -1) {
            layoutParams.width = editTextAttributes.layoutWidth
        }
        if (editTextAttributes.layoutHeight != -1) {
            layoutParams.height = editTextAttributes.layoutHeight
        }
        if (editTextAttributes.focusable.not()) {
            isFocusable = editTextAttributes.focusable
        }
        if (editTextAttributes.enabled.not()) {
            isEnabled = editTextAttributes.enabled
        }
        if (editTextAttributes.imeOptions != -1) {
            imeOptions = editTextAttributes.imeOptions
        }
        if (editTextAttributes.maxLength != -1) {
            editTextFilters.add(InputFilter.LengthFilter(editTextAttributes.maxLength))
        }
        if (editTextAttributes.textSize != -1) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextAttributes.textSize.toFloat())
        }
        if (editTextAttributes.inputType != -1) {
            inputType = editTextAttributes.inputType
        }
        if (editTextAttributes.textAllCaps) {
            editTextFilters.add(InputFilter.AllCaps())
        }
        if (editTextAttributes.textColorHighlight != -1) {
            highlightColor = editTextAttributes.textColorHighlight
        }
        if (editTextAttributes.padding != -1) {
            setPaddingRelative(
                editTextAttributes.padding,
                editTextAttributes.padding,
                editTextAttributes.padding,
                editTextAttributes.padding
            )
        } else {
            setPaddingRelative(
                editTextAttributes.paddingLeft,
                editTextAttributes.paddingTop,
                editTextAttributes.paddingRight,
                editTextAttributes.paddingBottom
            )
        }
        if (editTextAttributes.textColorHint != null) {
            setHintTextColor(editTextAttributes.textColorHint)
        }
        if (editTextAttributes.textColor != null) {
            setTextColor(editTextAttributes.textColor)
        }
        if (editTextAttributes.textColorLink != null) {
            setLinkTextColor(editTextAttributes.textColorLink)
        }
        if (editTextAttributes.focusableInTouchMode.not()) {
            isFocusableInTouchMode = editTextAttributes.focusableInTouchMode
        }
        if (editTextAttributes.hint.isNullOrBlank().not()) {
            hint = editTextAttributes.hint
        }
        if (editTextFilters.isNullOrEmpty().not()) {
            filters = editTextFilters.toTypedArray()
        }
        val customTypeface = Typeface.create(editTextAttributes.fontFamily, editTextAttributes.textStyle)
        typeface = customTypeface
    }
}

private class EditTextAttributes {

    var padding = -1
    var textSize = -1
    var maxLength = -1
    var inputType = -1
    var imeOptions = -1
    var layoutWidth = -1
    var layoutHeight = -1
    var textColorHighlight = -1
    var paddingTop = 0
    var paddingLeft = 0
    var paddingRight = 0
    var paddingBottom = 0
    var textStyle = Typeface.NORMAL
    var fontFamily = ""
    var enabled = true
    var focusable = true
    var textAllCaps = false
    var focusableInTouchMode = true
    var hint: String? = null
    var textColorHint: ColorStateList? = null
    var textColor: ColorStateList? = null
    var textColorLink: ColorStateList? = null

    companion object {
        const val attrLayoutWidth = android.R.attr.layout_width
        const val attrLayoutHeight = android.R.attr.layout_height
        const val attrFocusable = android.R.attr.focusable
        const val attrFocusableInTouchMode = android.R.attr.focusableInTouchMode
        const val attrEnabled = android.R.attr.enabled
        const val attrHint = android.R.attr.hint
        const val attrImeOptions = android.R.attr.imeOptions
        const val attrMaxLength = android.R.attr.maxLength
        const val attrTextSize = android.R.attr.textSize
        const val attrInputType = android.R.attr.inputType
        const val attrTextColorHint = android.R.attr.textColorHint
        const val attrTextColor = android.R.attr.textColor
        const val attrAllCaps = android.R.attr.textAllCaps
        const val attrPadding = android.R.attr.padding
        const val attrPaddingTop = android.R.attr.paddingTop
        const val attrPaddingBottom = android.R.attr.paddingBottom
        const val attrPaddingRight = android.R.attr.paddingRight
        const val attrPaddingLeft = android.R.attr.paddingLeft
        const val attrTextColorHighlight = android.R.attr.textColorHighlight
        const val attrTextColorLink = android.R.attr.text
        const val attrTextStyle = android.R.attr.textStyle
        const val attrFontFamily = android.R.attr.fontFamily

        fun getAttributesList(): IntArray = intArrayOf(
            attrLayoutWidth,
            attrLayoutHeight,
            attrFocusable,
            attrFocusableInTouchMode,
            attrEnabled,
            attrPadding,
            attrHint,
            attrPaddingTop,
            attrPaddingBottom,
            attrPaddingRight,
            attrPaddingLeft,
            attrImeOptions,
            attrMaxLength,
            attrTextSize,
            attrTextStyle,
            attrFontFamily,
            attrInputType,
            attrTextColorHint,
            attrTextColorHighlight,
            attrTextColor,
            attrTextColorLink,
            attrAllCaps
        ).apply { sort() }
    }
}