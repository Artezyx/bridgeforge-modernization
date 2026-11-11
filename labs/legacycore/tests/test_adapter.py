import json
import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROJECT = ROOT / "LegacyCore" / "LegacyCore.csproj"
EXAMPLE = ROOT / "examples" / "customer.xml"


def run(source: Path):
    return subprocess.run([
        "dotnet", "run", "--no-build", "--project", str(PROJECT), "--", str(source)
    ], cwd=ROOT, capture_output=True, text=True, check=False)


class SoapAdapterTests(unittest.TestCase):
    def test_valid_soap_payload(self):
        result = run(EXAMPLE)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(json.loads(result.stdout), {
            "id": 501, "fullName": "Camila Torres", "email": "camila@example.org"
        })

    def test_external_entity_payload_is_blocked(self):
        payload = '''<!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
<soap:Body><Customer><Id>1</Id><FullName>&xxe;</FullName>
<Email>a@example.org</Email></Customer></soap:Body></soap:Envelope>'''
        with tempfile.TemporaryDirectory() as d:
            path = Path(d) / "unsafe.xml"
            path.write_text(payload, encoding="utf-8")
            result = run(path)
            self.assertEqual(result.returncode, 2)
            self.assertNotIn("root:", result.stdout)

    def test_missing_customer_rejected(self):
        payload = '''<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
<soap:Body><GetCustomerResponse/></soap:Body></soap:Envelope>'''
        with tempfile.TemporaryDirectory() as d:
            path = Path(d) / "missing.xml"
            path.write_text(payload, encoding="utf-8")
            result = run(path)
            self.assertEqual(result.returncode, 2)
            self.assertIn("Customer", result.stderr)


if __name__ == "__main__":
    unittest.main()
