using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

[Table("posts")]
public class Post
{
    [Key]
    [Column("id")]
    public int Id { get; set; }

    [Column("title")]
    public required string Title { get; set; }

    [Column("description")]
    public string Description { get; set; } = "";

    [Column("body")]
    public string Body { get; set; } = "";

    [Column("readingTime")]
    public int ReadingTime { get; set; } = 1;

    [Column("published_at")]
    public DateTime? PublishedAt { get; set; }

    [Column("author_id")]
    public int AuthorId { get; set; }
    public User Author { get; set; } = null!;

    [Column("tags")]
    public ICollection<Tag> Tags { get; } = new List<Tag>();
}