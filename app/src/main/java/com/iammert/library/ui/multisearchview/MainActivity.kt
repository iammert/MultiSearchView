package com.iammert.library.ui.multisearchview

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.iammert.library.ui.multisearchview.databinding.ActivityMainBinding
import com.iammert.library.ui.multisearchviewlib.MultiSearchView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            insets
        }

        val adapter = PlaceAdapter()
        binding.recyclerViewPlaces.adapter = adapter
        val searchView = binding.multiSearchView

        // Every tab is a separate search, the list shows the results of the selected one.
        fun showResults(query: CharSequence?) {
            val trimmed = query?.trim() ?: ""
            adapter.submitList(PLACES.filter { it.contains(trimmed, ignoreCase = true) })
        }

        // Back finishes the search that is being typed before it closes the screen.
        val completeSearchOnBack = onBackPressedDispatcher.addCallback(this, enabled = false) {
            searchView.completeSearch()
        }

        searchView.setSearchViewListener(object : MultiSearchView.MultiSearchViewListener {
            override fun onSearchModeChanged(inSearchMode: Boolean) {
                completeSearchOnBack.isEnabled = inSearchMode
            }

            override fun onTextChanged(index: Int, s: CharSequence) {
                if (index == searchView.selectedIndex) {
                    showResults(s)
                }
            }

            override fun onSearchComplete(index: Int, s: CharSequence) {
                showResults(s)
            }

            override fun onItemSelected(index: Int, s: CharSequence) {
                showResults(s)
            }

            override fun onSearchItemRemoved(index: Int) {
                showResults(searchView.queries.getOrNull(searchView.selectedIndex))
            }
        })

        showResults(searchView.queries.getOrNull(searchView.selectedIndex))
    }

    private companion object {
        val PLACES = listOf(
            "Amsterdam", "Ankara", "Antalya", "Athens", "Barcelona", "Berlin", "Bodrum", "Budapest",
            "Copenhagen", "Dublin", "Edinburgh", "Florence", "Istanbul", "Izmir", "Kyoto", "Lisbon",
            "London", "Madrid", "Milan", "Munich", "New York", "Oslo", "Paris", "Prague", "Rome",
            "San Francisco", "Seoul", "Stockholm", "Tokyo", "Trabzon", "Vienna", "Zurich"
        )
    }
}
