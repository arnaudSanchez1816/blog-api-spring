import { prisma } from "../config/prisma.js"
import bcrypt from "bcryptjs"

export const getUserByEmail = async (email) => {
    const user = await prisma.user.findUnique({
        where: {
            email: email,
        },
    })

    return user
}

export const getUserById = async (id) => {
    const user = await prisma.user.findUnique({
        where: {
            id: id,
        },
        include: {
            roles: {
                include: { permissions: true },
            },
        },
    })

    return user
}

export const createUser = async ({
    email,
    name,
    password,
    roleName = "user",
}) => {
    const hashedPassword = await bcrypt.hash(
        password,
        +process.env.PASSWORD_SALT_LENGTH
    )

    const createdUser = await prisma.user.create({
        data: {
            email: email,
            name: name,
            password: hashedPassword,
            roles: {
                connect: {
                    name: roleName,
                },
            },
        },
    })

    return createdUser
}

export * as default from "./usersService.js"
