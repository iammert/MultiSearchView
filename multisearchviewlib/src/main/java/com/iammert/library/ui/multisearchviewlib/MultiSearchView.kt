package com.iammert.library.ui.multisearchviewlib

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.iammert.library.ui.multisearchviewlib.databinding.ViewMultiSearchBinding

class MultiSearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr) {

    interface MultiSearchViewListener {

        fun onTextChanged(index: Int, s: CharSequence)

        fun onSearchComplete(index: Int, s: CharSequence)

        fun onSearchItemRemoved(index: Int)

        fun onItemSelected(index: Int, s: CharSequence)
    }

    private val binding = ViewMultiSearchBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiSearchView, defStyleAttr, 0)
        try {
            binding.searchViewContainer.searchTextStyle =
                typedArray.getResourceId(R.styleable.MultiSearchView_searchTextStyle, 0)
            binding.searchViewContainer.minSearchLength = typedArray.getInt(
                R.styleable.MultiSearchView_minSearchLength,
                MultiSearchContainerView.DEFAULT_MIN_SEARCH_LENGTH
            )
        } finally {
            typedArray.recycle()
        }

        binding.imageViewSearch.setOnClickListener {
            if (binding.searchViewContainer.isInSearchMode().not()) {
                binding.searchViewContainer.search()
            } else {
                binding.searchViewContainer.completeSearch()
            }
        }
    }

    /**
     * Minimum length of a query, ignoring surrounding whitespace. Shorter queries are discarded
     * when the search completes. Set it to 0 to keep every query.
     */
    var minSearchLength: Int
        get() = binding.searchViewContainer.minSearchLength
        set(value) {
            binding.searchViewContainer.minSearchLength = value
        }

    fun setSearchViewListener(multiSearchViewListener: MultiSearchViewListener) {
        binding.searchViewContainer.setSearchViewListener(multiSearchViewListener)
    }
}