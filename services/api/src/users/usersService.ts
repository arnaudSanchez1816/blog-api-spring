import type { Prisma } from "@prisma/client"
import { prisma } from "../config/prisma.js"
import bcrypt from "bcryptjs"
import type { ApiUser } from "@/types/apiUser.js"

export const getUserByEmail = async (
    email: string,
    { includePassword = false } = {}
) => {
    const user = await prisma.user.findUnique({
        where: {
            email: email,
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

export const getUserById = async (
    id: number,
    { includePassword = false } = {}
) => {
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

interface CreateUserParams {
    email: string
    name: string
    password: string
    roleName: string
}

export const createUser = async ({
    email,
    name,
    password,
    roleName = "user",
}: CreateUserParams) => {
    const hashedPassword = await bcrypt.hash(
        password,
        +process.env.PASSWORD_SALT_LENGTH!
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
        omit: {
            password: true,
        },
    })

    return createdUser
}

export type UserPersonnalDetails = Prisma.UserGetPayload<{
    include: {
        roles: {
            select: {
                id: true
                name: true
            }
        }
    }
    omit: {
        password: true
    }
}>

export const GetUserPersonnalDetails = (
    userDetails: ApiUser
): UserPersonnalDetails => {
    console.log(userDetails)

    return {
        id: userDetails.id,
        name: userDetails.name,
        email: userDetails.email,
        roles: userDetails.roles.map((role) => ({
            id: role.id,
            name: role.name,
        })),
    }
}
