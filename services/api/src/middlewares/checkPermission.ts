import { PermissionType } from "@prisma/client"
import createHttpError from "http-errors"
import type { Request, Response, NextFunction } from "express"
import type { ApiUser } from "../types/apiUser.js"

/**
 *
 * @param {PermissionType} permission
 * @returns
 */
export const checkPermission =
    (permission: PermissionType) =>
    (
        req: Request<unknown, any, unknown, unknown>,
        res: Response,
        next: NextFunction
    ) => {
        try {
            const user = req.user as ApiUser
            if (!user) {
                throw new createHttpError.Forbidden()
            }

            const userRoles = user.roles || []
            const rolesWithPermissions = userRoles.filter((r) => {
                return r.permissions.find((p) => p.type === permission)
            })

            if (!rolesWithPermissions || rolesWithPermissions.length === 0) {
                throw new createHttpError.Forbidden()
            }

            next()
        } catch (error) {
            next(error)
        }
    }
