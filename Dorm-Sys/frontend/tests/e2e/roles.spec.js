import { test, expect } from '@playwright/test'

const roles = [
  { label: '学生', account: '20240001', route: /\/student\/desk/ },
  { label: '宿管', account: 'manager1', route: /\/dormmanager\/workbench/ },
  { label: '管理员', account: 'admin', route: /\/admin\/overview/ }
]

for (const role of roles) {
  test(`${role.label}演示账号可以登录`, async ({ page }) => {
    await page.goto('/login')
    await page.getByRole('button', { name: role.label, exact: true }).click()
    await expect(page.locator('#login-username')).toHaveValue(role.account)
    await page.getByRole('button', { name: '登录系统' }).click()
    await expect(page).toHaveURL(role.route)
  })
}

test('管理概览和角色数据范围正常', async ({ page }) => {
  await page.goto('/login')
  const password = await page.locator('#login-password').inputValue()

  const adminLogin = await page.request.post('/api/auth/login', {
    data: { username: 'admin', password, role: 'admin' }
  })
  expect(adminLogin.ok()).toBeTruthy()
  const adminToken = (await adminLogin.json()).data.token
  expect((await page.request.get('/api/dashboard/stats', {
    headers: { Authorization: `Bearer ${adminToken}` }
  })).status()).toBe(200)
  expect((await page.request.get('/api/dashboard/alerts', {
    headers: { Authorization: `Bearer ${adminToken}` }
  })).status()).toBe(200)

  const managerLogin = await page.request.post('/api/auth/login', {
    data: { username: 'manager1', password, role: 'dormmanager' }
  })
  const managerToken = (await managerLogin.json()).data.token
  const managerHeaders = { Authorization: `Bearer ${managerToken}` }
  const assigned = (await (await page.request.get('/api/building/list', { headers: managerHeaders })).json()).data
  const stats = (await (await page.request.get('/api/dashboard/buildings', { headers: managerHeaders })).json()).data
  expect(stats).toHaveLength(assigned.length)

  const studentLogin = await page.request.post('/api/auth/login', {
    data: { username: '20240001', password, role: 'student' }
  })
  const studentToken = (await studentLogin.json()).data.token
  expect((await page.request.get('/api/user/list', {
    headers: { Authorization: `Bearer ${studentToken}` }
  })).status()).toBe(403)
})

test('并发登录过期响应只提示一次', async ({ page }) => {
  const expiredResponse = {
    status: 401,
    contentType: 'application/json',
    body: JSON.stringify({ code: 401, message: '登录状态过期，请重新登录' })
  }

  await page.route('**/api/businessRecord/list**', route => route.fulfill(expiredResponse))
  await page.route('**/api/chat/notifications', route => route.fulfill(expiredResponse))

  await page.goto('/login')
  await page.getByRole('button', { name: '学生', exact: true }).click()
  await page.getByRole('button', { name: '登录系统' }).click()

  await expect(page).toHaveURL(/\/login/)
  await expect(page.locator('.el-message--error')).toHaveCount(1)
})

test('主动退出后忽略尚未完成请求的过期提示', async ({ page }) => {
  const delayedExpiredResponse = async route => {
    await new Promise(resolve => setTimeout(resolve, 800))
    await route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: JSON.stringify({ code: 401, message: '登录状态过期，请重新登录' })
    })
  }

  await page.route('**/api/businessRecord/list**', delayedExpiredResponse)
  await page.route('**/api/chat/notifications', delayedExpiredResponse)

  await page.goto('/login')
  await page.getByRole('button', { name: '学生', exact: true }).click()
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/student\/desk/)

  await page.locator('.user-profile').click()
  await page.getByText('退出登录', { exact: true }).click()

  await expect(page).toHaveURL(/\/login/)
  await expect(page.locator('.el-message--info')).toHaveCount(1)
  await page.waitForTimeout(1000)
  await expect(page.locator('.el-message--error')).toHaveCount(0)
})

test('管理员可以打开房间批量创建表单', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '管理员', exact: true }).click()
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/admin\/overview/)

  await page.goto('/admin/resources/rooms')
  await page.getByRole('button', { name: '批量创建' }).click()

  await expect(page.getByRole('dialog', { name: '批量创建房间' })).toBeVisible()
  await expect(page.getByText('每层房间数', { exact: true })).toBeVisible()
})

test('宿管端不再展示无实际智能能力的AI巡查', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '宿管', exact: true }).click()
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/dormmanager\/workbench/)

  await expect(page.getByText('AI巡查', { exact: true })).toHaveCount(0)
  await expect(page.getByText('AI 巡查报告', { exact: true })).toHaveCount(0)
})

test('管理员切换楼栋运营状态会保存到后端', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '管理员', exact: true }).click()
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/admin\/overview/)

  await page.route('**/api/building/list', route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      code: 200,
      data: [{ id: 1, name: '测试楼', type: '男生楼', floors: 6, manager: '王叔', location: '主校区', active: true }]
    })
  }))
  await page.route('**/api/room/list**', route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({ code: 200, data: [] })
  }))

  let submittedBuilding
  await page.route('**/api/building/save', async route => {
    submittedBuilding = route.request().postDataJSON()
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 200, data: true })
    })
  })

  await page.goto('/admin/resources/buildings')
  await expect(page.getByText('测试楼', { exact: true })).toBeVisible()
  await page.locator('.status-toggle .el-switch').click()

  await expect.poll(() => submittedBuilding).toMatchObject({ id: 1, active: false })
})
