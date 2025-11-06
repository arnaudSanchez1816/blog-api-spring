using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

[Table("tags"), Index(nameof(Slug), IsUnique = true)]
public class Tag
{
    [Key]
    [Column("id")]
    public int Id { get; set; }

    [Column("name")]
    public required string Name { get; set; }

    [Column("slug")]
    public required string Slug { get; set; }

    public ICollection<Post> Posts { get; } = new List<Post>();
}