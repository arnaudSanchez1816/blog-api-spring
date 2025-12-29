import { signupValidator } from "./signupValidators.js"
import * as signupService from "./signupService.js"
import { validateRequest } from "../middlewares/validator.js"
import type { Request, Response, NextFunction } from "express"
import z from "zod"

type SignupSchema = z.infer<typeof signupValidator>
export const signup = [
    validateRequest(signupValidator),
    async (
        req: Request<any, any, SignupSchema["body"]>,
        res: Response,
        next: NextFunction
    ) => {
        try {
            const { name, email, password } = req.body

            const newUser = await signupService.signupUser({
                name,
                email,
                password,
            })

            return res.status(201).json(newUser)
        } catch (error) {
            next(error)
        }
    },
]
