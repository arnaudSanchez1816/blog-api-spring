export const fetchAccessToken = async () => {
    const getTokenUrl = new URL("./auth/token", import.meta.env.VITE_API_URL)
    const getTokenResponse = await fetch(getTokenUrl, {
        mode: "cors",
        credentials: "include",
        method: "get",
    })
    if (!getTokenResponse.ok) {
        throw getTokenResponse
    }
    const { accessToken } = await getTokenResponse.json()

    return accessToken
}
