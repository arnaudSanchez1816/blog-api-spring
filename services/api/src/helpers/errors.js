export class AlreadyExistsError extends Error {
    constructor(message, { name, value }) {
        super(message)
        this.field = { name, value }
    }
}
