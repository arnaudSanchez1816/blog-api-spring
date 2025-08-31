import userService from "../users/usersService.js"

export const signupUser = async ({ email, name, password }) => {
    const newUser = await userService.createUser({ email, name, password })
    return newUser
}

export * as default from "./signupService.js"
