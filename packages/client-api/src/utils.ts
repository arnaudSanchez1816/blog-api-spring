export const checkApiUrlEnvVariable = () => {
    const apiUrl = import.meta.env.VITE_API_URL
    if (apiUrl === null || apiUrl === undefined) {
        throw new Error("API_URL env variable is not set, check your .env file")
    }
}
