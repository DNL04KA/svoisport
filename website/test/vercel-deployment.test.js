'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..');

test('Vercel routes activation endpoints to a persistent Neon handler', () => {
  const config = JSON.parse(fs.readFileSync(path.join(root, 'vercel.json'), 'utf8'));
  const sources = config.rewrites.map((route) => route.source);
  assert.ok(sources.includes('/api/create-activation-session.php'));
  assert.ok(sources.includes('/api/check-activation-session.php'));
  assert.ok(sources.includes('/api/activation-session.php'));
  assert.ok(sources.includes('/api/activate-session.php'));
  assert.ok(sources.includes('/api/check-subscription.php'));
  assert.ok(sources.includes('/api/devices.php'));
  assert.ok(sources.includes('/api/disconnect-device.php'));

  const handler = fs.readFileSync(path.join(root, 'api', 'activation.js'), 'utf8');
  assert.match(handler, /DATABASE_URL/);
  assert.match(handler, /activation_sessions/);
  assert.doesNotMatch(handler, /new Map\(/);
});

test('device management is scoped through the current linked device', () => {
  const handler = fs.readFileSync(path.join(root, 'api', 'activation.js'), 'utf8');
  assert.match(handler, /action === 'devices'/);
  assert.match(handler, /action === 'disconnect'/);
  assert.match(handler, /current_device_id/);
  assert.match(handler, /target_device_id/);
});

test('activation enforces a maximum of three TVs per account', () => {
  const handler = fs.readFileSync(path.join(root, 'api', 'activation.js'), 'utf8');
  assert.match(handler, /DEVICE_LIMIT\s*=\s*3/);
  assert.match(handler, /device_limit/);
  assert.match(handler, /COUNT\(\*\).*device_subscriptions/s);
  assert.match(handler, /pg_advisory_xact_lock/);
});
