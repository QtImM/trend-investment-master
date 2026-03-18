import socket
import subprocess
import sys
import time
from dataclasses import dataclass
from pathlib import Path

import verify_local_migration


DETACHED_PROCESS = 0x00000008
CREATE_NEW_PROCESS_GROUP = 0x00000200
ROOT = Path(__file__).resolve().parents[1]
TOOLS_DIR = ROOT / ".tools"
MAVEN_CMD = TOOLS_DIR / "apache-maven-3.9.9" / "bin" / "mvn.cmd"
NACOS_HOME = Path(r"C:\Tools\nacos-2.4.3\nacos")


@dataclass(frozen=True)
class ManagedService:
    name: str
    port: int
    health_url: str
    log_path: Path
    start_command: list[str]
    workdir: Path


SERVICES = [
    ManagedService(
        name="market-data-service",
        port=8061,
        health_url="http://127.0.0.1:8061/codes",
        log_path=TOOLS_DIR / "market-data-nacos.log",
        start_command=[
            str(MAVEN_CMD),
            "-pl",
            "market-data-service",
            "spring-boot:run",
            "-Dspring-boot.run.profiles=nacos",
        ],
        workdir=ROOT,
    ),
    ManagedService(
        name="trend-trading-backtest-service",
        port=8051,
        health_url="http://127.0.0.1:8051/actuator/health",
        log_path=TOOLS_DIR / "backtest-service-nacos.log",
        start_command=[
            "java",
            "-jar",
            str(ROOT / "trend-trading-backtest-service" / "target" / "trend-trading-backtest-service-1.0-SNAPSHOT.jar"),
            "--spring.profiles.active=nacos",
        ],
        workdir=ROOT,
    ),
    ManagedService(
        name="trend-trading-backtest-view",
        port=8041,
        health_url="http://127.0.0.1:8041/actuator/health",
        log_path=TOOLS_DIR / "backtest-view-nacos.log",
        start_command=[
            "java",
            "-jar",
            str(ROOT / "trend-trading-backtest-view" / "target" / "trend-trading-backtest-view-1.0-SNAPSHOT.jar"),
            "--spring.profiles.active=nacos",
        ],
        workdir=ROOT,
    ),
    ManagedService(
        name="gateway-service",
        port=8032,
        health_url="http://127.0.0.1:8032/actuator/health",
        log_path=TOOLS_DIR / "gateway-nacos.log",
        start_command=[
            "java",
            "-jar",
            str(ROOT / "gateway-service" / "target" / "gateway-service-1.0-SNAPSHOT.jar"),
            "--spring.profiles.active=nacos",
        ],
        workdir=ROOT,
    ),
]


def port_is_open(port: int) -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.settimeout(1)
        return sock.connect_ex(("127.0.0.1", port)) == 0


def http_ok(url: str) -> bool:
    try:
        code, _ = verify_local_migration.fetch(url, method="GET")
        return code == 200
    except Exception:
        return False


def wait_port(port: int, timeout_seconds: int = 45) -> bool:
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        if port_is_open(port):
            return True
        time.sleep(1)
    return False


def find_listening_pid(port: int) -> int | None:
    result = subprocess.run(
        ["netstat", "-ano", "-p", "tcp"],
        capture_output=True,
        text=True,
        check=True,
        cwd=ROOT,
    )
    for line in result.stdout.splitlines():
        parts = line.split()
        if len(parts) >= 5 and parts[0] == "TCP" and parts[1].endswith(f":{port}") and parts[3] == "LISTENING":
            try:
                return int(parts[4])
            except ValueError:
                return None
    return None


def task_name_for_pid(pid: int) -> str:
    result = subprocess.run(
        ["tasklist", "/FI", f"PID eq {pid}", "/FO", "CSV", "/NH"],
        capture_output=True,
        text=True,
        check=True,
        cwd=ROOT,
    )
    line = result.stdout.strip()
    if not line or line.startswith("INFO:"):
        return ""
    return line.split(",")[0].strip('"')


def stop_pid(pid: int) -> None:
    subprocess.run(["taskkill", "/PID", str(pid), "/F"], check=True, cwd=ROOT, capture_output=True, text=True)


def spawn_process(command: list[str], workdir: Path, log_path: Path) -> None:
    log_path.parent.mkdir(parents=True, exist_ok=True)
    with log_path.open("ab") as log_file:
        subprocess.Popen(
            command,
            cwd=workdir,
            stdout=log_file,
            stderr=subprocess.STDOUT,
            creationflags=DETACHED_PROCESS | CREATE_NEW_PROCESS_GROUP,
        )


