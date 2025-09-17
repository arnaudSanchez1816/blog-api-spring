import { validateRequest } from "../middlewares/validator.js"
import {
    createTagValidator,
    deleteTagValidator,
    editTagValidator,
    getTagValidator,
} from "./tagsValidators.js"
import tagService from "./tagsService.js"
import createHttpError from "http-errors"
import { checkPermission } from "../middlewares/checkPermission.js"

export const getTag = [
    validateRequest(getTagValidator),
    async (req, res, next) => {
        try {
            const { id } = req.params
            const tag = await tagService.getTag(id)
            if (!tag) {
                throw new createHttpError.NotFound()
            }

            return res.json(tag)
        } catch (error) {
            next(error)
        }
    },
]

export const getTags = async (req, res, next) => {
    try {
        const tags = await tagService.getTags()

        return res.json({
            metadata: {
                count: tags.length,
            },
            results: tags,
        })
    } catch (error) {
        next(error)
    }
}

export const createTag = [
    checkPermission("CREATE"),
    validateRequest(createTagValidator),
    async (req, res, next) => {
        try {
            const { name, slug } = req.body
            const newTag = await tagService.createTag({ name, slug })

            return res.status(201).json(newTag)
        } catch (error) {
            next(error)
        }
    },
]

export const updateTag = [
    checkPermission("UPDATE"),
    validateRequest(editTagValidator),
    async (req, res, next) => {
        try {
            const { id } = req.params
            const { name, slug } = req.body
            const updatedTag = await tagService.updateTag({
                id,
                name,
                slug,
            })

            return res.status(200).json(updatedTag)
        } catch (error) {
            next(error)
        }
    },
]

export const deleteTag = [
    checkPermission("DELETE"),
    validateRequest(deleteTagValidator),
    async (req, res, next) => {
        try {
            const { id } = req.params
            await tagService.deleteTag(id)

            return res.status(204).send()
        } catch (error) {
            next(error)
        }
    },
]
