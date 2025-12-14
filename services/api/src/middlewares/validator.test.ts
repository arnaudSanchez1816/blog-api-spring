import { z } from "zod"
import { validateRequest } from "@/middlewares/validator.js"
import { describe, it, vi, beforeEach, expect } from "vitest"
import { ValidationError } from "@/lib/errors.js"
import type { Request, Response, NextFunction } from "express"

describe("validator", () => {
    let request: Request
    let response: Response
    const next: NextFunction = vi.fn()

    beforeEach(() => {
        vi.restoreAllMocks()
        request = {} as unknown as Request
        response = {} as Response
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

        type SchemaType = z.infer<typeof schema>
        const req: Request<
            SchemaType["params"],
            any,
            SchemaType["body"],
            SchemaType["query"]
        > = {} as any
        req.body = {
            title: "nice title",
            content: "content string",
        }
        req.query = {
            qValue: "query",
        }
        req.params = {
            pValue: "params",
        }

        const middleware = validateRequest(schema)
        await middleware(req, response, next)

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
        type SchemaType = z.infer<typeof schema>
        const req: Request<
            SchemaType["params"],
            any,
            SchemaType["body"],
            SchemaType["query"]
        > = {} as any

        req.body = {
            //@ts-expect-error
            number: "54",
        }
        req.query = {
            upper: "uppercase",
        }
        req.params = {
            lower: "LOWERCASE",
        }

        const middleware = validateRequest(schema)
        await middleware(req, response, next)

        expect(req.body).toStrictEqual({
            number: 54,
        })
        expect(req.query).toStrictEqual({
            upper: "UPPERCASE",
        })
        expect(req.params).toStrictEqual({
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
        const error = promise.mock.settledResults[0]?.value
        expect(error).not.toBeUndefined()
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
        const error = promise.mock.settledResults[0]?.value
        expect(error).not.toBeUndefined()
        expect(error.details).toStrictEqual({
            title: expect.any(String),
            number: expect.any(String),
        })
    })
})
