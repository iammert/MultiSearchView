package com.iammert.library.ui.multisearchviewlib.extensions

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView

fun EditText.onSearchAction(filter: Boolean = true, onSearchClicked: () -> Unit) {
    setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            if(filter){
                onSearchClicked.invoke()
            }
            return@OnEditorActionListener true
        }
        false
    })
}