using System.ComponentModel.DataAnnotations;

namespace Realworlddotnet.Core.Entities;

public class ArticleImage
{
    public Guid Id { get; set; }

    [MaxLength(2000)]
    public string Url { get; set; } = null!;

    public Guid ArticleId { get; set; }
    public Article Article { get; set; } = null!;
}
