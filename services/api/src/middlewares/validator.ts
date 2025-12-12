import z, { ZodError } from "zod"
import type { ZodObject } from "zod"
import _ from "lodash"
import { ValidationError } from "../lib/errors.js"
import type { Request, Response, NextFunction } from "express"

interface ZodRequestObject extends ZodObject<z.ZodRawShape> {
    shape: {
        body?: z.ZodObject<z.ZodRawShape>
        params?: z.ZodObject<z.ZodRawShape>
        query?: z.ZodObject<z.ZodRawShape>
    }
}

export const validateRequest = <V extends ZodRequestObject>(validator: V) => {
    type SParams = V["shape"] extends { params: infer P } ? P : undefined
    type SBody = V["shape"] extends { body: infer B } ? B : undefined
    type SQuery = V["shape"] extends { query: infer Q } ? Q : undefined

    type TParams = SParams extends z.ZodTypeAny ? z.infer<SParams> : any
    type TBody = SBody extends z.ZodTypeAny ? z.infer<SBody> : any
    type TQuery = SQuery extends z.ZodTypeAny ? z.infer<SQuery> : any

    return async (
        req: Request<TParams, any, TBody, TQuery, any>,
        res: Response,
        next: NextFunction
    ) => {
        try {
            const data = await validator.parseAsync({
                body: req.body,
                params: req.params,
                query: req.query,
            })

            _.merge(req.params, data.params)
            _.merge(req.body, data.body)
            updateQuery(
                req,
                _.merge(req.query, data.query) as Record<string, any>
            )

            return next()
        } catch (error) {
            if (error instanceof ZodError) {
                const details = error.issues
                    .map((i) => ({ field: i.path.pop(), message: i.message }))
                    .reduce(
                        (detailsMap, issue) => {
                            const { field, message } = issue
                            if (!field) {
                                return detailsMap
                            }
                            detailsMap[field] = message
                            return detailsMap
                        },
                        {} as Record<PropertyKey, string>
                    )

                throw new ValidationError("Invalid request", 400, details)
            }
            // Rethrow
            throw error
        }
    }
}

// This only modifies req.query when it must change due to validation.
// `req.query` remains immutable after changing it here.
function updateQuery(req: any, value: Record<string, any>) {
    Object.defineProperty(req, "query", {
        ...Object.getOwnPropertyDescriptor(req, "query"),
        writable: false,
        value,
    })
}
