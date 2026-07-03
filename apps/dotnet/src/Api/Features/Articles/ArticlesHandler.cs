using Realworlddotnet.Core.Dto;
using Realworlddotnet.Core.Repositories;

namespace Realworlddotnet.Api.Features.Articles;

public class ArticlesHandler(IConduitRepository repository) : IArticlesHandler
{
    public async Task<Article> CreateArticleAsync(
        NewArticleDto newArticle, string username, CancellationToken cancellationToken)
    {
        var user = await repository.GetUserByUsernameAsync(username, cancellationToken);
        var tags = await repository.UpsertTagsAsync(newArticle.TagList, cancellationToken);
        await repository.SaveChangesAsync(cancellationToken);

        var article = new Article(
                newArticle.Title,
                newArticle.Description,
                newArticle.Body
            )
        { Author = user, Tags = tags.ToList() }
            ;

        repository.AddArticle(article);
        await repository.SaveChangesAsync(cancellationToken);
        return article;
    }

    public async Task<Article> UpdateArticleAsync(
        ArticleUpdateDto update, string slug, string username, CancellationToken cancellationToken)
    {
        var article = await repository.GetArticleBySlugAsync(slug, false, cancellationToken, username);

        if (article == null)
        {
            throw new ProblemDetailsException(422, "ArticleNotFound");
        }

        var currentUser = await repository.GetUserByUsernameAsync(username, cancellationToken);
        if (currentUser.Username != article.Author.Username && currentUser.Role != UserRole.Admin)
        {
            throw new ProblemDetailsException(403, $"{username} is not the author");
        }

        article.UpdateArticle(update);
        await repository.SaveChangesAsync(cancellationToken);
        return article;
    }

    public async Task DeleteArticleAsync(string slug, string username, CancellationToken cancellationToken)
    {
        var article = await repository.GetArticleBySlugAsync(slug, false, cancellationToken, username) ??
                      throw new ProblemDetailsException(new HttpValidationProblemDetails
                      {
                          Status = 422,
                          Title = "Article not found",
                          Detail = $"Slug: {slug}"
                      });

        var currentUser = await repository.GetUserByUsernameAsync(username, cancellationToken);
        if (currentUser.Username != article.Author.Username && currentUser.Role != UserRole.Admin)
        {
            throw new ProblemDetailsException(403, $"{username} is not the author");
        }

        repository.DeleteArticle(article);
        await repository.SaveChangesAsync(cancellationToken);
    }

    public Task<ArticlesResponseDto> GetArticlesAsync(ArticlesQuery query, string? username, bool isFeed,
        CancellationToken cancellationToken)
    {
        var getArticlesQuery = ArticlesMapper.MapFromQuery(query);
        return repository.GetArticlesAsync(getArticlesQuery, username, isFeed, cancellationToken);
    }


    public async Task<Article> GetArticleBySlugAsync(string slug, string? username, CancellationToken cancellationToken)
    {
        var article = await repository.GetArticleBySlugAsync(slug, false, cancellationToken, username) ??
                      throw new ProblemDetailsException(new HttpValidationProblemDetails
                      {
                          Status = 422,
                          Title = "Article not found",
                          Detail = $"Slug: {slug}"
                      });

        var comments = await repository.GetCommentsBySlugAsync(slug, username, cancellationToken);
        article.Comments = comments;

        return article;
    }

    public async Task<Core.Entities.Comment> AddCommentAsync(string slug, string username, CommentDto commentDto,
        CancellationToken cancellationToken)
    {
        var user = await repository.GetUserByUsernameAsync(username, cancellationToken);
        var article = await repository.GetArticleBySlugAsync(slug, false, cancellationToken, username) ??
                      throw new ProblemDetailsException(new HttpValidationProblemDetails
                      {
                          Status = 422,
                          Title = "Article not found",
                          Detail = $"Slug: {slug}"
                      });

        var comment = new Core.Entities.Comment(commentDto.Body, user.Username, article.Id);
        repository.AddArticleComment(comment);

        await repository.SaveChangesAsync(cancellationToken);
        return comment;
    }

    public async Task RemoveCommentAsync(string slug, int commentId, string username,
        CancellationToken cancellationToken)
    {
        _ = await repository.GetArticleBySlugAsync(slug, false, cancellationToken, username) ??
            throw new ProblemDetailsException(new HttpValidationProblemDetails
            {
                Status = 422,
                Title = "Article not found",
                Detail = $"Slug: {slug}"
            });

        var comments = await repository.GetCommentsBySlugAsync(slug, username, cancellationToken);
        var comment = comments.Find(x => x.Id == commentId)
                      ?? throw new ProblemDetailsException(new HttpValidationProblemDetails
                      {
                          Status = 422,
                          Title = "Comment not found",
                          Detail = $"CommentId {commentId}"
                      });


        var currentUser = await repository.GetUserByUsernameAsync(username, cancellationToken);
        if (comment.Author.Username != username && currentUser.Role != UserRole.Moderator && currentUser.Role != UserRole.Admin)
        {
            throw new ProblemDetailsException(new HttpValidationProblemDetails
            {
                Status = 422,
                Title = "User does not own Article",
                Detail = $"User: {username},  Slug: {slug}"
            });
        }

        comments.Remove(comment);
        await repository.SaveChangesAsync(cancellationToken);
    }

    public async Task<List<Core.Entities.Comment>> GetCommentsAsync(string slug, string? username,
        CancellationToken cancellationToken)
    {
        var comments = await repository.GetCommentsBySlugAsync(slug, username, cancellationToken);
        return comments;
    }

    public async Task<Article> AddFavoriteAsync(string slug, string username, CancellationToken cancellationToken)
    {
        var user = await repository.GetUserByUsernameAsync(username, cancellationToken);
        var article = await repository.GetArticleBySlugAsync(slug, false, cancellationToken, username) ??
                      throw new ProblemDetailsException(new HttpValidationProblemDetails
                      {
                          Status = 422,
                          Title = "Article not found",
                          Detail = $"Slug: {slug}"
                      });

        var articleFavorite = await repository.GetArticleFavoriteAsync(user.Username, article.Id);

        if (articleFavorite is null)
        {
            repository.AddArticleFavorite(new ArticleFavorite(user.Username, article.Id)
            {
                Timestamp = DateTime.UtcNow
            });
        }
        else
        {
            articleFavorite.Timestamp = DateTime.UtcNow;
        }

        await repository.SaveChangesAsync(cancellationToken);

        article = await repository.GetArticleBySlugAsync(slug, false, cancellationToken, username);
        return article!;
    }

    public async Task<Article> DeleteFavorite(string slug, string username, CancellationToken cancellationToken)
    {
        var user = await repository.GetUserByUsernameAsync(username, cancellationToken);
        var article = await repository.GetArticleBySlugAsync(slug, false, cancellationToken, username) ??
                      throw new ProblemDetailsException(new HttpValidationProblemDetails
                      {
                          Status = 422,
                          Title = "Article not found",
                          Detail = $"Slug: {slug}"
                      });

        var articleFavorite = await repository.GetArticleFavoriteAsync(user.Username, article.Id);

        if (articleFavorite is not null)
        {
            repository.RemoveArticleFavorite(articleFavorite);
            await repository.SaveChangesAsync(cancellationToken);
        }

        article = await repository.GetArticleBySlugAsync(slug, false, cancellationToken, username);
        return article!;
    }
}
