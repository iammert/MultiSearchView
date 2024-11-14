package com.iammert.library.ui.multisearchviewlib

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.ImageViewCompat
import com.iammert.library.ui.multisearchviewlib.databinding.MsvViewMultiSearchBinding

class MultiSearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr) {

    interface MultiSearchViewListener {

        fun onTextChanged(index: Int, s: CharSequence)

        fun onSearchComplete(index: Int, s: CharSequence)

        fun onSearchItemRemoved(index: Int)

        fun onItemSelected(index: Int, s: CharSequence)
    }

    private val binding = MsvViewMultiSearchBinding.inflate(LayoutInflater.from(context), this)

    init {
        // The layout is merged into this view, so the default end padding is set here.
        if (paddingEnd == 0) {
            setPaddingRelative(
                paddingStart,
                paddingTop,
                resources.getDimensionPixelSize(R.dimen.msv_padding_default),
                paddingBottom
            )
        }

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiSearchView, defStyleAttr, 0)
        try {
            binding.searchViewContainer.searchTextStyle =
                typedArray.getResourceId(R.styleable.MultiSearchView_searchTextStyle, 0)
            binding.searchViewContainer.minSearchLength = typedArray.getInt(
                R.styleable.MultiSearchView_minSearchLength,
                MultiSearchContainerView.DEFAULT_MIN_SEARCH_LENGTH
            )
            binding.searchViewContainer.animationDuration = typedArray.getInt(
                R.styleable.MultiSearchView_animationDuration,
                MultiSearchContainerView.DEFAULT_ANIM_DURATION.toInt()
            ).toLong()
            typedArray.getDrawable(R.styleable.MultiSearchView_searchIcon)?.let {
                binding.imageViewSearch.setImageDrawable(it)
            }
            binding.searchViewContainer.removeIcon = typedArray.getDrawable(R.styleable.MultiSearchView_removeIcon)
            typedArray.getColorStateList(R.styleable.MultiSearchView_iconTint)?.let { iconTint = it }
            typedArray.getColorStateList(R.styleable.MultiSearchView_indicatorColor)?.let { indicatorColor = it }
        } finally {
            typedArray.recycle()
        }

        binding.imageViewSearch.setOnClickListener {
            if (isInSearchMode) {
                completeSearch()
            } else {
                search()
            }
        }
    }

    /** `true` while a new query is being typed. */
    val isInSearchMode: Boolean
        get() = binding.searchViewContainer.isInSearchMode()

    /**
     * Adds a new search tab, focuses it and opens the keyboard, like tapping the search icon.
     * Does nothing when a search is already in progress.
     */
    fun search() {
        binding.searchViewContainer.search()
    }

    /**
     * Completes the search that is being typed. Queries shorter than [minSearchLength] are
     * discarded. Does nothing when no search is in progress.
     */
    fun completeSearch() {
        binding.searchViewContainer.completeSearch()
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

    /** Duration of the search animations in milliseconds. */
    var animationDuration: Long
        get() = binding.searchViewContainer.animationDuration
        set(value) {
            binding.searchViewContainer.animationDuration = value
        }

    /** Tint applied to the search and remove icons. `null` keeps the icons' own colors. */
    var iconTint: ColorStateList?
        get() = binding.searchViewContainer.iconTint
        set(value) {
            ImageViewCompat.setImageTintList(binding.imageViewSearch, value)
            binding.searchViewContainer.iconTint = value
        }

    /** Color of the line under the selected tab. */
    var indicatorColor: ColorStateList?
        get() = binding.searchViewContainer.indicatorColor
        set(value) {
            binding.searchViewContainer.indicatorColor = value
        }

    /** Sets the icon of the button that starts and completes a search. */
    fun setSearchIcon(drawable: Drawable?) {
        binding.imageViewSearch.setImageDrawable(drawable)
    }

    /** Sets the icon used to remove the selected tab. `null` restores the default icon. */
    fun setRemoveIcon(drawable: Drawable?) {
        binding.searchViewContainer.removeIcon =
            drawable ?: AppCompatResources.getDrawable(context, R.drawable.msv_ic_clear)
    }

    fun setSearchViewListener(multiSearchViewListener: MultiSearchViewListener) {
        binding.searchViewContainer.setSearchViewListener(multiSearchViewListener)
    }
}