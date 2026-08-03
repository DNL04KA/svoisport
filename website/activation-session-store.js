'use strict';

const crypto = require('crypto');

const SESSION_TTL_MS = 15 * 60 * 1000;
const SUBSCRIPTION_TTL_MS = 365 * 24 * 60 * 60 * 1000;
const PLANS = Object.freeze({
  month_1: Object.freeze({ id: 'month_1', title: '1 месяц', price: '9,99 BYN / мес', total: null }),
  month_3: Object.freeze({ id: 'month_3', title: '3 месяца', price: '8,49 BYN / мес', total: '25,49 BYN' }),
  month_12: Object.freeze({ id: 'month_12', title: '12 месяцев', price: '6,49 BYN / мес', total: '77,92 BYN' }),
});

class ActivationSessionStore {
  constructor(options = {}) {
    this.now = options.now || Date.now;
    this.id = options.id || (() => crypto.randomBytes(24).toString('base64url'));
    this.sessions = new Map();
    this.subscriptions = new Map();
  }

  createSession(deviceId, publicOrigin, planId = null) {
    if (!deviceId || typeof deviceId !== 'string') throw new TypeError('device_id is required');
    if (planId !== null && !PLANS[planId]) throw new TypeError('unknown plan');
    const sessionId = this.id();
    const expiresAtMs = this.now() + SESSION_TTL_MS;
    this.sessions.set(sessionId, { deviceId, planId, status: 'waiting', expiresAtMs });
    return {
      sessionId,
      qrUrl: `${publicOrigin}/activate.html?session=${encodeURIComponent(sessionId)}`,
      expiresAt: new Date(expiresAtMs).toISOString(),
    };
  }

  getSession(sessionId) {
    const session = this.sessions.get(sessionId);
    if (!session) return null;
    if (session.status === 'waiting' && this.now() > session.expiresAtMs) session.status = 'expired';
    return session;
  }

  getStatus(sessionId) {
    return this.getSession(sessionId)?.status || null;
  }

  getPublicSession(sessionId) {
    const session = this.getSession(sessionId);
    if (!session) return null;
    return {
      status: session.status,
      plan: session.planId ? PLANS[session.planId] : null,
    };
  }

  activateSession(sessionId) {
    const session = this.getSession(sessionId);
    if (!session) return null;
    if (session.status !== 'waiting') return session.status;
    session.status = 'activated';
    const untilMs = this.now() + SUBSCRIPTION_TTL_MS;
    this.subscriptions.set(session.deviceId, untilMs);
    return session.status;
  }

  getSubscription(deviceId) {
    const untilMs = this.subscriptions.get(deviceId);
    if (!untilMs || untilMs <= this.now()) return { active: false, until: null };
    return { active: true, until: new Date(untilMs).toISOString() };
  }
}

module.exports = { ActivationSessionStore, PLANS, SESSION_TTL_MS };
