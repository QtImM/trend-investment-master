import json
import sys
import urllib.error
import urllib.parse
import urllib.request


NACOS_ADDR = "http://127.0.0.1:8848"
GROUP = "DEFAULT_GROUP"
CORE_SERVICES = {
    "gateway-service": 8032,
    "market-data-service": 8061,
    "trend-trading-backtest-service": 8051,
    "trend-trading-backtest-view": 8041,
}
CORE_DATA_IDS = [
    "gateway-service-dev.yaml",
    "market-data-service-dev.yaml",
    "trend-trading-backtest-service-dev.yaml",
    "trend-trading-backtest-view-dev.yaml",
]
HTTP_CHECKS = [
    ("trend-web", "http://127.0.0.1:8032/trend-web/", 200),
    ("api-view-health", "http://127.0.0.1:8032/api-view/actuator/health", 200),
    ("api-backtest-health", "http://127.0.0.1:8032/api-backtest/actuator/health", 200),
    ("api-market-codes", "http://127.0.0.1:8032/api-market/codes", 200),
    (
        "api-backtest-simulate",
        "http://127.0.0.1:8032/api-backtest/simulate/000300/5/1.01/0.95/0.001/2018-01-01/2019-05-09",
        200,
    ),
]


def fetch(url: str, method: str = "GET", data: bytes | None = None) -> tuple[int, str]:
    request = urllib.request.Request(url, data=data, method=method)
    with urllib.request.urlopen(request, timeout=10) as response:
        return response.getcode(), response.read().decode("utf-8")


def verify_nacos_service(service_name: str, expected_port: int) -> None:
    params = urllib.parse.urlencode(
        {"serviceName": service_name, "groupName": GROUP, "healthyOnly": "false"}
    )
    code, body = fetch(f"{NACOS_ADDR}/nacos/v1/ns/instance/list?{params}")
    if code != 200:
        raise RuntimeError(f"Nacos instance list request failed for {service_name}: {code}")
    payload = json.loads(body)
    hosts = payload.get("hosts") or []
    if not hosts:
        raise RuntimeError(f"{service_name} has no hosts in Nacos")
    healthy_ports = [host["port"] for host in hosts if host.get("healthy")]
    if expected_port not in healthy_ports:
        raise RuntimeError(
            f"{service_name} missing healthy host on expected port {expected_port}: {healthy_ports}"
        )


def verify_data_id(data_id: str) -> None:
    params = urllib.parse.urlencode({"dataId": data_id, "group": GROUP})
    code, body = fetch(f"{NACOS_ADDR}/nacos/v1/cs/configs?{params}")
    if code != 200:
        raise RuntimeError(f"Config fetch failed for {data_id}: {code}")
    if not body.strip():
        raise RuntimeError(f"Config content is empty for {data_id}")


def verify_http_check(name: str, url: str, expected_status: int) -> None:
    code, body = fetch(url)
    if code != expected_status:
        raise RuntimeError(f"{name} returned {code}, expected {expected_status}")
    if name == "api-backtest-simulate":
        payload = json.loads(body)
        if "trades" not in payload or "profits" not in payload:
            raise RuntimeError("api-backtest-simulate payload missing core fields")


def main() -> int:
    checks: list[str] = []

    try:
        code, _ = fetch(f"{NACOS_ADDR}/nacos/")
        if code not in (200, 404):
            raise RuntimeError(f"Unexpected Nacos base status: {code}")
        checks.append("Nacos base reachable")

        for service_name, expected_port in CORE_SERVICES.items():
            verify_nacos_service(service_name, expected_port)
            checks.append(f"Nacos service healthy: {service_name}")

        for data_id in CORE_DATA_IDS:
            verify_data_id(data_id)
            checks.append(f"Nacos config exists: {data_id}")

        for name, url, expected_status in HTTP_CHECKS:
            verify_http_check(name, url, expected_status)
            checks.append(f"HTTP endpoint ok: {name}")
    except (urllib.error.URLError, urllib.error.HTTPError, RuntimeError, json.JSONDecodeError) as exc:
        print("VERIFICATION FAILED")
        print(str(exc))
        return 1

    print("VERIFICATION PASSED")
    for item in checks:
        print(f"- {item}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
