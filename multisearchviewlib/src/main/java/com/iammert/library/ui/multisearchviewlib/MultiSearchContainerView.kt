package com.iammert.library.ui.multisearchviewlib

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.ImageViewCompat
import androidx.core.view.doOnNextLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.iammert.library.ui.multisearchviewlib.databinding.MsvViewItemBinding
import com.iammert.library.ui.multisearchviewlib.databinding.MsvViewMultiSearchContainerBinding
import com.iammert.library.ui.multisearchviewlib.extensions.*
import com.iammert.library.ui.multisearchviewlib.helper.KeyboardHelper
import com.iammert.library.ui.multisearchviewlib.helper.SimpleTextWatcher


class MultiSearchContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var searchTextStyle = 0

    var removeIcon: Drawable? = null
        set(value) {
            field = value
            tabs.forEach { applyRemoveIcon(it) }
        }

    var iconTint: ColorStateList? = null
        set(value) {
            field = value
            tabs.forEach { applyRemoveIcon(it) }
        }

    var indicatorColor: ColorStateList? = null
        set(value) {
            field = value
            ViewCompat.setBackgroundTintList(binding.viewIndicator, value)
        }

    var animationDuration = DEFAULT_ANIM_DURATION
        set(value) {
            require(value >= 0) { "animationDuration must not be negative" }
            field = value
            searchEnterScrollAnimation.duration = value
            searchCompleteCollapseAnimator.duration = value
            firstSearchTranslateAnimator.duration = value
            indicatorAnimator.duration = value
        }

    var minSearchLength = DEFAULT_MIN_SEARCH_LENGTH
        set(value) {
            require(value >= 0) { "minSearchLength must not be negative" }
            field = value
        }

    private var searchViewWidth: Float = 0f

    private var viewWidth: Float = 0f

    private val binding = MsvViewMultiSearchContainerBinding.inflate(LayoutInflater.from(context), this)

    private val sizeRemoveIcon = context.resources.getDimensionPixelSize(R.dimen.msv_size_remove_icon)

    private val defaultPadding = context.resources.getDimensionPixelSize(R.dimen.msv_padding_default)

    private var isInSearchMode = false

    private var selectedTab: MsvViewItemBinding? = null

    private val tabs = mutableListOf<MsvViewItemBinding>()

    private var multiSearchViewListener: MultiSearchView.MultiSearchViewListener? = null

    private val searchEnterScrollAnimation = ValueAnimator.ofInt()
        .apply {
            duration = DEFAULT_ANIM_DURATION
            interpolator = LinearOutSlowInInterpolator()
            addUpdateListener {
                binding.horizontalScrollView.scrollTo(it.animatedValue as Int, 0)
            }

            endListener {
                selectedTab?.let {
                    it.editTextSearch.requestFocus()
                    KeyboardHelper.showKeyboard(it.editTextSearch)
                }
            }
        }

    private val searchCompleteCollapseAnimator = ValueAnimator.ofInt().apply {
        duration = DEFAULT_ANIM_DURATION
        interpolator = LinearOutSlowInInterpolator()
        addUpdateListener { valueAnimator ->
            selectedTab?.let {
                val newViewLayoutParams = it.root.layoutParams
                newViewLayoutParams.width = valueAnimator.animatedValue as Int
                it.root.layoutParams = newViewLayoutParams
            }
        }
    }

    private val firstSearchTranslateAnimator = ValueAnimator.ofFloat().apply {
        duration = DEFAULT_ANIM_DURATION
        interpolator = LinearOutSlowInInterpolator()
        addUpdateListener { valueAnimator ->
            binding.horizontalScrollView.translationX = valueAnimator.animatedValue as Float
        }
        endListener {
            selectedTab?.let {
                it.editTextSearch.requestFocus()
                KeyboardHelper.showKeyboard(it.editTextSearch)
            }
        }
    }

    private val indicatorAnimator = ValueAnimator.ofFloat().apply {
        duration = DEFAULT_ANIM_DURATION
        interpolator = LinearOutSlowInInterpolator()
        addUpdateListener { valueAnimator ->
            binding.viewIndicator.x = valueAnimator.animatedValue as Float
        }
    }

    init {
        binding.layoutItemContainer.layoutTransition = LayoutTransition()
            .apply {
                disableTransitionType(LayoutTransition.APPEARING)
                disableTransitionType(LayoutTransition.CHANGE_APPEARING)
            }
    }

    override fun onDetachedFromWindow() {
        cancelAnimations()
        super.onDetachedFromWindow()
    }

    private fun cancelAnimations() {
        searchEnterScrollAnimation.cancel()
        searchCompleteCollapseAnimator.cancel()
        firstSearchTranslateAnimator.cancel()
        indicatorAnimator.cancel()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = measuredWidth.toFloat()
        searchViewWidth = viewWidth * WIDTH_RATIO
    }

    fun setSearchViewListener(multiSearchViewListener: MultiSearchView.MultiSearchViewListener) {
        this.multiSearchViewListener = multiSearchViewListener
    }

    fun search() {
        if (isInSearchMode) {
            return
        }

        cancelAnimations()
        selectedTab?.let { deselectTab(it) }

        isInSearchMode = true
        selectedTab = createNewSearchView()
        selectedTab?.let {
            tabs.add(it)
            binding.layoutItemContainer.addView(it.root)
        }

        selectedTab?.root?.doOnLayout {
            val widthWithoutCurrentSearch = widthWithoutCurrentSearch()

            when {
                widthWithoutCurrentSearch == 0 -> {
                    // Slide in from the end edge, which is the left edge in RTL layouts.
                    val startTranslation = if (layoutDirection == LAYOUT_DIRECTION_RTL) -viewWidth else viewWidth
                    firstSearchTranslateAnimator.setFloatValues(startTranslation, 0f)
                    firstSearchTranslateAnimator.start()
                }
                widthWithoutCurrentSearch < viewWidth -> {
                    val scrollEnterStartValue = 0
                    val scrollEnterEndValue = (binding.layoutItemContainer.measuredWidth - viewWidth).toInt()
                    searchEnterScrollAnimation.setIntValues(scrollEnterStartValue, scrollEnterEndValue)
                    searchEnterScrollAnimation.start()
                }
                else -> {
                    val scrollEnterStartValue = (widthWithoutCurrentSearch - viewWidth).toInt()
                    val scrollEnterEndValue = (widthWithoutCurrentSearch - viewWidth + searchViewWidth.toInt()).toInt()
                    searchEnterScrollAnimation.setIntValues(scrollEnterStartValue, scrollEnterEndValue)
                    searchEnterScrollAnimation.start()
                }
            }
        }
    }

    fun completeSearch() {
        if (isInSearchMode.not()) {
            return
        }

        isInSearchMode = false
        KeyboardHelper.hideKeyboard(this)

        selectedTab?.let {
            if (it.editTextSearch.text.trim().length < minSearchLength) {
                removeTab(it)
                return
            }
        }

        selectedTab?.let { lockTab(it) }

        selectedTab?.let {
            val startWidthValue = it.root.measuredWidth
            val endWidthValue = it.editTextSearch.measuredWidth + sizeRemoveIcon + defaultPadding
            searchCompleteCollapseAnimator.setIntValues(startWidthValue, endWidthValue)
            searchCompleteCollapseAnimator.start()
            multiSearchViewListener?.onSearchComplete(tabs.indexOf(it), it.editTextSearch.text.toString())
        }

        selectedTab?.let { selectTab(it) }
    }

    fun isInSearchMode() = isInSearchMode

    val queries: List<String>
        get() = tabs.map { it.editTextSearch.text.toString() }

    val selectedIndex: Int
        get() = tabs.indexOf(selectedTab)

    fun select(index: Int) {
        val tab = tabs.getOrNull(index) ?: throw IndexOutOfBoundsException("No search at index $index")
        if (tab === selectedTab) {
            return
        }
        if (isInSearchMode) {
            completeSearch()
        }
        if (tab in tabs) {
            changeSelectedTab(tab)
        }
    }

    fun addSearch(query: CharSequence) {
        if (isInSearchMode) {
            completeSearch()
        }
        selectedTab?.let { deselectTab(it) }

        val tab = createNewSearchView()
        tab.root.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        tab.editTextSearch.setText(query)
        lockTab(tab)

        tabs.add(tab)
        binding.layoutItemContainer.addView(tab.root)
        selectedTab = tab
        binding.layoutItemContainer.doOnNextLayout {
            if (selectedTab === tab) {
                selectTab(tab)
            }
        }
    }

    fun removeSearch(index: Int) {
        val tab = tabs.getOrNull(index) ?: throw IndexOutOfBoundsException("No search at index $index")
        removeTab(tab, notifyListener = false)
    }

    fun clearSearches() {
        cancelAnimations()
        if (isInSearchMode) {
            isInSearchMode = false
            KeyboardHelper.hideKeyboard(this)
        }
        tabs.clear()
        binding.layoutItemContainer.removeAllViews()
        binding.horizontalScrollView.translationX = 0f
        binding.viewIndicator.visibility = View.INVISIBLE
        selectedTab = null
    }

    /** Makes a completed tab read only. */
    private fun lockTab(tab: MsvViewItemBinding) {
        tab.editTextSearch.isFocusable = false
        tab.editTextSearch.isFocusableInTouchMode = false
        tab.editTextSearch.clearFocus()
    }

    private fun createNewSearchView(): MsvViewItemBinding {
        val viewItem = MsvViewItemBinding.inflate(LayoutInflater.from(context), binding.layoutItemContainer, false)

        viewItem.editTextSearch.setStyle(context, searchTextStyle)
        applyRemoveIcon(viewItem)

        viewItem.root.layoutParams = LinearLayout.LayoutParams(searchViewWidth.toInt(), WRAP_CONTENT)

        val onTabClicked = OnClickListener { onTabClicked(viewItem) }
        viewItem.root.setOnClickListener(onTabClicked)
        viewItem.editTextSearch.setOnClickListener(onTabClicked)

        viewItem.editTextSearch.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                super.onTextChanged(s, start, before, count)
                val index = tabs.indexOf(viewItem)
                if (s != null && index != -1) {
                    multiSearchViewListener?.onTextChanged(index, s.toString())
                }
            }
        })

        viewItem.imageViewRemove.setOnClickListener { removeTab(viewItem) }

        viewItem.editTextSearch.onSearchAction {
            if (viewItem === selectedTab) {
                completeSearch()
            }
        }

        return viewItem
    }

    private fun applyRemoveIcon(viewItem: MsvViewItemBinding) {
        removeIcon?.let { viewItem.imageViewRemove.setImageDrawable(it.constantState?.newDrawable(resources) ?: it) }
        ImageViewCompat.setImageTintList(viewItem.imageViewRemove, iconTint)
    }

    private fun onTabClicked(viewItem: MsvViewItemBinding) {
        if (viewItem === selectedTab) {
            return
        }

        // Finish the search that is being typed first, otherwise it stays editable
        // and the view never leaves search mode.
        if (isInSearchMode) {
            completeSearch()
        }

        multiSearchViewListener?.onItemSelected(
            tabs.indexOf(viewItem),
            viewItem.editTextSearch.text.toString()
        )
        changeSelectedTab(viewItem)
    }

    private fun widthWithoutCurrentSearch(): Int {
        return when {
            binding.layoutItemContainer.childCount > 1 -> {
                var totalWith = 0
                for (i in 0 until binding.layoutItemContainer.childCount - 1) {
                    totalWith += binding.layoutItemContainer.getChildAt(i).measuredWidth
                }
                totalWith
            }
            else -> 0
        }
    }

    private fun removeTab(viewItemBinding: MsvViewItemBinding, notifyListener: Boolean = true) {
        val removeIndex = tabs.indexOf(viewItemBinding)
        if (removeIndex == -1) {
            return
        }

        if (viewItemBinding === selectedTab && isInSearchMode) {
            isInSearchMode = false
            cancelAnimations()
            KeyboardHelper.hideKeyboard(this)
        }

        val newSelectedTab = when {
            viewItemBinding !== selectedTab -> selectedTab
            tabs.size == 1 -> null
            removeIndex == tabs.size - 1 -> tabs[removeIndex - 1]
            else -> tabs[removeIndex + 1]
        }

        tabs.removeAt(removeIndex)
        binding.layoutItemContainer.removeView(viewItemBinding.root)
        selectedTab = newSelectedTab

        if (newSelectedTab == null) {
            binding.viewIndicator.visibility = View.INVISIBLE
        } else {
            // The remaining tabs move once the container is laid out again, so the
            // indicator target is only known after the next layout pass.
            binding.layoutItemContainer.doOnNextLayout {
                if (selectedTab === newSelectedTab) {
                    selectTab(newSelectedTab)
                }
            }
        }

        if (notifyListener) {
            multiSearchViewListener?.onSearchItemRemoved(removeIndex)
        }
    }

    private fun selectTab(viewItemBinding: MsvViewItemBinding) {
        val indicatorCurrentXPosition = binding.viewIndicator.x
        val indicatorTargetXPosition = viewItemBinding.root.x
        indicatorAnimator.setFloatValues(indicatorCurrentXPosition, indicatorTargetXPosition)
        indicatorAnimator.start()

        binding.viewIndicator.visibility = View.VISIBLE
        viewItemBinding.imageViewRemove.visibility = View.VISIBLE
        viewItemBinding.editTextSearch.alpha = 1f
    }

    private fun deselectTab(viewItemBinding: MsvViewItemBinding) {
        binding.viewIndicator.visibility = View.INVISIBLE
        viewItemBinding.imageViewRemove.visibility = View.GONE
        viewItemBinding.editTextSearch.alpha = 0.5f
    }

    private fun changeSelectedTab(newSelectedTabItem: MsvViewItemBinding) {
        selectedTab?.let { deselectTab(it) }
        selectedTab = newSelectedTabItem
        selectedTab?.let { selectTab(it) }
    }

    companion object {

        internal const val DEFAULT_MIN_SEARCH_LENGTH = 3
        private const val WIDTH_RATIO = 0.85f
        internal const val DEFAULT_ANIM_DURATION = 500L
    }
}