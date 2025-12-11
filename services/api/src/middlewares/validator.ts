import z, { ZodError } from "zod"
import type { ZodObject, ZodRawShape } from "zod"
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

export const validateRequest =
    <
        SParams extends z.ZodObject<z.ZodRawShape> | undefined,
        SBody extends z.ZodObject<z.ZodRawShape> | undefined,
        SQuery extends z.ZodObject<z.ZodRawShape> | undefined,
        // The ternary statement here handles the possibility of the S[Type] being undefined
        TParams extends z.infer<
            SParams extends z.ZodObject<z.ZodRawShape> ? SParams : any
        >,
        TQuery extends z.infer<
            SQuery extends z.ZodObject<z.ZodRawShape> ? SQuery : any
        >,
        TBody extends z.infer<
            SBody extends z.ZodObject<z.ZodRawShape> ? SBody : any
        >,
    >(
        validator: ZodRequestObject
    ) =>
    async (
        req: Request<TParams, any, TQuery, TBody, any>,
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
            updateQuery(req, _.merge(req.query, data.query))

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

// This only modifies req.query when it must change due to validation.
// `req.query` remains immutable after changing it here.
function updateQuery<T extends z.infer<ZodRequestObject>>(
    req: Request<T>,
    value: Record<string, any>
) {
    Object.defineProperty(req, "query", {
        ...Object.getOwnPropertyDescriptor(req, "query"),
        writable: false,
        value,
    })
}
