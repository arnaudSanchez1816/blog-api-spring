import { PermissionType } from "@prisma/client"
import { checkPermission } from "../middlewares/checkPermission.js"
import { validateRequest } from "../middlewares/validator.js"
import postsService, { SortByValues } from "../posts/postsService.js"
import { createUserValidator } from "./usersValidators.js"
import usersService from "../users/usersService.js"

export const getCurrentUser = async (req, res, next) => {
    try {
        // Omit password
        const { password, ...userDetails } = req.user

        return res.json(userDetails)
    } catch (error) {
        next(error)
    }
}

export const getCurrentUserPosts = async (req, res, next) => {
    try {
        const { id: userId } = req.user

        const { posts, count } = await postsService.getPosts({
            sortBy: SortByValues.idDesc,
            publishedOnly: false,
            authorId: userId,
        })

        const responseJson = {
            metadata: {
                count: count,
            },
            results: posts,
        }

        return res.json(responseJson)
    } catch (error) {
        next(error)
    }
}

export const createUser = [
    checkPermission(PermissionType.CREATE),
    validateRequest(createUserValidator),
    async (req, res, next) => {
        try {
            const { email, password, name, role } = req.body
            const newUser = await usersService.createUser({
                email,
                name,
                password,
                roleName: role,
            })

            const { userPassword, ...newUserDetails } = newUser
            return res.status(201).json(newUserDetails)
        } catch (error) {
            next(error)
        }
    },
]
