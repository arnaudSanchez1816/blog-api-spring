import * as tagService from "./tagsService.js"
import createHttpError from "http-errors"
import { UniqueConstraintError, ValidationError } from "../lib/errors.js"
import { handlePrismaKnownErrors } from "../helpers/errors.js"
import type { Request, Response, NextFunction } from "express"
import type z from "zod"
import {
    createTagValidator,
    deleteTagValidator,
    editTagValidator,
    getTagValidator,
} from "./tagsValidators.js"

type GetTagSchema = z.infer<typeof getTagValidator>
export const getTag = async (
    req: Request<GetTagSchema["params"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const { id } = req.params

        const tag = await tagService.getTag(id)
        if (!tag) {
            throw new createHttpError.NotFound()
        }

        return res.status(200).json(tag)
    } catch (error) {
        next(error)
    }
}

export const getTags = async (
    req: Request,
    res: Response,
    next: NextFunction
) => {
    try {
        const tags = await tagService.getTags()

        return res.status(200).json({
            metadata: {
                count: tags.length,
            },
            results: tags,
        })
    } catch (error) {
        next(error)
    }
}

type CreateTagSchema = z.infer<typeof createTagValidator>
export const createTag = async (
    req: Request<any, any, CreateTagSchema["body"]>,
    res: Response,
    next: NextFunction
) => {
    const { name, slug } = req.body
    try {
        const newTag = await tagService.createTag({ name, slug })

        return res.status(201).json(newTag)
    } catch (error) {
        if (error instanceof Error) {
            const handledError = handlePrismaKnownErrors(error)
            if (handledError instanceof UniqueConstraintError) {
                const message = `Tag with slug "${slug} already exists"`
                return next(
                    new ValidationError(message, 400, { slug: message })
                )
            }
        }
        next(error)
    }
}

type UpdateTagSchema = z.infer<typeof editTagValidator>
export const updateTag = async (
    req: Request<UpdateTagSchema["params"], any, UpdateTagSchema["body"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const { id } = req.params
        const { name, slug } = req.body
        const updatedTag = await tagService.updateTag(id, {
            name,
            slug,
        })

        return res.status(200).json(updatedTag)
    } catch (error) {
        next(error)
    }
}

type DeleteTagSchema = z.infer<typeof deleteTagValidator>
export const deleteTag = async (
    req: Request<DeleteTagSchema["params"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const { id } = req.params
        const deletedTag = await tagService.deleteTag(id)

        return res.status(200).json(deletedTag)
    } catch (error) {
        next(error)
    }
}
