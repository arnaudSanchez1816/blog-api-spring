export async function parseErrorResponse(errorResponse: Response) {
    const { status, statusText } = errorResponse
    let error: { errorMessage: string; status: number }
    if (errorResponse.body) {
        try {
            const errorBody = await errorResponse.json()
            error = { ...errorBody.error }
        } catch (parseError) {
            if (parseError instanceof SyntaxError == false) {
                console.error(parseError)
            }
            error = { errorMessage: statusText, status }
        }
    } else {
        error = { errorMessage: statusText, status }
    }

    return error
}
