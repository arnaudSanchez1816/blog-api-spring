using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

[Table("comments")]
public class Comment
{
    [Key]
    public int Id { get; set; }

    public required string Username { get; set; }

    public required string Body { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public int PostId { get; set; }

    [DeleteBehavior(DeleteBehavior.Cascade)]
    public Post Post { get; set; } = null!;
}