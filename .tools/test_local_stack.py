import io
import unittest
from unittest import mock

import local_stack


class LocalStackTest(unittest.TestCase):
    def test_http_ok_uses_get_and_returns_true_on_200(self):
        with mock.patch.object(local_stack.verify_local_migration, "fetch", return_value=(200, "{}")) as fetch:
            self.assertTrue(local_stack.http_ok("http://127.0.0.1/test"))
        fetch.assert_called_once_with("http://127.0.0.1/test", method="GET")

    def test_http_ok_returns_false_on_exception(self):
        with mock.patch.object(local_stack.verify_local_migration, "fetch", side_effect=RuntimeError("boom")):
            self.assertFalse(local_stack.http_ok("http://127.0.0.1/test"))

    def test_ensure_prerequisites_reports_missing_redis_and_nacos(self):
        with mock.patch.object(local_stack, "port_is_open", side_effect=[False, False]):
            stdout = io.StringIO()
            with mock.patch("sys.stdout", stdout):
                exit_code = local_stack.ensure_prerequisites()

        output = stdout.getvalue()
        self.assertEqual(exit_code, 1)
        self.assertIn("Redis 未监听 6379", output)
        self.assertIn("Nacos 未监听 8848", output)

    def test_ensure_prerequisites_passes_when_ports_are_ready(self):
        with mock.patch.object(local_stack, "port_is_open", side_effect=[True, True]):
            self.assertEqual(local_stack.ensure_prerequisites(), 0)

    def test_main_routes_verify_command(self):
        with mock.patch.object(local_stack, "verify", return_value=0) as verify:
            exit_code = local_stack.main(["local_stack.py", "verify"])
        self.assertEqual(exit_code, 0)
        verify.assert_called_once_with()

    def test_main_routes_up_command(self):
        with mock.patch.object(local_stack, "start_nacos", return_value=0) as start_nacos:
            with mock.patch.object(local_stack, "start_services", return_value=0) as start_services:
                exit_code = local_stack.main(["local_stack.py", "up"])
        self.assertEqual(exit_code, 0)
        start_nacos.assert_called_once_with()
        start_services.assert_called_once_with()

    def test_main_aborts_up_when_nacos_fails(self):
        with mock.patch.object(local_stack, "start_nacos", return_value=1) as start_nacos:
            with mock.patch.object(local_stack, "start_services") as start_services:
                exit_code = local_stack.main(["local_stack.py", "up"])
        self.assertEqual(exit_code, 1)
        start_nacos.assert_called_once_with()
        start_services.assert_not_called()


if __name__ == "__main__":
    unittest.main()
