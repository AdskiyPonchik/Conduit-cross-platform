package io.realworld.android.ui.article

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.realworld.android.R
import io.realworld.android.databinding.ListItemCommentBinding
import io.realworld.android.extensions.loadImage
import io.realworld.android.extensions.timeStamp
import io.realworld.api.models.entities.Comment

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(
    object : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) =
            oldItem == newItem
    }
) {
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            parent.context.getSystemService(LayoutInflater::class.java).inflate(
                R.layout.list_item_comment,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        ListItemCommentBinding.bind(holder.itemView).apply {
            val comment = getItem(position)
            commentBodyTextView.text = comment.body
            commentAuthorTextView.text = comment.author.username
            commentDateTextView.timeStamp = comment.createdAt
            commentAvatarImageView.loadImage(comment.author.image, true)
        }
    }
}
