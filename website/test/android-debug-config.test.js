'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

test('debug build permits cleartext only for local activation hosts', () => {
  const configPath = path.resolve(__dirname, '../../app/src/debug/res/xml/network_security_config.xml');
  const xml = fs.readFileSync(configPath, 'utf8');

  assert.match(xml, /cleartextTrafficPermitted="true"/);
  assert.match(xml, />10\.0\.2\.2</);
  assert.match(xml, />localhost</);
  assert.doesNotMatch(xml, /<base-config[^>]+cleartextTrafficPermitted="true"/);
});
