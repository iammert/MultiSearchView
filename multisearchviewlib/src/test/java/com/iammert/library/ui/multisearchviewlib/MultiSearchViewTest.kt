package com.iammert.library.ui.multisearchviewlib

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Looper
import android.os.Parcelable
import android.util.SparseArray
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.widget.ImageViewCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import java.time.Duration

@RunWith(AndroidJUnit4::class)
class MultiSearchViewTest {

    private lateinit var activity: Activity
    private lateinit var searchView: MultiSearchView
    private val events = mutableListOf<String>()

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar)
        searchView = MultiSearchView(activity)
        activity.setContentView(searchView, ViewGroup.LayoutParams(1000, ViewGroup.LayoutParams.WRAP_CONTENT))
        searchView.setSearchViewListener(object : MultiSearchView.MultiSearchViewListener {
            override fun onTextChanged(index: Int, s: CharSequence) {
                events += "changed $index $s"
            }

            override fun onSearchComplete(index: Int, s: CharSequence) {
                events += "complete $index $s"
            }

            override fun onSearchItemRemoved(index: Int) {
                events += "removed $index"
            }

            override fun onItemSelected(index: Int, s: CharSequence) {
                events += "selected $index $s"
            }
        })
        idle()
    }

    @Test
    fun searchAddsEditableTab() {
        clickSearchIcon()

        assertEquals(1, tabCount())
        assertTrue(container().isInSearchMode())
    }

    @Test
    fun completingReportsQueryWithItsIndex() {
        search("apple")
        search("banana")

        assertEquals(2, tabCount())
        assertTrue(events.contains("complete 0 apple"))
        assertTrue(events.contains("complete 1 banana"))
        assertFalse(container().isInSearchMode())
    }

    @Test
    fun textChangesReportIndexOfEditedTab() {
        search("apple")
        clickSearchIcon()
        editText(1).setText("kiwi")

        assertEquals("changed 1 kiwi", events.last())
    }

    @Test
    fun imeSearchActionCompletesSearch() {
        clickSearchIcon()
        editText(0).setText("apple")
        editText(0).onEditorAction(EditorInfo.IME_ACTION_SEARCH)
        idle()

        assertEquals("complete 0 apple", events.last())
        assertFalse(container().isInSearchMode())
    }

    @Test
    fun hardwareEnterKeyCompletesSearchOnce() {
        clickSearchIcon()
        editText(0).setText("apple")
        editText(0).dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        editText(0).dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        idle()

        assertEquals(1, events.count { it == "complete 0 apple" })
        assertFalse(container().isInSearchMode())
    }

    @Test
    fun shortQueryIsDiscarded() {
        search("ab")

        assertEquals(0, tabCount())
        assertTrue(events.contains("removed 0"))
    }

    @Test
    fun whitespaceDoesNotCountTowardsMinimumLength() {
        search("  a  ")

        assertEquals(0, tabCount())
    }

    @Test
    fun minSearchLengthCanBeChanged() {
        searchView.minSearchLength = 1
        search("a")

        assertEquals(1, tabCount())
        assertTrue(events.contains("complete 0 a"))
    }

    @Test
    fun removeIconRemovesSelectedTab() {
        search("apple")
        search("banana")

        tab(1).findViewById<android.view.View>(R.id.imageViewRemove).performClick()
        idle()

        assertEquals(1, tabCount())
        assertEquals("removed 1", events.last())
    }

    @Test
    fun selectingTabReportsItOnce() {
        search("apple")
        search("banana")

        tab(0).performClick()
        tab(0).performClick()
        idle()

        assertEquals(1, events.count { it == "selected 0 apple" })
    }

    @Test
    fun iconTintIsAppliedToNewTabs() {
        val tint = ColorStateList.valueOf(Color.BLUE)
        searchView.iconTint = tint
        search("apple")

        val removeIcon = tab(0).findViewById<ImageView>(R.id.imageViewRemove)
        assertEquals(tint, ImageViewCompat.getImageTintList(removeIcon))
        assertEquals(tint, ImageViewCompat.getImageTintList(searchView.findViewById(R.id.imageViewSearch)))
    }

    @Test
    fun searchWorksWithoutAnimations() {
        searchView.animationDuration = 0
        search("apple")

        assertEquals(1, tabCount())
        assertEquals("complete 0 apple", events.last())
    }

    @Test
    fun searchCanBeStartedAndCompletedFromCode() {
        searchView.search()
        idle()
        assertTrue(searchView.isInSearchMode)

        editText(0).setText("apple")
        searchView.completeSearch()
        idle()

        assertFalse(searchView.isInSearchMode)
        assertEquals("complete 0 apple", events.last())
    }

    @Test
    fun queriesAndSelectionAreExposed() {
        assertEquals(-1, searchView.selectedIndex)

        search("apple")
        search("banana")
        assertEquals(listOf("apple", "banana"), searchView.queries)
        assertEquals(1, searchView.selectedIndex)

        searchView.selectSearch(0)
        idle()
        assertEquals(0, searchView.selectedIndex)
        assertFalse(events.any { it.startsWith("selected") })
    }

    @Test
    fun searchesCanBeAddedAndRemovedFromCode() {
        searchView.addSearch("apple")
        searchView.addSearch("banana")
        searchView.addSearch("cherry")
        idle()
        assertEquals(listOf("apple", "banana", "cherry"), searchView.queries)
        assertEquals(2, searchView.selectedIndex)
        assertFalse(editText(0).isFocusable)

        searchView.removeSearch(0)
        idle()
        assertEquals(listOf("banana", "cherry"), searchView.queries)
        assertEquals(1, searchView.selectedIndex)

        searchView.clearSearches()
        idle()
        assertEquals(emptyList<String>(), searchView.queries)
        assertEquals(-1, searchView.selectedIndex)
        assertTrue(events.isEmpty())
    }

    @Test
    fun removingTypedSearchLeavesSearchMode() {
        search("apple")
        clickSearchIcon()

        searchView.removeSearch(1)
        idle()

        assertFalse(searchView.isInSearchMode)
        assertEquals(listOf("apple"), searchView.queries)
        assertEquals(0, searchView.selectedIndex)
    }

    @Test
    fun searchesSurviveStateRestore() {
        searchView.id = R.id.msv_test_search_view
        search("apple")
        search("banana")
        searchView.selectSearch(0)
        clickSearchIcon()
        editText(2).setText("x")
        idle()

        val state = SparseArray<Parcelable>()
        searchView.saveHierarchyState(state)

        val restored = MultiSearchView(activity)
        restored.id = R.id.msv_test_search_view
        activity.setContentView(restored, ViewGroup.LayoutParams(1000, ViewGroup.LayoutParams.WRAP_CONTENT))
        restored.restoreHierarchyState(state)
        idle()

        assertEquals(listOf("apple", "banana"), restored.queries)
        assertEquals(1, restored.selectedIndex)
        assertFalse(restored.isInSearchMode)
    }

    @Test
    fun listenerCanOverrideOnlySomeMethods() {
        val completed = mutableListOf<String>()
        searchView.setSearchViewListener(object : MultiSearchView.MultiSearchViewListener {
            override fun onSearchComplete(index: Int, s: CharSequence) {
                completed += s.toString()
            }
        })
        search("apple")
        search("ab")

        assertEquals(listOf("apple"), completed)
    }

    private fun search(query: String) {
        clickSearchIcon()
        editText(tabCount() - 1).setText(query)
        clickSearchIcon()
    }

    private fun clickSearchIcon() {
        searchView.findViewById<android.view.View>(R.id.imageViewSearch).performClick()
        idle()
    }

    private fun container(): MultiSearchContainerView = searchView.findViewById(R.id.searchViewContainer)

    private fun items(): LinearLayout = searchView.findViewById(R.id.layoutItemContainer)

    private fun tabCount() = items().childCount

    private fun tab(index: Int) = items().getChildAt(index)

    private fun editText(index: Int): EditText = tab(index).findViewById(R.id.editTextSearch)

    private fun idle() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2))
    }
}
