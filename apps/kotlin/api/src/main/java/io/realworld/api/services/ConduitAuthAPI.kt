package io.realworld.api.services

import io.realworld.api.models.requests.UpsertArticleRequest
import io.realworld.api.models.requests.UserUpdateRequest
import io.realworld.api.models.responses.ArticleResponse
import io.realworld.api.models.responses.ArticlesResponse
import io.realworld.api.models.responses.ProfileResponse
import io.realworld.api.models.responses.UserResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ConduitAuthAPI {

    @GET("user")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("user")
    suspend fun updateCurrentUser(
        @Body userUpdateRequest: UserUpdateRequest
    ): Response<UserResponse>

    @Multipart
    @POST("images")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("profiles/{username}")
    suspend fun getProfile(
        @Path("username") username: String
    ): Response<ProfileResponse>

    @POST("profiles/{username}/follow")
    suspend fun followProfile(
        @Path("username") username: String
    ): Response<ProfileResponse>

    @DELETE("profiles/{username}/follow")
    suspend fun unfollowProfile(
        @Path("username") username: String
    ): Response<ProfileResponse>

    @GET("articles/feed")
    suspend fun getFeedArticles(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<ArticlesResponse>

    @GET("search")
    suspend fun searchArticles(
        @Query("query") query: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<ArticlesResponse>

    @POST("articles/{slug}/favorite")
    suspend fun favoriteArticle(
        @Path("slug") slug: String
    ): Response<ArticleResponse>

    @DELETE("articles/{slug}/favorite")
    suspend fun unfavoriteArticle(
        @Path("slug") slug: String
    ): Response<ArticleResponse>

    @POST("articles")
    suspend fun createArticle(
        @Body article: UpsertArticleRequest
    ) :Response<ArticleResponse>

    @Multipart
    @POST("images")
    suspend fun uploadArticleImageWithFileField(
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @Multipart
    @POST("images")
    suspend fun uploadArticleImageWithImageField(
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>
}