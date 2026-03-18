import io
import json
import unittest
from unittest import mock

import verify_local_migration


class VerifyLocalMigrationTest(unittest.TestCase):
    def test_verify_nacos_service_accepts_expected_healthy_port(self):
        payload = json.dumps({"hosts": [{"healthy": True, "port": 8032}]})
        with mock.patch.object(verify_local_migration, "fetch", return_value=(200, payload)):
            verify_local_migration.verify_nacos_service("gateway-service", 8032)

    def test_verify_nacos_service_rejects_missing_hosts(self):
        payload = json.dumps({"hosts": []})
        with mock.patch.object(verify_local_migration, "fetch", return_value=(200, payload)):
            with self.assertRaisesRegex(RuntimeError, "has no hosts"):
                verify_local_migration.verify_nacos_service("gateway-service", 8032)

    def test_verify_data_id_rejects_empty_content(self):
        with mock.patch.object(verify_local_migration, "fetch", return_value=(200, "   ")):
            with self.assertRaisesRegex(RuntimeError, "Config content is empty"):
                verify_local_migration.verify_data_id("gateway-service-dev.yaml")

    def test_verify_http_check_requires_core_fields_for_backtest_simulate(self):
        with mock.patch.object(verify_local_migration, "fetch", return_value=(200, json.dumps({"ok": True}))):
            with self.assertRaisesRegex(RuntimeError, "payload missing core fields"):
                verify_local_migration.verify_http_check("api-backtest-simulate", "http://127.0.0.1/test", 200)

    def test_main_prints_passed_when_all_checks_succeed(self):
        calls = []

        def fake_fetch(url: str, method: str = "GET", data: bytes | None = None):
            calls.append(url)
            if url.endswith("/nacos/"):
                return 404, ""
            if "/nacos/v1/ns/instance/list?" in url:
                return 200, json.dumps({"hosts": [{"healthy": True, "port": 8032}, {"healthy": True, "port": 8061}, {"healthy": True, "port": 8051}, {"healthy": True, "port": 8041}]})
            if "/nacos/v1/cs/configs?" in url:
                return 200, "server:\n  port: 8032\n"
            if "api-backtest/simulate" in url:
                return 200, json.dumps({"trades": [], "profits": []})
            return 200, "{}"

        stdout = io.StringIO()
        with mock.patch.object(verify_local_migration, "fetch", side_effect=fake_fetch):
            with mock.patch("sys.stdout", stdout):
                exit_code = verify_local_migration.main()

        output = stdout.getvalue()
        self.assertEqual(exit_code, 0)
        self.assertIn("VERIFICATION PASSED", output)
        self.assertIn("HTTP endpoint ok: api-backtest-simulate", output)
        self.assertTrue(any(url.endswith("/nacos/") for url in calls))

    def test_main_prints_failed_when_service_check_breaks(self):
        stdout = io.StringIO()
        with mock.patch.object(
            verify_local_migration,
            "fetch",
            side_effect=[
                (404, ""),
                (200, json.dumps({"hosts": []})),
            ],
        ):
            with mock.patch("sys.stdout", stdout):
                exit_code = verify_local_migration.main()

        output = stdout.getvalue()
        self.assertEqual(exit_code, 1)
        self.assertIn("VERIFICATION FAILED", output)
        self.assertIn("gateway-service has no hosts in Nacos", output)


if __name__ == "__main__":
    unittest.main()
