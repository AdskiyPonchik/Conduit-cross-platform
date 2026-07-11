using Realworlddotnet.Api.Features.Articles;
using Realworlddotnet.Core.Repositories;

namespace Realworlddotnet.Api.Features.Search;

public class SearchHandler(IConduitRepository repository) : ISearchHandler
{
    public async Task<ArticlesResponse> SearchArticlesAsync(
        SearchQuery query, string username, CancellationToken cancellationToken)
    {
        var result = await repository.SearchArticlesAsync(query.Query, query.Limit, query.Offset, username, cancellationToken);
        return ArticlesMapper.MapFromArticles(result);
    }
}
