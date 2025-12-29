import { beforeEach } from "vitest"
import { describe, vi, it, expect } from "vitest"
import { checkPermission } from "#middlewares/checkPermission.js"
import createHttpError from "http-errors"
import type { Request, Response, NextFunction } from "express"

describe("checkPermission", () => {
    let response: Response
    let request: Request
    const next: NextFunction = vi.fn()

    beforeEach(() => {
        vi.restoreAllMocks()
        response = {} as unknown as Response
        request = {} as Request
    })

    it("should call next if user permissions are valid", () => {
        request.user = {
            roles: [
                {
                    id: 0,
                    permissions: [
                        {
                            type: "CREATE",
                        },
                    ],
                },
                {
                    id: 1,
                    permissions: [
                        {
                            type: "UPDATE",
                        },
                    ],
                },
            ],
        }
        const middleware = checkPermission("CREATE")
        middleware(request, response, next)
        expect(next).toHaveBeenCalledWith()
    })

    it("should throw a 403 http error if permissions are not valid", () => {
        request.user = {
            roles: [
                {
                    id: 0,
                    permissions: [
                        {
                            type: "CREATE",
                        },
                    ],
                },
            ],
        }
        const middleware = checkPermission("DELETE")
        middleware(request, response, next)
        expect(next).toHaveBeenCalledWith(expect.any(createHttpError.Forbidden))
    })

    it("should throw a 403 http error if there is no user", () => {
        const middleware = checkPermission("DELETE")
        middleware(request, response, next)
        expect(next).toHaveBeenCalledWith(expect.any(createHttpError.Forbidden))
    })

    it("should throw a 403 http error if user has no permissions", () => {
        request.user = {
            name: "Hello",
        }
        const middleware = checkPermission("DELETE")
        middleware(request, response, next)
        expect(next).toHaveBeenCalledWith(expect.any(createHttpError.Forbidden))
    })
})
