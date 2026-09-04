import { test, expect } from '@playwright/test'

const demoPassword = '123456'

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
    await page.locator('#login-password').fill(demoPassword)
    await page.getByRole('button', { name: '登录系统' }).click()
    await expect(page).toHaveURL(role.route)
  })
}

test('管理员顶栏不请求学生通知接口', async ({ page }) => {
  const notificationRequests = []
  await page.route(/\/api\/(?:businessRecord\/list|chat\/notifications|callRecord\/list|repairRequest\/list|transferRequest\/list|visitorRecord\/list)(?:\?.*)?$/, async route => {
    notificationRequests.push(route.request().url())
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 200, data: [] })
    })
  })

  await page.goto('/login')
  await page.getByRole('button', { name: '管理员', exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()

  await expect(page).toHaveURL(/\/admin\/overview/)
  await page.waitForLoadState('networkidle')
  expect(notificationRequests).toEqual([])
})

test('学生登录后返回同角色命名空间内的受保护页面', async ({ page }) => {
  await page.goto('/student/repair?source=login')
  await expect(page).toHaveURL(/\/login\?redirect=/)

  await page.getByRole('button', { name: '学生', exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()

  await expect(page).toHaveURL('http://127.0.0.1:5173/student/repair?source=login')
})

test('记住账号会在刷新和主动退出后恢复角色与账号且不保存密码', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '宿管', exact: true }).click()
  await page.getByText('记住账号', { exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/dormmanager\/workbench/)

  const rememberedAccount = await page.evaluate(() => JSON.parse(localStorage.getItem('dorm-remembered-account')))
  expect(rememberedAccount).toEqual({ role: 'dormmanager', username: 'manager1' })
  expect(rememberedAccount).not.toHaveProperty('password')

  await page.locator('.user-profile').click()
  await page.getByText('退出登录', { exact: true }).click()
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('button', { name: '宿管', exact: true })).toHaveAttribute('aria-pressed', 'true')
  await expect(page.locator('#login-username')).toHaveValue('manager1')
  await expect(page.locator('#login-password')).toHaveValue('')
  await expect(page.getByRole('checkbox', { name: '记住账号' })).toBeChecked()

  await page.reload()
  await expect(page.getByRole('button', { name: '宿管', exact: true })).toHaveAttribute('aria-pressed', 'true')
  await expect(page.locator('#login-username')).toHaveValue('manager1')
  await expect(page.getByRole('checkbox', { name: '记住账号' })).toBeChecked()
})

test('取消记住账号后不再恢复上次登录身份', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '宿管', exact: true }).click()
  await page.getByText('记住账号', { exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/dormmanager\/workbench/)

  await page.locator('.user-profile').click()
  await page.getByText('退出登录', { exact: true }).click()
  await page.getByText('记住账号', { exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/dormmanager\/workbench/)
  expect(await page.evaluate(() => localStorage.getItem('dorm-remembered-account'))).toBeNull()

  await page.locator('.user-profile').click()
  await page.getByText('退出登录', { exact: true }).click()
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('button', { name: '学生', exact: true })).toHaveAttribute('aria-pressed', 'true')
  await expect(page.locator('#login-username')).toHaveValue('20240001')
  await expect(page.getByRole('checkbox', { name: '记住账号' })).not.toBeChecked()
})

test('401 自动退出保留当前受保护页面并允许重新登录返回', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '管理员', exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/admin\/overview/)

  await page.route('**/api/building/list**', route => route.fulfill({
    status: 401,
    contentType: 'application/json',
    body: JSON.stringify({ code: 401, message: '登录状态过期，请重新登录' })
  }))
  await page.goto('/admin/resources/buildings?source=expired')

  await expect(page).toHaveURL(/\/login\?redirect=/)
  expect(new URL(page.url()).searchParams.get('redirect')).toBe('/admin/resources/buildings?source=expired')

  await page.unroute('**/api/building/list**')
  await page.getByRole('button', { name: '管理员', exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL('http://127.0.0.1:5173/admin/resources/buildings?source=expired')
})

test('宿管备忘录页面显示正确页名', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '宿管', exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/dormmanager\/workbench/)

  await page.goto('/dormmanager/memos')
  await expect(page.locator('.breadcrumb')).toContainText('备忘录')
  await expect(page.locator('.breadcrumb')).not.toContainText('当前页面')
})

