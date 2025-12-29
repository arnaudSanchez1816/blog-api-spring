import passport from "passport"
import {
    Strategy as JwtStrategy,
    ExtractJwt,
    type VerifiedCallback,
} from "passport-jwt"
import { Strategy as LocalStrategy } from "passport-local"
import { Strategy as AnonymousStrategy } from "passport-anonymous"
import bcrypt from "bcryptjs"
import * as userService from "../users/usersService.js"
import { SignInError } from "../lib/errors.js"

const checkJwtUser = async (jwtPayload: any, done: VerifiedCallback) => {
    try {
        const user = await userService.getUserById(jwtPayload.sub)

        if (!user) {
            return done(null, false)
        }

        return done(null, user)
    } catch (error) {
        done(error, false)
    }
}

const jwtStrategy = new JwtStrategy(
    {
        jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
        secretOrKey: process.env.JWT_ACCESS_SECRET!,
        algorithms: ["HS256"],
    },
    checkJwtUser
)
passport.use(jwtStrategy)

export const REFRESH_TOKEN_COOKIE = "__Http-refreshToken"
const jwtRefreshStrategy = new JwtStrategy(
    {
        jwtFromRequest: ExtractJwt.fromExtractors([
            (req) => {
                const refreshToken = req.signedCookies[REFRESH_TOKEN_COOKIE]
                if (!refreshToken) {
                    return null
                }
                return refreshToken
            },
        ]),
        secretOrKey: process.env.JWT_REFRESH_SECRET!,
        algorithms: ["HS256"],
    },
    checkJwtUser
)
jwtRefreshStrategy.name = "jwt-refresh"
passport.use(jwtRefreshStrategy)

const LOCAL_AUTH_ERROR_MESSAGE = "Invalid e-mail/password"
const localStrategy = new LocalStrategy(
    {
        usernameField: "email",
        passwordField: "password",
        session: false,
    },
    async (username, password, done) => {
        try {
            const user = await userService.getUserByEmail(username, {
                includePassword: true,
            })
            if (!user) {
                return done(new SignInError(LOCAL_AUTH_ERROR_MESSAGE), false)
            }

            const passwordAreMatching = await bcrypt.compare(
                password,
                user.password
            )
            if (!passwordAreMatching) {
                return done(new SignInError(LOCAL_AUTH_ERROR_MESSAGE), false)
            }

            return done(null, user)
        } catch (error) {
            done(error)
        }
    }
)
passport.use(localStrategy)

const anonymousStrategy = new AnonymousStrategy()
anonymousStrategy.name = "anonymous"
passport.use(anonymousStrategy)

export const strategies = {
    local: localStrategy.name,
    jwt: jwtStrategy.name,
    jwtRefresh: jwtRefreshStrategy.name,
    anonymous: anonymousStrategy.name!,
}

export default passport
