import passport from "passport"
import { Strategy as JwtStrategy, ExtractJwt } from "passport-jwt"
import { Strategy as LocalStrategy } from "passport-local"
import bcrypt from "bcryptjs"
import userService from "../users/usersService.js"

const jwtStrategy = new JwtStrategy(
    {
        jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
        secretOrKey: process.env.JWT_SECRET,
        algorithms: ["HS256"],
    },
    async (jwtPayload, done) => {
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
)
passport.use(jwtStrategy)

const LOCAL_AUTH_ERROR_MESSAGE = "Invalid e-mail/password"
const localStrategy = new LocalStrategy(
    {
        usernameField: "email",
        passwordField: "password",
        session: false,
    },
    async (username, password, done) => {
        try {
            const user = await userService.getUserByEmail(username)
            if (!user) {
                return done(null, false, {
                    message: LOCAL_AUTH_ERROR_MESSAGE,
                })
            }

            const passwordAreMatching = await bcrypt.compare(
                password,
                user.password
            )
            if (!passwordAreMatching) {
                return done(null, false, {
                    message: LOCAL_AUTH_ERROR_MESSAGE,
                })
            }

            return done(null, user)
        } catch (error) {
            done(error, false)
        }
    }
)
passport.use(localStrategy)

export const strategies = {
    local: localStrategy.name,
    jwt: jwtStrategy.name,
}

export default passport