test('入住办理只展示可用房间和空闲床位', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '宿管', exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/dormmanager\/workbench/)

  const fulfill = (route, data) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({ code: 200, data })
  })
  await page.route('**/api/user/unassigned**', route => fulfill(route, [
    { id: 9001, name: '测试学生', gender: '男', username: 'test-student' }
  ]))
  await page.route('**/api/user/list**', route => fulfill(route, []))
  await page.route('**/api/building/list**', route => fulfill(route, [
    { id: 100, name: '测试男生楼', type: '男生宿舍' }
  ]))
  await page.route('**/api/room/list**', route => fulfill(route, [
    { id: 101, buildingId: 100, roomNumber: '维修房', status: 'MAINTENANCE' },
    { id: 102, buildingId: 100, roomNumber: '可用房', status: 'NORMAL' }
  ]))
  await page.route('**/api/bed/list**', route => {
    const roomId = new URL(route.request().url()).searchParams.get('roomId')
    return fulfill(route, roomId === '102' ? [
      { id: 201, roomId: 102, bedNumber: '损坏床', status: 'BROKEN', studentId: null },
      { id: 202, roomId: 102, bedNumber: '空床', status: 'EMPTY', studentId: null }
    ] : [])
  })

  await page.goto('/dormmanager/checkin')
  await page.getByRole('button', { name: '办理新入住' }).click()
  const formSelects = page.locator('.el-dialog .el-select')
  await formSelects.nth(0).click()
  await page.getByRole('option', { name: '测试学生 (男) - test-student' }).click()
  await formSelects.nth(1).click()
  await page.getByRole('option', { name: '测试男生楼' }).click()
  await formSelects.nth(2).click()
  await expect(page.getByRole('option', { name: '维修房' })).toHaveCount(0)
  await page.getByRole('option', { name: '可用房' }).click()
  await formSelects.nth(3).click()
  await expect(page.getByRole('option', { name: '损坏床号床' })).toHaveCount(0)
  await expect(page.getByRole('option', { name: '空床号床' })).toBeVisible()
})

for (const redirect of ['/student/repair', 'https://example.com/steal']) {
  test(`管理员登录忽略不安全 redirect：${redirect}`, async ({ page }) => {
    await page.goto(`/login?redirect=${encodeURIComponent(redirect)}`)
    await page.getByRole('button', { name: '管理员', exact: true }).click()
    await page.locator('#login-password').fill(demoPassword)
    await page.getByRole('button', { name: '登录系统' }).click()

    await expect(page).toHaveURL(/\/admin\/overview/)
  })
}

test('管理概览和角色数据范围正常', async ({ page }) => {
  const adminLogin = await page.request.post('/api/auth/login', {
    data: { username: 'admin', password: demoPassword, role: 'admin' }
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
    data: { username: 'manager1', password: demoPassword, role: 'dormmanager' }
  })
  const managerToken = (await managerLogin.json()).data.token
  const managerHeaders = { Authorization: `Bearer ${managerToken}` }
  const assigned = (await (await page.request.get('/api/building/list', { headers: managerHeaders })).json()).data
  const stats = (await (await page.request.get('/api/dashboard/buildings', { headers: managerHeaders })).json()).data
  expect(stats).toHaveLength(assigned.length)

  const studentLogin = await page.request.post('/api/auth/login', {
    data: { username: '20240001', password: demoPassword, role: 'student' }
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
  await page.locator('#login-password').fill(demoPassword)
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
  await page.locator('#login-password').fill(demoPassword)
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
  await page.locator('#login-password').fill(demoPassword)
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
  await page.locator('#login-password').fill(demoPassword)
  await page.getByRole('button', { name: '登录系统' }).click()
  await expect(page).toHaveURL(/\/dormmanager\/workbench/)

  await expect(page.getByText('AI巡查', { exact: true })).toHaveCount(0)
  await expect(page.getByText('AI 巡查报告', { exact: true })).toHaveCount(0)
})

test('管理员切换楼栋运营状态会保存到后端', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '管理员', exact: true }).click()
  await page.locator('#login-password').fill(demoPassword)
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
