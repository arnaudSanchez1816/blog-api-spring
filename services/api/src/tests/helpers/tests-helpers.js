import { expect } from "vitest"
import { api, v1Api } from "./supertest"
import { generateAccessToken } from "../../auth/authService"

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
}
