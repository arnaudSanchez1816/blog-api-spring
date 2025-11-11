import { z } from "zod"
import { validateRequest } from "./validator"
import { describe, it, vi, beforeEach, expect } from "vitest"
import { ValidationError } from "../lib/errors"

describe("validator", () => {
    let request
    let response
    const next = vi.fn()

    beforeEach(() => {
        vi.resetAllMocks()
        request = {}
        response = {}
    })

    it("should call next if validation was successful", async () => {
        const schema = z.object({
            body: z.object({
                title: z.string(),
                content: z.string(),
            }),
            query: z.object({
                qValue: z.string(),
            }),
            params: z.object({
                pValue: z.string(),
            }),
        })
        request.body = {
            title: "nice title",
            content: "content string",
        }
        request.query = {
            qValue: "query",
        }
        request.params = {
            pValue: "params",
        }

        const middleware = validateRequest(schema)
        await middleware(request, response, next)

        expect(next).toHaveBeenCalled()
    })

    it("should replace body, query and params properties with validated one", async () => {
        const schema = z.object({
            body: z.object({
                number: z.coerce.number().int(),
            }),
            query: z.object({
                upper: z.string().toUpperCase(),
            }),
            params: z.object({
                lower: z.string().toLowerCase(),
            }),
        })
        request.body = {
            number: "54",
        }
        request.query = {
            upper: "uppercase",
        }
        request.params = {
            lower: "LOWERCASE",
        }

        const middleware = validateRequest(schema)
        await middleware(request, response, next)

        expect(request.body).toStrictEqual({
            number: 54,
        })
        expect(request.query).toStrictEqual({
            upper: "UPPERCASE",
        })
        expect(request.params).toStrictEqual({
            lower: "lowercase",
        })
    })

    it("should throw a validation error if request is invalid", async () => {
        const schema = z.object({
            body: z.object({
                title: z.string(),
            }),
        })
        request.body = {
            content: "oops wrong name !",
        }

        const middleware = validateRequest(schema)
        const promise = vi.fn(() => middleware(request, response, next))
        await expect(promise).rejects.toThrow(ValidationError)
        const error = promise.mock.settledResults[0].value
        expect(error.details).toStrictEqual({
            title: expect.any(String),
        })
    })

    it("should set errors details to an array if multiple fields failed validation", async () => {
        const schema = z.object({
            body: z.object({
                title: z.string(),
                content: z.string(),
                number: z.number(),
            }),
        })
        request.body = {
            header: "wrong property",
            content: "content",
            number: "hello",
        }

        const middleware = validateRequest(schema)
        const promise = vi.fn(() => middleware(request, response, next))
        await expect(promise).rejects.toThrow(ValidationError)
        const error = promise.mock.settledResults[0].value
        expect(error.details).toStrictEqual({
            title: expect.any(String),
            number: expect.any(String),
        })
    })
})
