using System.ComponentModel.DataAnnotations;

namespace Realworlddotnet.Core.Entities;

public class SearchCount
{
    [Key]
    [MaxLength(500)]
    public string KeywordId { get; set; } = string.Empty;

    public int Count { get; set; }
}
