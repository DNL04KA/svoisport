'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { createAppServer } = require('../serve');

async function withServer(run) {
  const server = createAppServer();
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const { port } = server.address();
  try {
    await run(`http://127.0.0.1:${port}`);
  } finally {
    await new Promise((resolve, reject) => server.close((error) => error ? reject(error) : resolve()));
  }
}

test('creates, confirms, and reports an activation session', async () => {
  await withServer(async (base) => {
    const createdResponse = await fetch(`${base}/api/create-activation-session.php`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ device_id: 'tv-integration', plan_id: '36735' }),
    });
    assert.equal(createdResponse.status, 201);
    const created = await createdResponse.json();
    assert.match(created.sessionId, /^[A-Za-z0-9_-]{20,}$/);
    assert.equal(created.qrUrl, `${base}/activate.html?session=${created.sessionId}`);

    const publicSession = await fetch(`${base}/api/activation-session.php?session=${encodeURIComponent(created.sessionId)}`);
    assert.deepEqual(await publicSession.json(), {
      status: 'waiting',
      plan: {
        id: '36735',
        title: '12 месяцев',
        price: null,
        total: null,
        paymentUrl: 'https://sport-tv.by/payment/?id=36735',
      },
    });

    const waiting = await fetch(`${base}/api/check-activation-session.php?sessionId=${encodeURIComponent(created.sessionId)}`);
    assert.deepEqual(await waiting.json(), { status: 'waiting' });

    const page = await fetch(`${base}/activate.html?session=${encodeURIComponent(created.sessionId)}`);
    assert.equal(page.status, 200);
    assert.match(await page.text(), /Подтвердить активацию/);

    const activatedResponse = await fetch(`${base}/api/activate-session.php`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ session: created.sessionId, email: 'viewer@example.com' }),
    });
    assert.equal(activatedResponse.status, 200);
    assert.deepEqual(await activatedResponse.json(), { status: 'activated' });

    const activated = await fetch(`${base}/api/check-activation-session.php?sessionId=${encodeURIComponent(created.sessionId)}`);
    assert.deepEqual(await activated.json(), { status: 'activated' });
  });
});

test('rejects activation without a valid session', async () => {
  await withServer(async (base) => {
    const response = await fetch(`${base}/api/activate-session.php`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ session: 'missing' }),
    });
    assert.equal(response.status, 404);
  });
});

test('requires an account e-mail before activating a session', async () => {
  await withServer(async (base) => {
    const created = await fetch(`${base}/api/create-activation-session.php`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ device_id: 'tv-email-check' }),
    }).then((response) => response.json());

    const response = await fetch(`${base}/api/activate-session.php`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ session: created.sessionId }),
    });
    assert.equal(response.status, 400);
    assert.deepEqual(await response.json(), { error: 'valid email is required' });
  });
});