def ensure_prerequisites() -> int:
    issues: list[str] = []
    if not port_is_open(6379):
        issues.append("Redis 未监听 6379，请先启动本机 Redis。")
    if not port_is_open(8848):
        issues.append(
            f"Nacos 未监听 8848，请先执行 {NACOS_HOME}\\bin\\startup.cmd -m standalone，或先用本脚本 start-nacos。"
        )
    if issues:
        print("前置条件未满足：")
        for issue in issues:
            print(f"- {issue}")
        return 1
    return 0


def start_nacos() -> int:
    if port_is_open(8848):
        print("Nacos 已在 8848 运行。")
        return 0
    nacos_bin = NACOS_HOME / "bin"
    subprocess.run(
        ["cmd", "/c", "startup.cmd", "-m", "standalone"],
        cwd=nacos_bin,
        check=True,
        capture_output=True,
        text=True,
    )
    if wait_port(8848, 30):
        print(f"Nacos 已启动：{NACOS_HOME}")
        return 0
    print("Nacos 启动超时，请查看 Nacos 日志。")
    return 1


def stop_nacos() -> int:
    if not port_is_open(8848):
        print("Nacos 当前未运行。")
        return 0
    nacos_bin = NACOS_HOME / "bin"
    subprocess.run(["cmd", "/c", "shutdown.cmd"], cwd=nacos_bin, check=True, capture_output=True, text=True)
    for _ in range(20):
        if not port_is_open(8848):
            print("Nacos 已停止。")
            return 0
        time.sleep(1)
    print("Nacos 停止超时，请手动检查。")
    return 1


def start_services() -> int:
    if ensure_prerequisites() != 0:
        return 1
    for service in SERVICES:
        if port_is_open(service.port):
            print(f"{service.name} 已在 {service.port} 运行。")
            continue
        spawn_process(service.start_command, service.workdir, service.log_path)
        if wait_port(service.port, 60):
            print(f"{service.name} 已启动，日志：{service.log_path}")
        else:
            print(f"{service.name} 启动超时，请检查日志：{service.log_path}")
            return 1
    return 0


def stop_services() -> int:
    for service in reversed(SERVICES):
        pid = find_listening_pid(service.port)
        if pid is None:
            print(f"{service.name} 当前未监听 {service.port}。")
            continue
        task_name = task_name_for_pid(pid).lower()
        if task_name != "java.exe":
            print(f"{service.name} 的端口 {service.port} 当前由非 Java 进程占用，跳过停止：PID {pid} ({task_name})")
            continue
        stop_pid(pid)
        print(f"{service.name} 已停止：PID {pid}")
    return 0


def status() -> int:
    print("本地链路状态：")
    print(f"- Redis 6379: {'UP' if port_is_open(6379) else 'DOWN'}")
    print(f"- Nacos 8848: {'UP' if port_is_open(8848) else 'DOWN'}")
    for service in SERVICES:
        port_state = "UP" if port_is_open(service.port) else "DOWN"
        health_state = "UP" if http_ok(service.health_url) else "DOWN"
        print(f"- {service.name} {service.port}: 端口 {port_state} / 健康检查 {health_state}")
    return 0


def verify() -> int:
    return verify_local_migration.main()


def print_help() -> int:
    print("用法：python .tools\\local_stack.py <命令>")
    print("可用命令：")
    print("- start-nacos     启动本地 Nacos 2.4.3")
    print("- stop-nacos      停止本地 Nacos 2.4.3")
    print("- start           启动四个核心服务")
    print("- stop            停止四个核心服务")
    print("- status          查看 Redis / Nacos / 四个核心服务状态")
    print("- verify          执行本地迁移链路验收")
    print("- up              先启动 Nacos，再启动四个核心服务")
    print("- down            先停止四个核心服务，再停止 Nacos")
    return 0


def main(argv: list[str]) -> int:
    if len(argv) != 2:
        return print_help()
    command = argv[1].lower()
    if command == "start-nacos":
        return start_nacos()
    if command == "stop-nacos":
        return stop_nacos()
    if command == "start":
        return start_services()
    if command == "stop":
        return stop_services()
    if command == "status":
        return status()
    if command == "verify":
        return verify()
    if command == "up":
        if start_nacos() != 0:
            return 1
        return start_services()
    if command == "down":
        stop_services()
        return stop_nacos()
    return print_help()


if __name__ == "__main__":
    sys.exit(main(sys.argv))
