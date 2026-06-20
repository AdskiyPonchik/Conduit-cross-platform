using Realworlddotnet.Core.Dto;
using Realworlddotnet.Core.Entities;

namespace Realworlddotnet.Core.Repositories;

public interface IConduitRepository
{
    public Task AddUserAsync(User user);

    public Task<User?> GetUserByEmailAsync(string email);

    public Task<User> GetUserByUsernameAsync(string username, CancellationToken cancellationToken);

    public Task<IEnumerable<Tag>> UpsertTagsAsync(IEnumerable<string> tags, CancellationToken cancellationToken);

    public Task SaveChangesAsync(CancellationToken cancellationToken);

    public Task<ArticlesResponseDto> GetArticlesAsync(ArticlesQuery articlesQuery, string? username, bool isFeed,
        CancellationToken cancellationToken);

    public Task<Article?> GetArticleBySlugAsync(string slug, bool asNoTracking, CancellationToken cancellationToken, string? username = null);

    public void AddArticle(Article article);

    public void DeleteArticle(Article article);
    public void AddArticleComment(Comment comment);
    public void RemoveArticleComment(Comment comment);

    public Task<List<Comment>>
        GetCommentsBySlugAsync(string slug, string? username, CancellationToken cancellationToken);

    public Task<ArticleFavorite?> GetArticleFavoriteAsync(string username, Guid articleId);

    public void AddArticleFavorite(ArticleFavorite articleFavorite);

    public void RemoveArticleFavorite(ArticleFavorite articleFavorite);

    public Task<List<Tag>> GetTagsAsync(CancellationToken cancellationToken);

    public Task<bool> IsFollowingAsync(string username, string followerUsername, CancellationToken cancellationToken);

    public void Follow(string username, string followerUsername);

    public void UnFollow(string username, string followerUsername);

    public Task<List<ArticleFavorite>> GetLikedArticles();
    public Task<List<Comment>> GetAllComments();
    public Task<List<UserLink>> GetAllFollowedUsers();

    public Task<ArticlesResponseDto> SearchArticlesAsync(
        string[] keywords, string username, int limit, int offset, CancellationToken cancellationToken);

    public Task UpsertSearchCountAsync(string[] keywords, CancellationToken cancellationToken);
    public void AddArticleImage(ArticleImage articleImage);
}
