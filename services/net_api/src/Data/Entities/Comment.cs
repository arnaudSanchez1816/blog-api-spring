using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

[Table("comments")]
public class Comment
{
    [Key]
    [Column("id")]
    public int Id { get; set; }

    [Column("username")]
    public required string Username { get; set; }

    [Column("body")]
    public required string Body { get; set; }

    [Column("created_at")]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [Column("post_id")]
    public int PostId { get; set; }

    [Column("post")]
    [DeleteBehavior(DeleteBehavior.Cascade)]
    public Post Post { get; set; } = null!;
}