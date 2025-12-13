import { beforeEach } from "vitest"
import { mockDeep, mockReset } from "vitest-mock-extended"
import { vi } from "vitest"
import { prisma } from "../prisma.js"

vi.mock("../prisma.js", () => ({ prisma: mockDeep() }))

beforeEach(() => {
    mockReset(prismaMock)
})

const prismaMock = prisma

export default prismaMock
