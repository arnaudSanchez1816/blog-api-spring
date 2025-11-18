import createHttpError from "http-errors"
import { REFRESH_TOKEN_COOKIE } from "../config/passport.js"
import * as authService from "./authService.js"
import _ from "lodash"

const COOKIE_REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60 * 1000

export const login = async (req, res, next) => {
    try {
        const user = req.user
        if (!user) {
            throw new createHttpError.Unauthorized()
        }

        const generateAccessToken = authService.generateAccessToken(user, {
            expiresIn: "1 day",
        })
        const generateRefreshToken = authService.generateRefreshToken(user, {
            expiresIn: "30 days",
        })
        const [accessToken, refreshToken] = await Promise.all([
            generateAccessToken,
            generateRefreshToken,
        ])

        res.cookie(REFRESH_TOKEN_COOKIE, refreshToken, {
            maxAge: COOKIE_REFRESH_TOKEN_MAX_AGE,
            httpOnly: true,
            secure: true,
            signed: true,
        })

        return res.status(200).json({
            user: _.omit(user, "password"),
            accessToken: accessToken,
        })
    } catch (error) {
        next(error)
    }
}

export const getAccessToken = async (req, res, next) => {
    try {
        const user = req.user
        if (!user) {
            throw new createHttpError.Unauthorized()
        }

        const accessToken = await authService.generateAccessToken(user, {
            expiresIn: "1 day",
        })

        return res.status(200).json({
            accessToken: accessToken,
        })
    } catch (error) {
        next(error)
    }
}
