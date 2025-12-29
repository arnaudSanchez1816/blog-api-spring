import * as signature from "cookie-signature"

const shapeFlags = (flags: string[]): Record<string, string> =>
    flags.reduce((shapedFlags, flag) => {
        const [flagName, rawValue] = flag.split("=")
        if (!flagName) {
            console.error(`Invalid flag : ${flag}`)
            return shapedFlags
        }
        // edge case where a cookie has a single flag and "; " split results in trailing ";"
        const value = rawValue ? rawValue.replace(";", "") : true
        return { ...shapedFlags, [flagName]: value }
    }, {})

const extractCookies = (
    headers: Record<string, unknown>
): Record<string, { value: string; flags: Record<string, string> }> => {
    const cookies = headers["set-cookie"] // Cookie[]
    if (!cookies || !Array.isArray(cookies)) {
        throw new Error("headers is missing a valid 'set-cookie' property")
    }

    return cookies
        .filter<string>((cookie) => typeof cookie === "string")
        .reduce((shapedCookies, cookieString) => {
            const [rawCookie, ...flags] = cookieString.split("; ")
            if (!rawCookie) {
                console.error(`Invalid cookie string : ${cookieString}`)
                return shapedCookies
            }
            const [cookieName, value] = rawCookie.split("=")
            if (!cookieName) {
                console.error(`Invalid cookie name : ${rawCookie}`)
                return shapedCookies
            }

            return {
                ...shapedCookies,
                [cookieName]: { value, flags: shapeFlags(flags) },
            }
        }, {})
}

function parseSigned(str: string) {
    if (typeof str !== "string") {
        return undefined
    }

    let val: string | undefined
    if (str.substring(0, 2) === "s:") {
        // unsign doesn't work in test environment due to crypto.timingSafeEqual check failing
        //val = signature.unsign(str.slice(2), process.env.SIGNED_COOKIE_SECRET)
        const sVal = str.slice(2)
        val = sVal.slice(0, sVal.lastIndexOf("."))
    } else if (str.substring(0, 4) === "s%3A") {
        const sVal = str.slice(4)
        //val = signature.unsign(sVal, process.env.SIGNED_COOKIE_SECRET)
        val = sVal.slice(0, sVal.lastIndexOf("."))
    } else {
        return str
    }

    if (val) {
        return val
    }

    return false
}

export { shapeFlags, extractCookies, parseSigned }
