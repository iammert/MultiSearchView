package com.iammert.library.ui.multisearchviewlib

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.iammert.library.ui.multisearchviewlib.databinding.ViewItemBinding
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.widget.*
import kotlinx.android.synthetic.main.view_item.view.*
import androidx.databinding.DataBindingUtil
import com.iammert.library.ui.multisearchviewlib.databinding.ViewMultiSearchContainerBinding
import com.iammert.library.ui.multisearchviewlib.helper.KeyboardHelper.hideKeyboard
import com.iammert.library.ui.multisearchviewlib.extensions.afterMeasured
import com.iammert.library.ui.multisearchviewlib.extensions.endListener
import com.iammert.library.ui.multisearchviewlib.extensions.inflate
import com.iammert.library.ui.multisearchviewlib.extensions.onSearchAction
import com.iammert.library.ui.multisearchviewlib.helper.KeyboardHelper
import com.iammert.library.ui.multisearchviewlib.helper.SimpleTextWatcher


class MultiSearchContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {

    private var searchViewWidth: Float = 0f

    private var viewWidth: Float = 0f

    private val binding: ViewMultiSearchContainerBinding = inflate(R.layout.view_multi_search_container)

    private val sizeRemoveIcon = context.resources.getDimensionPixelSize(R.dimen.msv_size_remove_icon)

    private val defaultPadding = context.resources.getDimensionPixelSize(R.dimen.padding_16dp)

    private var isInSearchMode = false

    private var selectedTab: ViewItemBinding? = null

    private var multiSearchViewListener: MultiSearchView.MultiSearchViewListener? = null

    private var searchTextStyle: Int = 0

    private val searchEnterScrollAnimation = ValueAnimator.ofInt()
        .apply {
            duration = DEFAULT_ANIM_DURATION
            interpolator = LinearOutSlowInInterpolator()
            addUpdateListener {
                binding.horizontalScrollView.smoothScrollTo(it.animatedValue as Int, 0)
            }

            endListener {
                selectedTab?.let {
                    it.root.editTextSearch.requestFocus()
                    KeyboardHelper.showKeyboard(context)
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
                it.root.editTextSearch.requestFocus()
                KeyboardHelper.showKeyboard(context)
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

        selectedTab?.let { deselectTab(it) }

        isInSearchMode = true
        selectedTab = createNewSearchView()
        binding.layoutItemContainer.addView(selectedTab?.root)

        selectedTab?.root?.afterMeasured {
            val widthWithoutCurrentSearch = widthWithoutCurrentSearch()

            when {
                widthWithoutCurrentSearch == 0 -> {
                    firstSearchTranslateAnimator.setFloatValues(viewWidth, 0f)
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
        hideKeyboard(context)

        selectedTab?.let {
            if (it.editTextSearch.text.length < 3) {
                removeTab(it)
                return
            }
        }

        selectedTab?.let {
            it.root.editTextSearch.isFocusable = false
            it.root.editTextSearch.isFocusableInTouchMode = false
            it.root.editTextSearch.clearFocus()
        }

        selectedTab?.let {
            val startWidthValue = it.root.measuredWidth
            val endWidthValue = it.root.editTextSearch.measuredWidth + sizeRemoveIcon + defaultPadding
            searchCompleteCollapseAnimator.setIntValues(startWidthValue, endWidthValue)
            searchCompleteCollapseAnimator.start()
            multiSearchViewListener?.onSearchComplete(
                binding.layoutItemContainer.childCount - 1,
                it.root.editTextSearch.text
            )
        }

        selectedTab?.let { selectTab(it) }
    }

    fun isInSearchMode() = isInSearchMode

    private fun createNewSearchView(): ViewItemBinding {
        val viewItem: ViewItemBinding = context.inflate(R.layout.view_item)
        viewItem.editTextSearch.setTextAppearance(viewItem.root.context, searchTextStyle)

        viewItem.root.layoutParams = LinearLayout.LayoutParams(searchViewWidth.toInt(), WRAP_CONTENT)

        viewItem.root.setOnClickListener {
            if (viewItem != selectedTab) {
                multiSearchViewListener?.onItemSelected(
                    binding.layoutItemContainer.indexOfChild(viewItem.root),
                    viewItem.editTextSearch.text
                )
                changeSelectedTab(viewItem)
            }
        }

        viewItem.root.editTextSearch.setOnClickListener {
            if (viewItem != selectedTab) {
                multiSearchViewListener?.onItemSelected(
                    binding.layoutItemContainer.indexOfChild(viewItem.root),
                    viewItem.editTextSearch.text
                )
                changeSelectedTab(viewItem)
            }
        }

        viewItem.editTextSearch.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                super.onTextChanged(s, start, before, count)
                s?.let { multiSearchViewListener?.onTextChanged(binding.layoutItemContainer.childCount - 1, it) }
            }
        })

        viewItem.root.imageViewRemove.setOnClickListener {
            selectedTab?.let { removeTab(it) }
        }

        viewItem.editTextSearch.onSearchAction(filter = isInSearchMode) { completeSearch() }

        return viewItem
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

    private fun removeTab(viewItemBinding: ViewItemBinding) {
        val removeIndex = binding.layoutItemContainer.indexOfChild(viewItemBinding.root)

        val currentChildCount = binding.layoutItemContainer.childCount

        when {
            currentChildCount == 1 -> {
                binding.viewIndicator.visibility = View.INVISIBLE
                binding.layoutItemContainer.removeView(viewItemBinding.root)
            }
            removeIndex == currentChildCount - 1 -> {
                val newSelectedView = binding.layoutItemContainer.getChildAt(removeIndex - 1)
                val newSelectedViewBinding = DataBindingUtil.bind<ViewItemBinding>(newSelectedView)
                selectTab(newSelectedViewBinding!!)
                binding.layoutItemContainer.removeView(viewItemBinding.root)
                selectedTab = newSelectedViewBinding
            }
            else -> {
                val newSelectedTabView = binding.layoutItemContainer.getChildAt(removeIndex + 1)
                val newSelectedViewBinding = DataBindingUtil.bind<ViewItemBinding>(newSelectedTabView)
                selectTab(newSelectedViewBinding!!)
                binding.layoutItemContainer.removeView(viewItemBinding.root)
                selectedTab = newSelectedViewBinding
            }
        }

        multiSearchViewListener?.onSearchItemRemoved(removeIndex)
    }

    private fun selectTab(viewItemBinding: ViewItemBinding) {
        val indicatorCurrentXPosition = binding.viewIndicator.x
        val indicatorTargetXPosition = viewItemBinding.root.x
        indicatorAnimator.setFloatValues(indicatorCurrentXPosition, indicatorTargetXPosition)
        indicatorAnimator.start()

        binding.viewIndicator.visibility = View.VISIBLE
        viewItemBinding.imageViewRemove.visibility = View.VISIBLE
        viewItemBinding.editTextSearch.alpha = 1f
    }

    private fun deselectTab(viewItemBinding: ViewItemBinding) {
        binding.viewIndicator.visibility = View.INVISIBLE
        viewItemBinding.imageViewRemove.visibility = View.GONE
        viewItemBinding.editTextSearch.alpha = 0.5f
    }

    private fun changeSelectedTab(newSelectedTabItem: ViewItemBinding) {
        selectedTab?.let { deselectTab(it) }
        selectedTab = newSelectedTabItem
        selectedTab?.let { selectTab(it) }
    }

    companion object {

        private const val WIDTH_RATIO = 0.85f
        private const val DEFAULT_ANIM_DURATION = 500L
    }
}