'use strict';

const crypto = require('node:crypto');
const { neon } = require('@neondatabase/serverless');
const DEVICE_LIMIT = 3;

const PLANS = {
  month_1: { id: 'month_1', title: '1 месяц', price: '9,99 BYN / мес', total: null, days: 30 },
  month_3: { id: 'month_3', title: '3 месяца', price: '8,49 BYN / мес', total: '25,49 BYN', days: 90 },
  month_12: { id: 'month_12', title: '12 месяцев', price: '6,49 BYN / мес', total: '77,92 BYN', days: 365 },
};

async function prepare(sql) {
  await sql`CREATE TABLE IF NOT EXISTS activation_sessions (
    session_id TEXT PRIMARY KEY,
    device_id TEXT NOT NULL,
    plan_id TEXT,
    email TEXT,
    status TEXT NOT NULL DEFAULT 'waiting',
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
  )`;
  await sql`CREATE TABLE IF NOT EXISTS subscriptions (
    email TEXT PRIMARY KEY,
    active_until TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
  )`;
  await sql`CREATE TABLE IF NOT EXISTS device_subscriptions (
    device_id TEXT PRIMARY KEY,
    email TEXT NOT NULL,
    active_until TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
  )`;
}

function send(res, status, data) {
  res.setHeader('Cache-Control', 'no-store');
  res.setHeader('X-Content-Type-Options', 'nosniff');
  return res.status(status).json(data);
}

module.exports = async function handler(req, res) {
  if (!process.env.DATABASE_URL) return send(res, 503, { error: 'database is not configured' });
  const sql = neon(process.env.DATABASE_URL);
  await prepare(sql);
  const action = req.query.action;

  if (action === 'create' && req.method === 'POST') {
    const { device_id: deviceId, plan_id: planId = null } = req.body || {};
    if (typeof deviceId !== 'string' || !deviceId) return send(res, 400, { error: 'device_id is required' });
    if (planId !== null && !PLANS[planId]) return send(res, 400, { error: 'unknown plan' });
    const sessionId = crypto.randomBytes(24).toString('base64url');
    const expiresAt = new Date(Date.now() + 15 * 60 * 1000);
    await sql`INSERT INTO activation_sessions (session_id, device_id, plan_id, expires_at)
      VALUES (${sessionId}, ${deviceId}, ${planId}, ${expiresAt.toISOString()})`;
    const origin = `https://${req.headers['x-forwarded-host'] || req.headers.host}`;
    return send(res, 201, { sessionId, qrUrl: `${origin}/activate.html?session=${encodeURIComponent(sessionId)}`, expiresAt: expiresAt.toISOString() });
  }

  const sessionId = String(req.query.sessionId || req.query.session || req.body?.session || '');
  if (['status', 'public', 'activate'].includes(action)) {
    await sql`UPDATE activation_sessions SET status = 'expired'
      WHERE session_id = ${sessionId} AND status = 'waiting' AND expires_at < NOW()`;
    const rows = await sql`SELECT * FROM activation_sessions WHERE session_id = ${sessionId} LIMIT 1`;
    if (!rows[0]) return send(res, 404, { error: 'session not found' });
    const session = rows[0];
    if (action === 'status' && req.method === 'GET') return send(res, 200, { status: session.status });
    if (action === 'public' && req.method === 'GET') {
      const plan = session.plan_id ? PLANS[session.plan_id] : null;
      return send(res, 200, { status: session.status, plan: plan ? { id: plan.id, title: plan.title, price: plan.price, total: plan.total } : null });
    }
    if (action === 'activate' && req.method === 'POST') {
      if (session.status === 'expired') return send(res, 410, { status: 'expired' });
      const email = String(req.body?.email || '').trim().toLowerCase();
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return send(res, 400, { error: 'valid email is required' });
      let until;
      if (session.plan_id) {
        const days = PLANS[session.plan_id].days;
        until = new Date(Date.now() + days * 86400000).toISOString();
        // TODO(bePaid): replace this test confirmation with a verified bePaid webhook.
        await sql`INSERT INTO subscriptions (email, active_until) VALUES (${email}, ${until})
          ON CONFLICT (email) DO UPDATE SET active_until = EXCLUDED.active_until, updated_at = NOW()`;
      } else {
        const subscriptions = await sql`SELECT active_until FROM subscriptions WHERE email = ${email} AND active_until > NOW()`;
        if (!subscriptions[0]) return send(res, 402, { error: 'active subscription not found' });
        until = subscriptions[0].active_until;
      }
      const linked = await sql`WITH account_lock AS (
          SELECT pg_advisory_xact_lock(hashtext(${email}))
        ), active_devices AS (
          SELECT COUNT(*)::int AS count FROM device_subscriptions, account_lock
          WHERE email = ${email} AND active_until > NOW() AND device_id <> ${session.device_id}
        )
        INSERT INTO device_subscriptions (device_id, email, active_until)
        SELECT ${session.device_id}, ${email}, ${until} FROM active_devices
        WHERE count < ${DEVICE_LIMIT}
        ON CONFLICT (device_id) DO UPDATE SET email = EXCLUDED.email, active_until = EXCLUDED.active_until, updated_at = NOW()
        RETURNING device_id`;
      if (!linked[0]) {
        await sql`UPDATE activation_sessions SET status = 'device_limit', email = ${email} WHERE session_id = ${sessionId}`;
        return send(res, 409, { status: 'device_limit', error: 'maximum of 3 TVs per subscription' });
      }
      await sql`UPDATE activation_sessions SET status = 'activated', email = ${email}
        WHERE session_id = ${sessionId} AND status = 'waiting'`;
      return send(res, 200, { status: 'activated' });
    }
  }

  if (action === 'subscription' && req.method === 'GET') {
    const deviceId = String(req.query.device_id || '');
    if (!deviceId) return send(res, 400, { error: 'device_id is required' });
    const rows = await sql`SELECT active_until FROM device_subscriptions WHERE device_id = ${deviceId} AND active_until > NOW()`;
    return send(res, 200, { active: Boolean(rows[0]), until: rows[0]?.active_until || null });
  }
  return send(res, 405, { error: 'method not allowed' });
};
