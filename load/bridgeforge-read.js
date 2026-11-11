import http from 'k6/http';
import encoding from 'k6/encoding';
import { check, sleep } from 'k6';

export const options = {
  scenarios: { readOnly: { executor: 'constant-vus', vus: 8, duration: '30s' } },
  thresholds: { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<700'] },
};

export default function () {
  const url = __ENV.BASE_URL || 'http://localhost:8082';
  if (!__ENV.READER_PASSWORD || !__ENV.CUSTOMER_ID)
    throw new Error('Set READER_PASSWORD and an existing CUSTOMER_ID in your disposable database');
  const username = __ENV.READER_USER || 'portfolio-reader';
  const auth = encoding.b64encode(username + ':' + __ENV.READER_PASSWORD);
  const response = http.get(url + '/api/v1/customers/' + __ENV.CUSTOMER_ID, {
    headers: { Authorization: 'Basic ' + auth },
  });
  check(response, { 'read succeeds': r => r.status === 200 });
  sleep(0.2);
}
