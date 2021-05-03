package com.iammert.library.ui.multisearchviewlib

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import com.iammert.library.ui.multisearchviewlib.databinding.ViewMultiSearchBinding
import com.iammert.library.ui.multisearchviewlib.extensions.inflate

class MultiSearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr) {

    interface MultiSearchViewListener {

        fun onTextChanged(index: Int, s: CharSequence)

        fun onSearchComplete(index: Int, s: CharSequence)

        fun onSearchItemRemoved(index: Int)

        fun onItemSelected(index: Int, s: CharSequence)
    }

    private val binding = inflate<ViewMultiSearchBinding>(R.layout.view_multi_search)

    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.MultiSearchView, defStyleAttr, defStyleAttr)
        val searchTextStyle = typedArray.getResourceId(R.styleable.MultiSearchView_searchTextStyle, 0)
        val imageSource = typedArray.getResourceId(R.styleable.MultiSearchView_searchIcon, R.drawable.ic_round_search_24px)
        val searchIconColor = typedArray.getResourceId(R.styleable.MultiSearchView_searchIconColor, android.R.color.black)
        binding.searchViewContainer.apply {
            this.searchTextStyle = searchTextStyle
        }

        setSearchIconDrawable(imageSource)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            setSearchIconColor(searchIconColor)
        }

        binding.imageViewSearch.setOnClickListener {
            if (binding.searchViewContainer.isInSearchMode().not()) {
                binding.searchViewContainer.search()
            } else {
                binding.searchViewContainer.completeSearch()
            }
        }
    }

    fun setSearchViewListener(multiSearchViewListener: MultiSearchViewListener) {
        binding.searchViewContainer.setSearchViewListener(multiSearchViewListener)
    }


    fun setSearchIconDrawable(drawable: Int) {
        binding.imageViewSearch.setImageResource(drawable)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setSearchIconColor(color : Int) {
        binding.imageViewSearch.imageTintList = AppCompatResources.getColorStateList(context, color)
    }

}