import { Router } from "express"
import { signup } from "./signupController.js"
import { validateRequest } from "../middlewares/validator.js"
import { signupValidator } from "./signupValidators.js"

const router: Router = Router()

router.post("/", validateRequest(signupValidator), signup)

export default router
