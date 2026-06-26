using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Hellang.Middleware.ProblemDetails;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Realworlddotnet.Core.Dto;
using Realworlddotnet.Core.Entities;
using Realworlddotnet.Core.Repositories;
using Realworlddotnet.Data.Contexts;

namespace Realworlddotnet.Data.Services;

public class ConduitRepository(ConduitContext context) : IConduitRepository
{
    public async Task AddUserAsync(User user)
    {
        if (await context.Users.AnyAsync(x => x.Username == user.Username))
        {
            throw new ProblemDetailsException(new ValidationProblemDetails
            {
                Status = 422,
                Detail = "Cannot register user",
                Errors = { new KeyValuePair<string, string[]>("Username", ["Username not available"]) }
            });
        }

        if (await context.Users.AnyAsync(x => x.Email == user.Email))
        {
            throw new ProblemDetailsException(new ValidationProblemDetails
            {
                Status = 422,
                Detail = "Cannot register user",
                Errors = { new KeyValuePair<string, string[]>("Email", ["Email address already in use"]) }
            });
        }

        context.Users.Add(user);
    }

    public async Task<User?> GetUserByEmailAsync(string email)
    {
        return await context.Users.FirstOrDefaultAsync(x => x.Email == email);
    }


    public Task<User> GetUserByUsernameAsync(string username, CancellationToken cancellationToken)
    {
        return context.Users.FirstAsync(x => x.Username == username, cancellationToken);
    }

    public async Task<IEnumerable<Tag>> UpsertTagsAsync(IEnumerable<string> tags,
        CancellationToken cancellationToken)
    {
        var dbTags = await context.Tags.Where(x => tags.Contains(x.Id)).ToListAsync(cancellationToken);

        foreach (var tag in tags)
        {
            if (!dbTags.Exists(x => x.Id == tag))
            {
                context.Tags.Add(new Tag(tag));
            }
        }

        return context.Tags.Where(x => tags.Contains(x.Id)).ToList();
    }

    public async Task SaveChangesAsync(CancellationToken cancellationToken)
    {
        await context.SaveChangesAsync(cancellationToken);
    }

    public async Task<ArticlesResponseDto> GetArticlesAsync(
        ArticlesQuery articlesQuery,
        string? username,
        bool isFeed,
        CancellationToken cancellationToken)
    {
        var query = context.Articles.Select(x => x);

        if (!string.IsNullOrWhiteSpace(articlesQuery.Author))
        {
            query = query.Where(x => x.Author.Username == articlesQuery.Author);
        }

        if (!string.IsNullOrWhiteSpace(articlesQuery.Tag))
        {
            query = query.Where(x => x.Tags.Any(tag => tag.Id == articlesQuery.Tag));
        }

        if (!string.IsNullOrWhiteSpace(articlesQuery.Favorited))
        {
            query = query.Where(x => x.ArticleFavorites.Any(f => f.Username == articlesQuery.Favorited));
        }

        query = query.Include(x => x.Author);

        if (username is not null)
        {
            query = query.Include(x => x.Author)
                .ThenInclude(x => x.Followers.Where(fu => fu.FollowerUsername == username))
                .AsSplitQuery();
        }

        if (isFeed)
        {
            query = query.Where(x =>
                x.Author.Username == username ||
                x.ArticleFavorites.Any(f => f.Username == username) ||
                x.Author.Followers.Any(fu => fu.FollowerUsername == username));
        }

        query = query.OrderByDescending(x => x.UpdatedAt);

        var total = await query.CountAsync(cancellationToken);
        var pageQuery = query
            .Skip(articlesQuery.Offset).Take(articlesQuery.Limit)
            .Include(x => x.Author)
            .Include(x => x.Tags)
            .Include(x => x.ArticleFavorites)
            .Include(x => x.Images)
            .AsSplitQuery() // changed for DB performance.
            .AsNoTracking();

        var page = await pageQuery.ToListAsync(cancellationToken);
        foreach (var article in page)
        {
            article.FavoritesCount = article.ArticleFavorites.Count;
            article.Favorited = username != null && article.ArticleFavorites.Any(f => f.Username == username);
        }

        return new ArticlesResponseDto(page, total);
    }

    public async Task<Article?> GetArticleBySlugAsync(string slug, bool asNoTracking,
        CancellationToken cancellationToken, string? username = null)
    {
        var query = context.Articles
            .Include(x => x.Author)
            .Include(x => x.Tags)
            .Include(x => x.Images)
            .AsQueryable();

        if (username != null)
        {
            query = query
                .Include(x => x.Author)
                .ThenInclude(x => x.Followers.Where(fu => fu.FollowerUsername == username))
                .Include(x => x.ArticleFavorites.Where(af => af.Username == username))
                .AsSplitQuery();
        }

        if (asNoTracking)
        {
            query = query.AsNoTracking();
        }

        var article = await query
            .FirstOrDefaultAsync(x => x.Slug == slug, cancellationToken);

        if (article == null)
        {
            return article;
        }

        article.FavoritesCount =
            await context.ArticleFavorites.CountAsync(x => x.ArticleId == article.Id, cancellationToken);
        article.Favorited = username != null
&& article.ArticleFavorites.Any(f => f.Username == username);
        return article;
    }

