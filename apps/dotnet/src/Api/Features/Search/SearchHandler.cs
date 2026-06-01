using Realworlddotnet.Api.Features.Articles;
using Realworlddotnet.Core.Repositories;

namespace Realworlddotnet.Api.Features.Search;

public class SearchHandler(IConduitRepository repository) : ISearchHandler
{
    public async Task<ArticlesResponse> SearchArticlesAsync(
        SearchQuery query, string username, CancellationToken cancellationToken)
    {
        var keywords = query.Query.Split('+', StringSplitOptions.RemoveEmptyEntries);

        if (query.Offset == 0) // update search count only for the first page
        {
            await repository.UpsertSearchCountAsync(keywords, cancellationToken);
        }

        var result = await repository.SearchArticlesAsync(keywords, username, query.Limit, query.Offset, cancellationToken);
        return ArticlesMapper.MapFromArticles(result);
    }
}
