import { beforeEach } from "vitest"
import { exec } from "child_process"
import { promisify } from "util"

const execPromisify = promisify(exec)

beforeEach(async () => {
    await resetDb()
})

async function resetDb() {
    await execPromisify("pnpm prisma migrate reset --force --skip-seed")
}
