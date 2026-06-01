using Realworlddotnet.Api.Features.Articles;
using Realworlddotnet.Infrastructure.Extensions.OpenApi;

namespace Realworlddotnet.Api.Features.Search;

public static class SearchEndpoints
{
    public static void AddSearchEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGroup("search")
            .RequireAuthorization()
            .WithTags("Search")
            .WithUnauthenticated()
            .MapGet("/", Search);
    }

    private static async Task<Ok<ArticlesResponse>> Search(
        [AsParameters] SearchQuery query,
        ISearchHandler searchHandler,
        ClaimsPrincipal claimsPrincipal,
        CancellationToken cancellationToken)
    {
        var username = claimsPrincipal.FindFirstValue(ClaimTypes.NameIdentifier)!;
        var result = await searchHandler.SearchArticlesAsync(query, username, cancellationToken);
        return TypedResults.Ok(result);
    }
}
