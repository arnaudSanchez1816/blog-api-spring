import jwt from "jsonwebtoken"

export const generateAccessToken = async (
    user,
    { expiresIn = "1 day" } = {}
) => {
    const accessToken = await new Promise((resolve, reject) => {
        jwt.sign(
            {
                sub: user.id,
                name: user.name,
                email: user.email,
            },
            process.env.JWT_SECRET,
            {
                expiresIn,
                algorithm: "HS256",
            },
            (error, encodedToken) => {
                if (error) {
                    return reject(error)
                }

                resolve(encodedToken)
            }
        )
    })

    return accessToken
}

export * as default from "./authService.js"
