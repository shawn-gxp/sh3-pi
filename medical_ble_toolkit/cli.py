import sys
import asyncio
from typing import List, Optional

from .ble_client import build_arg_parser, _async_main, log
from .common.hexutil import ms_timestamp

def _has_direct_flags(argv: List[str]) -> bool:
    """True if user passed flags that imply non-interactive connect mode."""
    markers = (
        "--profile",
        "--address",
        "-a",
        "--scan",
        "--list-profiles",
        "--pair",
        "--no-pair",
        "--auto-parse",
        "--duration",
        "-t",
        "connect",
        "omron",
        "nipro",
    )
    return any(a in markers or a.startswith("--profile=") for a in argv)


def main(argv: Optional[List[str]] = None) -> int:
    argv = list(sys.argv[1:] if argv is None else argv)

    # No args (or only -v/-q) → interactive wizard (default brand: Omron)
    if not argv or argv in (["-v"], ["--verbose"], ["-q"], ["--quiet"]):
        from .interactive import main as interactive_main
        return interactive_main()

    if argv and argv[0] in ("interactive", "i", "menu", "wizard"):
        from .interactive import main as interactive_main
        return interactive_main()

    parser = build_arg_parser()
    args = parser.parse_args(argv)

    if getattr(args, "command", None) in ("interactive", "i", "menu", "wizard"):
        from .interactive import main as interactive_main
        return interactive_main()

    # If user ran without subcommand, treat as "connect" when flags present
    if args.command is None:
        if _has_direct_flags(argv):
            args.command = "connect"
        else:
            from .interactive import main as interactive_main
            return interactive_main()

    if args.quiet:
        args.verbose = False
    try:
        return asyncio.run(_async_main(args))
    except KeyboardInterrupt:
        log.warning("Interrupted by user  ts=%s", ms_timestamp())
        return 130
