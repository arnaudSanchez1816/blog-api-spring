import type { Prisma } from "@prisma/client"
import * as userService from "../users/usersService.js"

type SignupUserDTO = Prisma.UserGetPayload<{
    select: {
        email: true
        name: true
        password: true
    }
}>
export const signupUser = async ({ email, name, password }: SignupUserDTO) => {
    const newUser = await userService.createUser({
        email,
        name,
        password,
        roleName: "user",
    })
    return newUser
}
