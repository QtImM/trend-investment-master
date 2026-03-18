import pathlib
import sys
import urllib.parse
import urllib.request


NACOS_ADDR = "http://127.0.0.1:8848"
GROUP = "DEFAULT_GROUP"
TEMPLATE_DIR = pathlib.Path("infra/nacos-config/templates")
CORE_DATA_IDS = [
    "gateway-service-dev.yaml",
    "market-data-service-dev.yaml",
    "trend-trading-backtest-service-dev.yaml",
    "trend-trading-backtest-view-dev.yaml",
]


def fetch_config(data_id: str) -> str:
    params = urllib.parse.urlencode({"dataId": data_id, "group": GROUP})
    url = f"{NACOS_ADDR}/nacos/v1/cs/configs?{params}"
    with urllib.request.urlopen(url, timeout=10) as response:
        return response.read().decode("utf-8")


def publish_config(data_id: str, content: str) -> str:
    payload = urllib.parse.urlencode(
        {
            "dataId": data_id,
            "group": GROUP,
            "content": content,
            "type": "yaml",
        }
    ).encode("utf-8")
    request = urllib.request.Request(
        f"{NACOS_ADDR}/nacos/v1/cs/configs",
        data=payload,
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        return response.read().decode("utf-8")


def sync_data_id(data_id: str) -> None:
    template_path = TEMPLATE_DIR / data_id
    content = template_path.read_text(encoding="utf-8")
    result = publish_config(data_id, content)
    print(f"{data_id}: {result}")


def sync_core() -> None:
    for data_id in CORE_DATA_IDS:
        sync_data_id(data_id)


def usage() -> None:
    print("Usage:")
    print("  python .tools/nacos_config_sync.py get <dataId>")
    print("  python .tools/nacos_config_sync.py put <dataId>")
    print("  python .tools/nacos_config_sync.py sync-core")


def main() -> int:
    if len(sys.argv) < 2:
        usage()
        return 1

    command = sys.argv[1]
    if command == "sync-core":
        sync_core()
        return 0

    if len(sys.argv) != 3:
        usage()
        return 1

    data_id = sys.argv[2]
    if command == "get":
        print(fetch_config(data_id))
        return 0
    if command == "put":
        sync_data_id(data_id)
        return 0

    usage()
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
