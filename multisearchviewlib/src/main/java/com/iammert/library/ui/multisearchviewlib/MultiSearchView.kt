package com.iammert.library.ui.multisearchviewlib

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.ImageViewCompat
import com.iammert.library.ui.multisearchviewlib.databinding.MsvViewMultiSearchBinding

class MultiSearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr) {

    /**
     * Receives search events. Every method has an empty default implementation, so only the
     * events of interest need to be overridden, also from Java.
     */
    interface MultiSearchViewListener {

        /** The text of the tab at [index] changed to [s]. */
        fun onTextChanged(index: Int, s: CharSequence) {}

        /** The search at [index] was completed with the query [s]. */
        fun onSearchComplete(index: Int, s: CharSequence) {}

        /** The tab at [index] was removed by the user or discarded because it was too short. */
        fun onSearchItemRemoved(index: Int) {}

        /** The user selected the tab at [index] with the query [s]. */
        fun onItemSelected(index: Int, s: CharSequence) {}

        /**
         * A search started ([inSearchMode] is `true`) or ended. Useful to intercept the back
         * button only while a query is being typed.
         */
        fun onSearchModeChanged(inSearchMode: Boolean) {}
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

    /**
     * Enables or disables the view. A disabled view ignores taps, completes the search that is
     * being typed and does not start new searches.
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.imageViewSearch.isEnabled = enabled
        binding.searchViewContainer.isEnabled = enabled
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

    /** Text of every search tab, in display order. */
    val queries: List<String>
        get() = binding.searchViewContainer.queries

    /** Index of the selected tab, or -1 when there are no searches. */
    val selectedIndex: Int
        get() = binding.searchViewContainer.selectedIndex

    /**
     * Selects the tab at [index]. A search that is being typed is completed first.
     * [MultiSearchViewListener.onItemSelected] is not called for programmatic selections.
     *
     * @throws IndexOutOfBoundsException when there is no tab at [index]
     */
    fun selectSearch(index: Int) {
        binding.searchViewContainer.select(index)
    }

    /**
     * Adds a completed search tab with [query] and selects it, for example to restore filters
     * from a saved screen state. A search that is being typed is completed first.
     * No listener methods are called.
     */
    fun addSearch(query: CharSequence) {
        binding.searchViewContainer.addSearch(query)
    }

    /**
     * Removes the tab at [index] without calling
     * [MultiSearchViewListener.onSearchItemRemoved].
     *
     * @throws IndexOutOfBoundsException when there is no tab at [index]
     */
    fun removeSearch(index: Int) {
        binding.searchViewContainer.removeSearch(index)
    }

    /** Removes every tab and leaves search mode. No listener methods are called. */
    fun clearSearches() {
        binding.searchViewContainer.clearSearches()
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

    override fun onSaveInstanceState(): Parcelable {
        val state = SavedState(super.onSaveInstanceState())
        val queries = queries
        val selectedIndex = selectedIndex
        // A search that is still being typed is kept only if it would pass completeSearch().
        val keptIndices = queries.indices.filter { queries[it].trim().length >= minSearchLength }
        state.queries = keptIndices.map { queries[it] }
        state.selectedIndex = keptIndices.indexOf(selectedIndex).takeIf { it != -1 } ?: keptIndices.lastIndex
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        clearSearches()
        state.queries.forEach { addSearch(it) }
        if (state.selectedIndex in state.queries.indices) {
            selectSearch(state.selectedIndex)
        }
    }

    // Every tab uses the same view ids, so their own states would overwrite each other.
    // The tabs are recreated from SavedState instead.
    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    /** Sets the listener for search events, or removes it when [multiSearchViewListener] is `null`. */
    fun setSearchViewListener(multiSearchViewListener: MultiSearchViewListener?) {
        binding.searchViewContainer.setSearchViewListener(multiSearchViewListener)
    }

    private class SavedState : BaseSavedState {

        var queries: List<String> = emptyList()
        var selectedIndex = -1

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            queries = parcel.createStringArrayList().orEmpty()
            selectedIndex = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeStringList(queries)
            out.writeInt(selectedIndex)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel) = SavedState(parcel)

                override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
            }
        }
    }
}
