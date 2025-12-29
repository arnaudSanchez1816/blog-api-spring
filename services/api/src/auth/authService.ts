import type { Prisma } from "@prisma/client"
import jwt from "jsonwebtoken"
import type { SignOptions } from "jsonwebtoken"

type UserDetails = Prisma.UserGetPayload<{
    select: {
        id: true
        name: true
        email: true
    }
}>

interface GenerateTokenOptions {
    expiresIn?: SignOptions["expiresIn"]
}

export const generateAccessToken = async (
    user: UserDetails,
    { expiresIn = "1 day" }: GenerateTokenOptions = {}
) => {
    const accessToken: string = await new Promise((resolve, reject) => {
        jwt.sign(
            {
                sub: user.id,
                name: user.name,
                email: user.email,
            },
            process.env.JWT_ACCESS_SECRET!,
            {
                expiresIn,
                algorithm: "HS256",
            },
            (error, encodedToken) => {
                if (error) {
                    return reject(error)
                }

                resolve(encodedToken!)
            }
        )
    })

    return accessToken
}

export const generateRefreshToken = async (
    user: UserDetails,
    { expiresIn = "30 days" }: GenerateTokenOptions = {}
) => {
    const refreshToken: string = await new Promise((resolve, reject) => {
        jwt.sign(
            {
                sub: user.id,
                name: user.name,
                email: user.email,
            },
            process.env.JWT_REFRESH_SECRET!,
            {
                expiresIn,
                algorithm: "HS256",
            },
            (err, encodedToken) => {
                if (err) {
                    return reject(err)
                }

                resolve(encodedToken!)
            }
        )
    })

    return refreshToken
}
