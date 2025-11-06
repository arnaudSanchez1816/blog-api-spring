using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

[Table("posts")]
public class Post
{
    [Key]
    public int Id { get; set; }

    public required string Title { get; set; }

    public string Description { get; set; } = "";

    public string Body { get; set; } = "";

    public int ReadingTime { get; set; } = 1;

    public DateTime? PublishedAt { get; set; }

    public int AuthorId { get; set; }
    public User Author { get; set; } = null!;

    public ICollection<Tag> Tags { get; } = new List<Tag>();
}