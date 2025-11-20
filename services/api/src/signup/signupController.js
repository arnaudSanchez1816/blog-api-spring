import { signupValidator } from "./signupValidators.js"
import * as signupService from "./signupService.js"
import { validateRequest } from "../middlewares/validator.js"

export const signup = [
    validateRequest(signupValidator),
    async (req, res, next) => {
        try {
            const { name, email, password } = req.body

            const newUser = await signupService.signupUser({
                name,
                email,
                password,
            })

            const { userPassword, ...userDetails } = newUser

            return res.status(201).json(userDetails)
        } catch (error) {
            next(error)
        }
    },
]
