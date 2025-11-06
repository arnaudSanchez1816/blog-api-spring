using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

[Table("users"), Index(nameof(Email), IsUnique = true)]
public class User
{
    [Key]
    public int Id { get; set; }

    public required string Email { get; set; }

    public required string Name { get; set; }

    public required string Password { get; set; }

    public ICollection<Post> Posts { get; } = new List<Post>();
}