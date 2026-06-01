using Realworlddotnet.Api.Features.Articles;

namespace Realworlddotnet.Api.Features.Search;

public interface ISearchHandler
{
    Task<ArticlesResponse> SearchArticlesAsync(SearchQuery query, string username, CancellationToken cancellationToken);
}
