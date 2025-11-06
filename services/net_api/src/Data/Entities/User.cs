using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

[Table("users"), Index(nameof(Email), IsUnique = true)]
public class User
{
    [Key]
    [Column("id")]
    public int Id { get; set; }

    [Column("email")]
    public required string Email { get; set; }

    [Column("name")]
    public required string Name { get; set; }

    [Column("password")]
    public required string Password { get; set; }

    [Column("posts")]
    public ICollection<Post> Posts { get; } = new List<Post>();
}