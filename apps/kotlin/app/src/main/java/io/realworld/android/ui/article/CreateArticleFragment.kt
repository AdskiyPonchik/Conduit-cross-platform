package io.realworld.android.ui.article

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import io.realworld.android.utils.MarkdownUtils
import io.realworld.android.databinding.FragmentCreateArticleBinding
import io.realworld.api.ConduitClient
import kotlinx.coroutines.launch

class CreateArticleFragment: Fragment() {

    companion object {
        private const val REQUEST_PICK_IMAGE = 2001
    }


    private var _binding:FragmentCreateArticleBinding?= null
    private lateinit var articleViewModel:ArticleViewModel
    private val uploadedImageLinks = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentCreateArticleBinding.inflate(layoutInflater, container, false)
        articleViewModel = ViewModelProvider(this).get(ArticleViewModel::class.java)

        return _binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.apply {
            articleBodyTv.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    renderPreview(s?.toString().orEmpty())
                }
                override fun afterTextChanged(s: Editable?) = Unit
            })

            uploadImageButton.setOnClickListener {
                openImagePicker()
            }

            submitButton.setOnClickListener{
                val title = articleTitleTv.text.toString().trim()
                val description = articleDesciptionTv.text.toString().trim()
                val body = articleBodyTv.text.toString().trim()

                if (title.isBlank() || description.isBlank() || body.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "Please provide title, description, and content.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val tags = articleTagTv.text.toString()
                    .split("\\s".toRegex())
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                articleViewModel.createArticle(
                        title = title,
                        description = description,
                        body = body,
                        tagList = tags,
                        images = null
                )
                Toast.makeText(requireContext(), "Article Published", Toast.LENGTH_SHORT).show()
            }

            renderPreview(articleBodyTv.text?.toString().orEmpty())
        }
    }

    @Suppress("DEPRECATION")
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Pick image"), REQUEST_PICK_IMAGE)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PICK_IMAGE || resultCode != android.app.Activity.RESULT_OK) return
        data?.data?.let { uploadImage(it) }
    }

    private fun uploadImage(uri: Uri) {
        lifecycleScope.launch {
            val binding = _binding ?: return@launch
            val bytes = requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                Toast.makeText(requireContext(), "Image could not be loaded.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
            val extension = mimeType.substringAfter('/', "jpg")
            val fileName = "article_${System.currentTimeMillis()}.$extension"

            val uploadedLink = articleViewModel.uploadArticleImage(bytes, fileName, mimeType)
            if (uploadedLink == null) {
                Toast.makeText(requireContext(), "Upload failed.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val resolvedLink = if (uploadedLink.startsWith("/")) {
                "${ConduitClient.baseUrl.trimEnd('/')}$uploadedLink"
            } else {
                uploadedLink
            }

            if (!uploadedImageLinks.contains(resolvedLink)) {
                uploadedImageLinks.add(resolvedLink)
            }
            binding.uploadedImagesTextView.text = uploadedImageLinks.joinToString(separator = "\n")
            insertImageMarkdown(resolvedLink)
            Toast.makeText(requireContext(), "Image uplaoded.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertImageMarkdown(url: String) {
        val binding = _binding ?: return
        val editor = binding.articleBodyTv
        val selectionStart = editor.selectionStart.coerceAtLeast(0)
        val markdown = "\n![alt text]($url \"Title\")\n"
        editor.text?.insert(selectionStart, markdown)
    }

    private fun renderPreview(markdown: String) {
        val binding = _binding ?: return
        val html = MarkdownUtils.wrapArticleHtml(MarkdownUtils.markdownToHtml(markdown))
        binding.previewWebView.loadDataWithBaseURL(ConduitClient.baseUrl, html, "text/html", "utf-8", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}