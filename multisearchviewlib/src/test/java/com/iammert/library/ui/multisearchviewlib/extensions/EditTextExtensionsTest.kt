package com.iammert.library.ui.multisearchviewlib.extensions

import android.graphics.Color
import android.widget.EditText
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iammert.library.ui.multisearchviewlib.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditTextExtensionsTest {

    private lateinit var editText: EditText

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        editText = EditText(context)
        FrameLayout(context).addView(editText)
        editText.setPadding(10, 20, 30, 40)
        editText.hint = "Search"
    }

    @Test
    fun styleWithoutPaddingKeepsExistingPadding() {
        editText.setStyle(editText.context, R.style.MsvTest_TextColorOnly)

        assertEquals(Color.RED, editText.currentTextColor)
        assertEquals(10, editText.paddingLeft)
        assertEquals(20, editText.paddingTop)
        assertEquals(30, editText.paddingRight)
        assertEquals(40, editText.paddingBottom)
        assertEquals("Search", editText.hint.toString())
    }

    @Test
    fun styleAppliesDefinedAttributes() {
        editText.setStyle(editText.context, R.style.MsvTest_Full)

        assertEquals("Find", editText.hint.toString())
        assertEquals(Color.GREEN, editText.linkTextColors.defaultColor)
        assertEquals(5, editText.paddingLeft)
        assertEquals(20, editText.paddingTop)

        editText.setText("abcdef")
        assertEquals("ABCD", editText.text.toString())
    }

    @Test
    fun zeroStyleIsIgnored() {
        editText.setStyle(editText.context, 0)

        assertEquals(10, editText.paddingLeft)
        assertEquals("Search", editText.hint.toString())
    }
}
