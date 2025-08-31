import { ZodError, ZodAny } from "zod"

/**
 *
 * @param {ZodAny} validator
 * @returns
 */
export const validateRequest = (validator) => async (req, res, next) => {
    try {
        await validator.parseAsync({
            body: req.body,
            params: req.params,
            query: req.query,
        })

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
