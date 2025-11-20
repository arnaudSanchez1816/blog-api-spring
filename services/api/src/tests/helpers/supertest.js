import supertest from "supertest"
import app from "../../app.js"
import { normalize } from "path"

export const api = supertest(app)

export function v1Api(route) {
    const apiRoute = `/api/v1/${route}`

    return normalize(apiRoute)
}
