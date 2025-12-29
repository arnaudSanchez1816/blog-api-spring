import { pino } from "../config/pino.js"
import { handlePrismaKnownErrors } from "../helpers/errors.js"
import BaseError, { ValidationError } from "../lib/errors.js"
import z, { ZodError } from "zod"
import createHttpError from "http-errors"
import { Prisma } from "@prisma/client"
import type { Request, Response, NextFunction } from "express"

export const errorHandler = (
    error: Error,
    req: Request,
    res: Response,
    next: NextFunction
) => {
    pino.error(error)

    let handledError: BaseError | undefined

    if (error instanceof ZodError) {
        handledError = new ValidationError(
            "Invalid request",
            400,
            z.flattenError(error)
        )
    }

    if (createHttpError.isHttpError(error)) {
        handledError = new BaseError(error.message, error.statusCode, {
            name: "HttpError",
        })
    }

    if (error instanceof Prisma.PrismaClientKnownRequestError) {
        handledError = handlePrismaKnownErrors(error)
    }

    const shouldSendCause = process.env.NODE_ENV === "development" || false
    const errorResponse =
        error instanceof BaseError
            ? error
            : (handledError ??
              new BaseError("Something went wrong", 500, {
                  cause: shouldSendCause ? error : undefined,
              }))

    return res
        .status(errorResponse.statusCode)
        .json({ error: errorResponse.toResponse() })
}
