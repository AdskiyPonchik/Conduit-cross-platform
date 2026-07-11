package io.realworld.android.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realworld.android.R
import io.realworld.android.databinding.FragmentFeedBinding

class GlobalFeedFragment : Fragment() {
    private var _binding: FragmentFeedBinding? = null
    private lateinit var viewModel: FeedViewModel
    private lateinit var feedAdapter: ArticleFeedAdapter

    // assignment2: AuthViewModel for reactive login state (search bar visibility)
    private lateinit var authViewModel: io.realworld.android.AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(FeedViewModel::class.java)
        feedAdapter = ArticleFeedAdapter { openArticle(it) }

        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val layoutManager = LinearLayoutManager(context)
        _binding?.feedRecyclerView?.layoutManager = layoutManager
        _binding?.feedRecyclerView?.adapter = feedAdapter

        // assignment3: improved infinite scroll using lastVisible position
        _binding?.feedRecyclerView?.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        val lastVisible = layoutManager.findLastVisibleItemPosition()
                        val total = layoutManager.itemCount
                        if (lastVisible >= total - 5) {
                            viewModel.loadMore()
                        }
                    }
                }
            },
        )

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // assignment2: Initialisiere das AuthViewModel über den Activity-Scope (MainActivity)
        authViewModel = ViewModelProvider(requireActivity()).get(io.realworld.android.AuthViewModel::class.java)

        // Initial den globalen Feed laden
        viewModel.fetchGlobalFeed()

        // assignment2: Reaktive Überwachung des Login-Status für Suchleiste
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            // Ein Nutzer ist eingeloggt, wenn das User-Objekt existiert und das statische Token gesetzt ist
            val isLoggedIn = user != null && io.realworld.api.ConduitClient.authToken != null

            if (isLoggedIn) {
                // 1. Suchleiste anzeigen
                _binding?.searchContainer?.visibility = android.view.View.VISIBLE

                // 2. Click-Listener für die Suche einrichten
                _binding?.searchButton?.setOnClickListener {
                    val query = _binding?.searchEditText?.text?.toString() ?: ""
                    if (query.isNotBlank()) {
                        viewModel.searchFeed(query)
                    } else {
                        viewModel.fetchGlobalFeed()
                    }
                }
            } else {
                // 1. Feature komplett verschwinden lassen
                _binding?.searchContainer?.visibility = android.view.View.GONE

                // 2. Textfeld aufräumen, damit bei einem erneuten Login kein alter Text drin steht
                _binding?.searchEditText?.setText("")

                // 3. Falls gerade Suchergebnisse aktiv waren, wieder den normalen Feed laden
                viewModel.fetchGlobalFeed()
            }
        }

        // Feed-Ergebnisse im RecyclerView anzeigen
        viewModel.feed.observe(viewLifecycleOwner) {
            feedAdapter.submitList(it)
        }

        // assignment3: error message display
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            _binding?.errorTextView?.visibility = if (msg != null) View.VISIBLE else View.GONE
            _binding?.errorTextView?.text = msg
        }
    }

    fun openArticle(articleId: String) {
        findNavController().navigate(
            R.id.action_globalFeed_openArticle,
            bundleOf(
                resources.getString(R.string.arg_article_id) to articleId
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
