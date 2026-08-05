'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { ActivationSessionStore } = require('../activation-session-store');

test('creates a waiting session with a 15 minute expiry', () => {
  let now = Date.parse('2026-08-01T10:00:00Z');
  const store = new ActivationSessionStore({ now: () => now, id: () => 'session-1' });

  const session = store.createSession('tv-1', 'https://example.test', '36734');

  assert.deepEqual(session, {
    sessionId: 'session-1',
    qrUrl: 'https://sport-tv.by/payment/?id=36734',
    expiresAt: '2026-08-01T10:15:00.000Z',
  });
  assert.equal(store.getStatus('session-1'), 'waiting');
  assert.deepEqual(store.getPublicSession('session-1'), {
    status: 'waiting',
    plan: {
      id: '36734',
      title: '6 месяцев',
      price: '40.00 BYN',
      total: '40.00 BYN',
      paymentUrl: 'https://sport-tv.by/payment/?id=36734',
    },
  });
});

test('existing subscription activation keeps the session confirmation URL', () => {
  const store = new ActivationSessionStore({ now: () => 0, id: () => 'session-existing' });

  assert.equal(
    store.createSession('tv-1', 'https://example.test').qrUrl,
    'https://example.test/activate.html?session=session-existing'
  );
});

test('activates a waiting session exactly once', () => {
  const store = new ActivationSessionStore({ now: () => 0, id: () => 'session-1' });
  store.createSession('tv-1', 'https://example.test');

  assert.equal(store.activateSession('session-1'), 'activated');
  assert.equal(store.activateSession('session-1'), 'activated');
  assert.equal(store.getStatus('session-1'), 'activated');
  assert.equal(store.getSubscription('tv-1').active, true);
});

test('expires a waiting session after 15 minutes and refuses activation', () => {
  let now = 0;
  const store = new ActivationSessionStore({ now: () => now, id: () => 'session-1' });
  store.createSession('tv-1', 'https://example.test');
  now = 15 * 60 * 1000 + 1;

  assert.equal(store.getStatus('session-1'), 'expired');
  assert.equal(store.activateSession('session-1'), 'expired');
  assert.equal(store.getSubscription('tv-1').active, false);
});

test('returns null for an unknown session', () => {
  const store = new ActivationSessionStore();
  assert.equal(store.getStatus('missing'), null);
  assert.equal(store.activateSession('missing'), null);
});

test('rejects an unknown subscription plan', () => {
  const store = new ActivationSessionStore();
  assert.throws(
    () => store.createSession('tv-1', 'https://example.test', 'unknown'),
    /unknown plan/
  );
});
