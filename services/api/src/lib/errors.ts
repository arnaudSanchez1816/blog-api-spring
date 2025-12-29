export class BaseError extends Error {
    statusCode
    constructor(
        errorMessage: string,
        statusCode: number,
        { name = "BaseError", cause }: { name?: string; cause?: Error }
    ) {
        super(errorMessage, { cause })
        this.statusCode = statusCode
        this.name = name
    }

    toResponse() {
        return {
            //name: this.name,
            errorMessage: this.message,
        }
    }
}

export class NotFoundError extends BaseError {
    constructor(errorMessage = "Not found", statusCode = 404, cause?: Error) {
        super(errorMessage, statusCode, { name: "NotFoundError", cause })
    }
}

export class SignInError extends BaseError {
    constructor(
        errorMessage = "Invalid email or password",
        statusCode = 401,
        cause?: Error
    ) {
        super(errorMessage, statusCode, { name: "SignInError", cause })
    }
}

export class ValidationError extends BaseError {
    details
    constructor(
        errorMessage = "Field is invalid",
        statusCode = 400,
        details = {},
        cause?: Error
    ) {
        super(errorMessage, statusCode, { name: "AlreadyExistsError", cause })
        this.details = details
    }

    toResponse() {
        const response = super.toResponse()
        return {
            ...response,
            details: this.details,
        }
    }
}

export class UniqueConstraintError extends BaseError {
    constructor(
        errorMessage = "Resource already exists",
        statusCode = 400,
        cause?: Error
    ) {
        super(errorMessage, statusCode, { name: "UniqueResourceError", cause })
    }
}

export default BaseError
