public static class UsersController
{
    public static async Task<IResult> GetCurrentUser()
    {
        return TypedResults.Ok();
    }

    public static async Task<IResult> GetCurrentUserPosts()
    {
        return TypedResults.Ok();
    }

    public static async Task<IResult> CreateUser()
    {
        return TypedResults.Ok();
    }
}