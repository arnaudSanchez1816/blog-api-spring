import { Prisma } from "@prisma/client"
import { prisma } from "../config/prisma.js"
import { AlreadyExistsError } from "../helpers/errors.js"

export const getTag = async (tagIdOrSlug) => {
    const isSlug = typeof tagIdOrSlug === "string"

    const tag = await prisma.tag.findUnique({
        where: {
            ...(!isSlug && { id: tagIdOrSlug }),
            ...(isSlug && { slug: tagIdOrSlug }),
        },
    })

    return tag
}

export const getTags = async () => {
    const tags = await prisma.tag.findMany()
    return tags
}

export const createTag = async ({ name, slug }) => {
    try {
        const newTag = await prisma.tag.create({
            data: {
                name,
                slug,
            },
        })

        return newTag
    } catch (error) {
        if (
            error instanceof Prisma.PrismaClientKnownRequestError &&
            error.code === "P2002" &&
            error.meta?.target.includes("slug")
        ) {
            throw new AlreadyExistsError(`Tag slug '${slug}' already exists.`, {
                name: "slug",
                value: slug,
            })
        }
        throw error
    }
}

export const updateTag = async ({ id: tagIdOrSlug, name, slug }) => {
    const isSlug = typeof tagIdOrSlug === "string"
    const updatedTag = await prisma.tag.update({
        where: {
            ...(!isSlug && { id: tagIdOrSlug }),
            ...(isSlug && { slug: tagIdOrSlug }),
        },
        data: {
            name,
            slug,
        },
    })

    return updatedTag
}

export const deleteTag = async (tagIdOrSlug) => {
    const isSlug = typeof tagIdOrSlug === "string"
    const deletedTag = await prisma.tag.delete({
        where: {
            ...(!isSlug && { id: tagIdOrSlug }),
            ...(isSlug && { slug: tagIdOrSlug }),
        },
    })

    return deletedTag
}

export * as default from "./tagsService.js"
