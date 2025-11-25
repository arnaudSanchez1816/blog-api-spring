import { prisma } from "../config/prisma.js"
import bcrypt from "bcryptjs"

export const getUserByEmail = async (
    email,
    { includePassword = false } = {}
) => {
    const user = await prisma.user.findUnique({
        where: {
            email: email,
        },
        omit: {
            password: !includePassword,
        },
    })

    return user
}

export const getUserById = async (id, { includePassword = false } = {}) => {
    const user = await prisma.user.findUnique({
        where: {
            id: id,
        },
        include: {
            roles: {
                include: { permissions: true },
            },
        },
        omit: {
            password: !includePassword,
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
        include: {
            roles: true,
        },
    })

    return createdUser
}
