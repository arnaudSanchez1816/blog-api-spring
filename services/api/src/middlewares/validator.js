import { ZodError, ZodAny } from "zod"
import _ from "lodash"

/**
 *
 * @param {ZodAny} validator
 * @returns
 */
export const validateRequest = (validator) => async (req, res, next) => {
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
            // Todo : Handle validation errors here ?
            return next(error)
        }
        // Rethrow
        throw error
    }
}

// This only modifies req.query when it must change due to validation.
// `req.query` remains immutable after changing it here.
function updateQuery(req, value) {
    Object.defineProperty(req, "query", {
        ...Object.getOwnPropertyDescriptor(req, "query"),
        writable: false,
        value,
    })
}