    public void AddArticle(Article article)
    {
        context.Articles.Add(article);
    }

    public void DeleteArticle(Article article)
    {
        context.Articles.Remove(article);
    }

    public async Task<ArticleFavorite?> GetArticleFavoriteAsync(string username, Guid articleId)
    {
        return await context.ArticleFavorites.FirstOrDefaultAsync(x =>
            x.Username == username && x.ArticleId == articleId);
    }

    public void AddArticleFavorite(ArticleFavorite articleFavorite)
    {
        context.ArticleFavorites.Add(articleFavorite);
    }

    public void AddArticleComment(Comment comment)
    {
        context.Comments.Add(comment);
    }

    public void RemoveArticleComment(Comment comment)
    {
        context.Comments.Remove(comment);
    }

    public async Task<List<Comment>> GetCommentsBySlugAsync(string slug, string? username,
        CancellationToken cancellationToken)
    {
        return await context.Comments.Where(x => x.Article.Slug == slug)
            .Include(x => x.Author)
            .ThenInclude(x => x.Followers.Where(fu => fu.FollowerUsername == username))
            .ToListAsync(cancellationToken);
    }

    public void RemoveArticleFavorite(ArticleFavorite articleFavorite)
    {
        context.ArticleFavorites.Remove(articleFavorite);
    }

    public Task<List<Tag>> GetTagsAsync(CancellationToken cancellationToken)
    {
        return context.Tags.AsNoTracking().ToListAsync(cancellationToken);
    }

    public Task<bool> IsFollowingAsync(string username, string followerUsername, CancellationToken cancellationToken)
    {
        return context.FollowedUsers.AnyAsync(
            x => x.Username == username && x.FollowerUsername == followerUsername,
            cancellationToken);
    }

    public void Follow(string username, string followerUsername)
    {
        context.FollowedUsers.Add(new UserLink(username, followerUsername));
    }

    public void UnFollow(string username, string followerUsername)
    {
        context.FollowedUsers.Remove(new UserLink(username, followerUsername));
    }

    public async Task<List<ArticleFavorite>> GetLikedArticles()
    {
        return await context.ArticleFavorites.Select(x => x).AsNoTracking().ToListAsync();
    }

    public async Task<List<Comment>> GetAllComments()
    {
        return await context.Comments.Select(x => x).AsNoTracking().ToListAsync();
    }

    public async Task<List<UserLink>> GetAllFollowedUsers()
    {
        return await context.FollowedUsers.Select(x => x).AsNoTracking().ToListAsync();
    }

    public async Task<ArticlesResponseDto> SearchArticlesAsync(
        string[] keywords, string username, int limit, int offset, CancellationToken cancellationToken)
    {
        var query = context.Articles
            .Include(x => x.Author)
                .ThenInclude(x => x.Followers.Where(fu => fu.FollowerUsername == username))
            .AsSplitQuery()
            .AsQueryable();

        foreach (var keyword in keywords)
        {
            var kw = keyword.ToLowerInvariant();
            query = query.Where(x =>
                x.Title.ToLower().Contains(kw) ||
                x.Body.ToLower().Contains(kw) ||
                x.Tags.Any(t => t.Id.ToLower().Contains(kw)));
        }

        query = query.OrderByDescending(x => x.UpdatedAt);

        var total = await query.CountAsync(cancellationToken);
        var pageQuery = query
            .Skip(offset).Take(limit)
            .Include(x => x.Tags)
            .Include(x => x.ArticleFavorites)
            .Include(x => x.Images)
            .AsSplitQuery()
            .AsNoTracking();

        var page = await pageQuery.ToListAsync(cancellationToken);
        foreach (var article in page)
        {
            article.FavoritesCount = article.ArticleFavorites.Count;
        }

        return new ArticlesResponseDto(page, total);
    }

    public async Task UpsertSearchCountAsync(string[] keywords, CancellationToken cancellationToken)
    {
        var keywordsLower = keywords.Select(k => k.ToLowerInvariant()).Order().ToList();
        var keywordCombined = string.Join("+", keywordsLower);

        if (keywordsLower.Count > 1)
        {
            keywordsLower.Add(keywordCombined); // also update count for the combined keyword
        }

        foreach (var keyword in keywordsLower)
        {
            var entry = await context.SearchCounts.FirstOrDefaultAsync(x => x.KeywordId == keyword, cancellationToken);
            if (entry is null)
            {
                context.SearchCounts.Add(new SearchCount { KeywordId = keyword, Count = 1 });
            }
            else
            {
                entry.Count++;
            }
        }

        await context.SaveChangesAsync(cancellationToken);
    }

    public void AddArticleImage(ArticleImage articleImage)
    {
        context.ArticleImages.Add(articleImage);
    }
}
