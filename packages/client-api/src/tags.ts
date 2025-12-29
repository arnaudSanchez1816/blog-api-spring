export interface TagDetails {
    id: number
    name: string
    slug: string
}

export interface FetchTagsResult {
    metadata: {
        count: number
    }
    results: TagDetails[]
}

export const fetchTags = async (): Promise<FetchTagsResult> => {
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./tags`, apiUrl)

    const response = await fetch(url, { mode: "cors" })
    if (!response.ok) {
        throw response
    }

    const tags = await response.json()

    return tags
}

export const deleteTag = async (
    idOrSlug: string | number,
    accessToken: string
): Promise<TagDetails> => {
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./tags/${idOrSlug}`, apiUrl)

    const response = await fetch(url, {
        mode: "cors",
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
        },
    })
    if (!response.ok) {
        throw response
    }

    const body = await response.json()
    return body
}

interface EditTagParams {
    name: string
    slug: string
}

export const editTag = async (
    { name, slug }: EditTagParams,
    idOrSlug: string | number,
    accessToken: string
): Promise<TagDetails> => {
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./tags/${idOrSlug}`, apiUrl)

    const response = await fetch(url, {
        mode: "cors",
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({ name, slug }),
    })
    if (!response.ok) {
        throw response
    }

    const updatedTag = await response.json()
    return updatedTag
}

interface CreateTagParams extends Pick<EditTagParams, "name" | "slug"> {
    name: string
    slug: string
}

export const createTag = async (
    { name, slug }: CreateTagParams,
    accessToken: string
): Promise<TagDetails> => {
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./tags`, apiUrl)

    const response = await fetch(url, {
        mode: "cors",
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({ name, slug }),
    })
    if (!response.ok) {
        throw response
    }

    const createdTag = await response.json()
    return createdTag
}
