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

  const handler = fs.readFileSync(path.join(root, 'api', 'activation.js'), 'utf8');
  assert.match(handler, /DATABASE_URL/);
  assert.match(handler, /activation_sessions/);
  assert.doesNotMatch(handler, /new Map\(/);
});
