import { expect } from "vitest"
import { api, v1Api } from "./supertest"
import jwt from "jsonwebtoken"

export const testAuthenticationHeader = async (route, user) => {
    const token = generateAccessToken(user)

    // No header
    let { status } = await api.post(v1Api(route))
    expect(status).toBe(401)

    // Bad header formatting
    ;({ status } = await api
        .post(v1Api(route))
        .set("Authorization", `beeeerer ${token}`))

    expect(status).toBe(401)
    ;({ status } = await api
        .post(v1Api(route))
        .set("Authorization", `Bearer${token}`))

    expect(status).toBe(401)
    ;({ status } = await api
        .post(v1Api(route))
        .set("Authorization", `${token}`))

    expect(status).toBe(401)

    const expiredToken = generateAccessToken(user, { expiresIn: "100 ms" })
    await new Promise((res) => setTimeout(res, 200))
    ;({ status } = await api
        .post(v1Api(route))
        .auth(expiredToken, { type: "bearer" }))

    expect(status).toBe(401)
}

export const testPermissions = async (route, user) => {
    const token = generateAccessToken(user)

    const { status } = await api
        .post(v1Api(route))
        .auth(token, { type: "bearer" })

    expect(status).toBe(403)
}

export const generateAccessToken = (
    { id, name, email },
    { expiresIn = "10 minutes" } = {}
) => {
    const token = jwt.sign(
        {
            sub: id,
            name,
            email,
        },
        process.env.JWT_ACCESS_SECRET,
        {
            expiresIn,
        }
    )

    return token
}
