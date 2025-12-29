import { expect } from "vitest"
import { api, v1Api } from "./supertest.js"
import jwt, { type SignOptions } from "jsonwebtoken"
import prisma from "./prisma.js"
import type { PostDetails } from "#posts/postsService.js"
import type { Prisma } from "@prisma/client"
import type { ApiUser } from "#types/apiUser.js"

export const testAuthenticationHeader = async (
    route: string,
    user: Pick<ApiUser, "id" | "email" | "name">
) => {
    const token = generateAccessToken(user)

    // No header
    let { status } = await api.post(v1Api(route))
    expect(status).toBe(401)

    // Bad header formatting
    ;({ status } = await api
        .post(v1Api(route))
        .set("Authorization", `beeeerer ${token}`))

    expect(status).toBe(401)
    ;({ status } = await api
        .post(v1Api(route))
        .set("Authorization", `Bearer${token}`))

    expect(status).toBe(401)
    ;({ status } = await api
        .post(v1Api(route))
        .set("Authorization", `${token}`))

    expect(status).toBe(401)

    const expiredToken = generateAccessToken(user, { expiresIn: "100 ms" })
    await new Promise((res) => setTimeout(res, 200))
    ;({ status } = await api
        .post(v1Api(route))
        .auth(expiredToken, { type: "bearer" }))

    expect(status).toBe(401)
}

export const testPermissions = async (
    route: string,
    user: Pick<ApiUser, "id" | "email" | "name">
) => {
    const token = generateAccessToken(user)

    const { status } = await api
        .post(v1Api(route))
        .auth(token, { type: "bearer" })

    expect(status).toBe(403)
}

interface GenerateAccessTokenOptions {
    expiresIn?: SignOptions["expiresIn"]
}
export const generateAccessToken = (
    { id, name, email }: Pick<ApiUser, "id" | "email" | "name">,
    { expiresIn = "10 minutes" }: GenerateAccessTokenOptions = {}
) => {
    const token = jwt.sign(
        {
            sub: id,
            name,
            email,
        },
        process.env.JWT_ACCESS_SECRET!,
        {
            expiresIn,
            algorithm: "HS256",
        }
    )

    return token
}

export const createPosts = async (
    data: Prisma.PostCreateManyAndReturnArgs["data"]
) => {
    const posts = await prisma.post.createManyAndReturn({
        data,
        include: {
            author: {
                select: {
                    id: true,
                    name: true,
                },
            },
        },
        omit: {
            body: true,
            authorId: true,
        },
    })

    return posts.map((p) => ({
        ...p,
        publishedAt: p.publishedAt != null ? p.publishedAt.toISOString() : null,
    }))
}

interface GeneratePostDetailsParams
    extends Partial<
        Prisma.PostGetPayload<{
            include: {
                author: true
                tags: true
                comments: true
            }
        }>
    > {
    commentsCount?: number
}

export const generatePostDetails = (
    data: GeneratePostDetailsParams
): PostDetails => {
    return {
        id: data.id ?? 1,
        author: data.author ?? {
            id: data.authorId ?? 1,
            name: "username",
        },
        body: data.body ?? "content",
        title: data.title ?? "title",
        readingTime: data.readingTime ?? 1,
        description: data.description ?? "description",
        publishedAt: data.publishedAt ?? null,
        tags: data.tags ?? [
            {
                id: 1,
                name: "tag-name",
                slug: "tag-slug",
            },
        ],
        comments: data.comments ?? [],
        commentsCount: data.commentsCount ?? 0,
    }
}
