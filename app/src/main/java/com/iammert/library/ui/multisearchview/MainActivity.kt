package com.iammert.library.ui.multisearchview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.iammert.library.ui.multisearchview.databinding.ActivityMainBinding
import com.iammert.library.ui.multisearchviewlib.MultiSearchView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.multiSearchView.setSearchViewListener(object : MultiSearchView.MultiSearchViewListener{
            override fun onItemSelected(index: Int, s: CharSequence) {
                Log.v("TEST", "onItemSelected: index: $index, query: $s")
            }

            override fun onTextChanged(index: Int, s: CharSequence) {
                Log.v("TEST", "changed: index: $index, query: $s")
            }

            override fun onSearchComplete(index: Int, s: CharSequence) {
                Log.v("TEST", "complete: index: $index, query: $s")
            }

            override fun onSearchItemRemoved(index: Int) {
                Log.v("TEST", "remove: index: $index")
            }

        })
    }
}
