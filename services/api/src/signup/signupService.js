import * as userService from "../users/usersService.js"

export const signupUser = async ({ email, name, password }) => {
    const newUser = await userService.createUser({
        email,
        name,
        password,
        roleName: "user",
    })
    return newUser
}
