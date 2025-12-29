import type { Prisma } from "@prisma/client"
import { prisma } from "../config/prisma.js"
import type { TagIdOrSlug } from "../types/tagIdOrSlug.js"

type UpdateTagDTO = Prisma.TagGetPayload<{
    select: {
        name: true
        slug: true
    }
}>

type CreateTagDTO = Prisma.TagGetPayload<{
    select: {
        name: true
        slug: true
    }
}>

export const getTag = async (tagIdOrSlug: TagIdOrSlug) => {
    const isSlug = typeof tagIdOrSlug === "string"
    let where: Prisma.TagWhereUniqueInput
    if (isSlug) {
        where = {
            slug: tagIdOrSlug,
        }
    } else {
        where = {
            id: tagIdOrSlug,
        }
    }

    const tag = await prisma.tag.findUnique({
        where,
    })

    return tag
}

export const getTags = async () => {
    const tags = await prisma.tag.findMany()
    return tags
}

export const createTag = async ({ name, slug }: CreateTagDTO) => {
    const newTag = await prisma.tag.create({
        data: {
            name,
            slug,
        },
    })

    return newTag
}

export const updateTag = async (
    tagIdOrSlug: TagIdOrSlug,
    { name, slug }: UpdateTagDTO
) => {
    const isSlug = typeof tagIdOrSlug === "string"
    let where: Prisma.TagWhereUniqueInput
    if (isSlug) {
        where = {
            slug: tagIdOrSlug,
        }
    } else {
        where = {
            id: tagIdOrSlug,
        }
    }

    const updatedTag = await prisma.tag.update({
        where,
        data: {
            name,
            slug,
        },
    })

    return updatedTag
}

export const deleteTag = async (tagIdOrSlug: TagIdOrSlug) => {
    const isSlug = typeof tagIdOrSlug === "string"
    let where: Prisma.TagWhereUniqueInput
    if (isSlug) {
        where = {
            slug: tagIdOrSlug,
        }
    } else {
        where = {
            id: tagIdOrSlug,
        }
    }

    const deletedTag = await prisma.tag.delete({
        where,
    })

    return deletedTag
}
