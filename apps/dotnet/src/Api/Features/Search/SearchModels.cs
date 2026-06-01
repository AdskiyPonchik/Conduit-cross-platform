namespace Realworlddotnet.Api.Features.Search;

public record SearchQuery(string Query, int Limit = 20, int Offset = 0);
