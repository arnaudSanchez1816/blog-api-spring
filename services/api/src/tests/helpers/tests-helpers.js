import { expect } from "vitest"
import { api, v1Api } from "./supertest"
import { generateAccessToken } from "../../auth/authService"

export const testAuthenticationHeader = async (route, user, data) => {
    const token = generateAccessToken(user)

    let { status, body } = await api
        .post(v1Api(route))
        .set("Authorization", `beeeerer ${token}`)
        .send(data)

    expect(status).toBe(401)
    ;({ status, body } = await api
        .post(v1Api(route))
        .set("Authorization", `Bearer${token}`)
        .send(data))

    expect(status).toBe(401)
    ;({ status, body } = await api
        .post(v1Api(route))
        .set("Authorization", `${token}`)
        .send(data))

    expect(status).toBe(401)
}
