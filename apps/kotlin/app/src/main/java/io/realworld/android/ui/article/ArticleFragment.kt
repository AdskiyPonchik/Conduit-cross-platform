package io.realworld.android.ui.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import io.realworld.android.R
import io.realworld.android.utils.MarkdownUtils
import io.realworld.android.databinding.FragmentArticleBinding
import io.realworld.android.extensions.loadImage
import io.realworld.android.extensions.timeStamp
import io.realworld.api.ConduitClient

class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    lateinit var articleViewModel: ArticleViewModel
    private lateinit var commentAdapter: CommentAdapter
    private var articleId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        articleViewModel = ViewModelProvider(this).get(ArticleViewModel::class.java)
        _binding = FragmentArticleBinding.inflate(inflater, container, false)

        commentAdapter = CommentAdapter()
        _binding?.commentsRecyclerView?.layoutManager = LinearLayoutManager(context)
        _binding?.commentsRecyclerView?.adapter = commentAdapter

        arguments?.let {
            articleId = it.getString(resources.getString(R.string.arg_article_id))
        }

        articleId?.let {
            articleViewModel.fetchArticle(it)
            articleViewModel.fetchComments(it)
        }

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        articleViewModel.article.observe(viewLifecycleOwner) { article ->
            article ?: return@observe
            _binding?.apply {
                titleTextView.text = article.title
                val htmlBody = MarkdownUtils.markdownToHtml(article.body)
                val wrappedHtml = MarkdownUtils.wrapArticleHtml(htmlBody)
                bodyWebView.loadDataWithBaseURL(
                    ConduitClient.baseUrl,
                    wrappedHtml,
                    "text/html",
                    "utf-8",
                    null
                )
                authorTextView.text = article.author.username
                dateTextView.timeStamp = article.createdAt
                avatarImageView.loadImage(article.author.image, true)
            }
        }

        articleViewModel.comments.observe(viewLifecycleOwner) { comments ->
            commentAdapter.submitList(comments)
        }

        articleViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg ?: return@observe
            android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_LONG).show()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}