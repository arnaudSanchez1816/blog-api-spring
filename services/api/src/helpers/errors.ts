import { Prisma } from "@prisma/client"
import { NotFoundError, UniqueConstraintError } from "../lib/errors.js"

interface HandlePrismaKnownErrorsOptions {
    uniqueConstraintName?: string
}

export const handlePrismaKnownErrors = (
    error: Error,
    { uniqueConstraintName }: HandlePrismaKnownErrorsOptions = {}
) => {
    if (!error) {
        return
    }

    if (error instanceof Prisma.PrismaClientKnownRequestError == false) {
        // Rethrow error if is not a prisma known errors
        return
    }

    if (error.code === "P2002") {
        const targets = error.meta?.target as string[] | undefined
        if (uniqueConstraintName && targets?.includes(uniqueConstraintName)) {
            return new UniqueConstraintError(
                `${uniqueConstraintName} given value already exists.`
            )
        }
        return new UniqueConstraintError(
            `${targets?.at(-1) ?? "Some fields"} given value already exists.`
        )
    }

    if (error.code === "P2016") {
        let p2016Error: Prisma.PrismaClientKnownRequestError & {
            details?: string
        } = error
        const { details } = p2016Error
        if (details?.includes("RecordNotFound")) {
            return new NotFoundError("Not found", 404)
        }
    }

    if (error.code === "P2025") {
        return new NotFoundError("Not found", 404)
    }
}
