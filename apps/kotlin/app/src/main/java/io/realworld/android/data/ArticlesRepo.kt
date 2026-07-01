package io.realworld.android.data

import io.realworld.android.utils.withRetry
import io.realworld.api.ConduitClient
import io.realworld.api.models.entities.Article
import io.realworld.api.models.entities.ArticleData
import io.realworld.api.models.requests.UpsertArticleRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response

object ArticlesRepo {
    val api = ConduitClient.publicApi
    val authApi = ConduitClient.authApi

    val pageSize = 20

    suspend fun getGlobalFeed(offset: Int = 0) =
        withRetry { api.getArticles(limit = pageSize, offset = offset) }.body()?.articles

    suspend fun getMyFeed(offset: Int = 0) =
        withRetry { authApi.getFeedArticles(limit = pageSize, offset = offset) }.body()?.articles

    suspend fun searchArticles(query: String, offset: Int = 0): List<Article>? {
        val response = withRetry {
            authApi.searchArticles(
                query = query.trim().replace(" ", "+"),
                limit = pageSize,
                offset = offset
            )
        }
        if (response.code() == 401) throw SecurityException("Nicht autorisiert")
        return response.body()?.articles
    }

    suspend fun createArticle(
        title:String,
        description:String,
        body:String,
        tagList:List<String> = emptyList(),
        images: List<String>? = null
    ) : Article? {
        val response =authApi.createArticle(
            UpsertArticleRequest(
            ArticleData(
                title=title,
                description = description,
                body = body,
                tagList = tagList,
                images = images
            )
        )
        )

        return response.body()?.article
    }

    suspend fun uploadArticleImage(
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ): String? {
        uploadWithPartName(bytes, fileName, mimeType, "file")?.let { return it }
        uploadWithPartName(bytes, fileName, mimeType, "image")?.let { return it }
        return null
    }

    private suspend fun uploadWithPartName(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
        partName: String
    ): String? {
        val part = MultipartBody.Part.createFormData(
            partName,
            fileName,
            bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        )

        val response = when (partName) {
            "file" -> authApi.uploadArticleImageWithFileField(part)
            "image" -> authApi.uploadArticleImageWithImageField(part)
            else -> return null
        }

        return extractUrlFromResponse(response)
    }

    private fun extractUrlFromResponse(response: Response<okhttp3.ResponseBody>): String? {
        if (!response.isSuccessful) return null

        response.headers()["Location"]?.let { headerUrl ->
            if (headerUrl.isNotBlank()) return headerUrl
        }

        val payload = response.body()?.string().orEmpty()
        return extractImageUrl(payload)
    }

    private fun extractImageUrl(payload: String): String? {
        if (payload.isBlank()) return null
        return try {
            val json = JSONObject(payload)
            findUrlInJson(json)
        } catch (_: Exception) {
            Regex("https?://[^\\s\"']+|/api/images/[^\\s\"']+")
                .find(payload)
                ?.value
        }
    }

    private fun findUrlInJson(json: JSONObject): String? {
        val preferredKeys = listOf("url", "imageUrl", "image", "link", "path")
        preferredKeys.forEach { key ->
            val value = json.optString(key)
            if (value.startsWith("http") || value.startsWith("/api/images/")) {
                return value
            }
        }
        val nestedCandidates = listOf("data", "result", "image")
        nestedCandidates.forEach { key ->
            val nested = json.optJSONObject(key) ?: return@forEach
            findUrlInJson(nested)?.let { return it }
        }
        return null
    }
}