package io.realworld.android.ui.search

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
import io.realworld.android.ui.feed.ArticleFeedAdapter

class SearchResultFragment : Fragment() {
    private var _binding: FragmentFeedBinding? = null
    private lateinit var viewModel: SearchViewModel
    private lateinit var feedAdapter: ArticleFeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        feedAdapter = ArticleFeedAdapter { openArticle(it) }

        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val layoutManager = LinearLayoutManager(context)
        _binding?.feedRecyclerView?.layoutManager = layoutManager
        _binding?.feedRecyclerView?.adapter = feedAdapter

        _binding?.feedRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    val total = layoutManager.itemCount
                    if (lastVisible >= total - 5) {
                        viewModel.loadMore()
                    }
                }
            }
        })

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val query = arguments?.getString("query") ?: ""
        viewModel.results.observe(viewLifecycleOwner) {
            feedAdapter.submitList(it)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            _binding?.errorTextView?.visibility = if (msg != null) View.VISIBLE else View.GONE
            _binding?.errorTextView?.text = msg
        }
        viewModel.search(query)
    }

    private fun openArticle(articleId: String) {
        findNavController().navigate(
            R.id.action_search_openArticle,
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
