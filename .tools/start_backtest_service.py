import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
LOG_PATH = ROOT / ".tools" / "backtest-service-nacos.log"
JAR_PATH = ROOT / "trend-trading-backtest-service" / "target" / "trend-trading-backtest-service-1.0-SNAPSHOT.jar"


def main() -> None:
    log = LOG_PATH.open("ab")
    subprocess.Popen(
        ["java", "-jar", str(JAR_PATH), "--spring.profiles.active=nacos"],
        cwd=ROOT,
        stdout=log,
        stderr=subprocess.STDOUT,
        creationflags=0x00000008 | 0x00000200,
    )


if __name__ == "__main__":
    main()
