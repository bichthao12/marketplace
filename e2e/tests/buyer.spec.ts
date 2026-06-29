import { test, expect } from '@playwright/test';

test('home page loads', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('body')).toBeVisible();
});

test('products page loads', async ({ page }) => {
  await page.goto('/products');
  await expect(page.getByRole('heading').first()).toBeVisible();
});

test('register page loads', async ({ page }) => {
  await page.goto('/register');
  await expect(page.getByRole('button')).toBeVisible();
});
