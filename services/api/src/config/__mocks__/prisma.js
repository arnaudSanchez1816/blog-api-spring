import { beforeEach } from "vitest"
import { mockDeep, mockReset } from "vitest-mock-extended"
import { vi } from "vitest"
import { prisma } from "config/prisma.js"

beforeEach(() => {
    mockReset(prisma)
})

vi.mock("config/prisma.js", () => ({ prisma: mockDeep() }))
export default prisma
