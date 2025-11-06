using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

[Table("tags"), Index(nameof(Slug), IsUnique = true)]
public class Tag
{
    [Key]
    public int Id { get; set; }

    public required string Name { get; set; }

    public required string Slug { get; set; }

    public ICollection<Post> Posts { get; } = new List<Post>();
}