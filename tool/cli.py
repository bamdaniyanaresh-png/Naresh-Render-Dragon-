import argparse
import platform
import sys

from .builder import build, check, check_environment, clean
from .config import PROFILES
from .interface import print_interface
from .materials import available_base_materials, material_names


def main(argv=None):
    parser = argparse.ArgumentParser(
        prog="build",
        description="Independent RenderDragon material builder",
    )
    sub = parser.add_subparsers(dest="command", required=True)

    sub.add_parser("setup", help="verify the external BGFX shaderc compiler")

    mats = sub.add_parser("mats", help="compile material.bin files")
    default_profile = "windows" if platform.system() == "Windows" else "android"
    mats.add_argument("-p", "--profile", choices=sorted(PROFILES), default=default_profile)
    mats.add_argument("-m", "--materials", nargs="+", default=None)
    mats.add_argument(
        "-D",
        "--define",
        dest="defines",
        action="append",
        default=[],
        help="compiler macro definition; repeat to pass multiple definitions",
    )

    sub.add_parser("list", help="list source materials")
    interface = sub.add_parser(
        "interface",
        help="show shader attributes, varyings, and mapped engine uniforms",
    )
    interface.add_argument(
        "material",
        nargs="?",
        default="RenderChunk",
        help="material name (default: RenderChunk)",
    )
    sub.add_parser("check", help="validate project and build environment")
    sub.add_parser("clean", help="remove generated build output")

    args = parser.parse_args(argv)
    try:
        if args.command == "setup":
            from .setup import setup_environment
            setup_environment()
        elif args.command == "mats":
            build(args.profile, args.materials or material_names(), args.defines)
        elif args.command == "list":
            print("Source materials (build eligibility shown):")
            base_materials = available_base_materials()
            for name in material_names():
                status = "ready" if name in base_materials else "missing base material"
                print(f"  {name:<24} [{status}]")
        elif args.command == "interface":
            print_interface(args.material)
        elif args.command == "check":
            check_environment()
            errors = check()
            if errors:
                print("Project check failed:")
                for error in errors:
                    print("  - " + error)
                return 2
            print("Project check passed.")
        elif args.command == "clean":
            clean()
        return 0
    except KeyboardInterrupt:
        print("\nCancelled.", file=sys.stderr)
        return 130
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
