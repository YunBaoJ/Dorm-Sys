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
