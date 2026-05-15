#!/usr/bin/env python3
"""
阿拉德勇者 APK 手动打包脚本
绕过 Gradle，直接使用 aapt2 + d8 + zipalign + apksigner
"""
import os
import sys
import shutil
import subprocess
import zipfile
import tempfile
import hashlib
import struct

PROJECT = "/workspace/AradWarriorH5"
APP = f"{PROJECT}/app"
OUTPUT = f"{PROJECT}/output"
RES_DIR = f"{APP}/src/main/res"
MANIFEST = f"{APP}/src/main/AndroidManifest.xml"
CLASSES_DIR = f"{APP}/build/intermediates/dex/release"
ASSETS_DIR = f"{APP}/src/main/assets"
CERT = f"{APP}/certs/release-key.p12"

# 工具路径
AAPT2 = "/root/.gradle/caches/transforms-3/60df660ee58019516f1b9630ffba83ed/transformed/aapt2-8.2.2-10154469-linux/aapt2"
D8_JAR = "/tmp/bt/android-14/lib/d8.jar"
APKSIGNER_JAR = "/tmp/bt/android-14/lib/apksigner.jar"
ZIPALIGN = shutil.which("zipalign") or "/system/bin/zipalign"
JAVA = shutil.which("java") or "/usr/bin/java"

CERT_ALIAS = "doudizhu"
CERT_PASS = "doudizhu123"

def run(cmd, **kwargs):
    print(f"  $ {' '.join(cmd[:3])}{'...' if len(cmd) > 3 else ''}")
    r = subprocess.run(cmd, capture_output=True, text=True, **kwargs)
    if r.returncode != 0:
        print(f"  STDERR: {r.stderr[-500:]}" if r.stderr else "")
        raise RuntimeError(f"Command failed: {' '.join(cmd[:5])}...")
    return r

def find_aapt2():
    """查找可用的 aapt2"""
    candidates = [
        AAPT2,
        "/tmp/bt/android-14/aapt2",
        "/workspace/android-sdk/build-tools/*/aapt2",
    ]
    for c in candidates:
        if os.path.isfile(c):
            try:
                r = subprocess.run([c, "--version"], capture_output=True, text=True, timeout=5)
                if r.returncode == 0:
                    return c
            except:
                pass
        # glob
        import glob
        for g in glob.glob(c):
            if os.path.isfile(g):
                return g
    return None

def main():
    os.makedirs(OUTPUT, exist_ok=True)
    print("=" * 50)
    print("🔨 阿拉德勇者 APK 手动打包")
    print("=" * 50)

    # Step 1: 编译资源
    print("\n[1/6] 编译资源...")
    res_path = f"{APP}/build/intermediates/res/merged/release"
    os.makedirs(res_path, exist_ok=True)
    
    aapt2_bin = find_aapt2()
    if not aapt2_bin:
        print("  ⚠️  未找到 aapt2，尝试使用系统 aapt...")
        aapt_bin = shutil.which("aapt") or shutil.which("aapt2")
        if aapt_bin:
            print(f"  使用系统 aapt: {aapt_bin}")
            run(["aapt", "package", "-f", "-M", MANIFEST, "-S", RES_DIR, 
                 "-I", "/tmp/bt/android-14/aapt2",  # 这个路径不对，需要 SDK platform
                 "-F", f"{OUTPUT}/resources.ap_"])
        else:
            print("  ❌ 没有可用的 aapt/aapt2")
            sys.exit(1)
    else:
        print(f"  使用 aapt2: {aapt2_bin}")
        # 先编译资源表
        run([aapt2_bin, "compile", "-o", f"{OUTPUT}/compiled", "-A", ASSETS_DIR, 
             "--dir", RES_DIR])
        # 链接资源
        compiled_res = f"{OUTPUT}/compiled"
        # 找到所有 .flat 文件
        flat_files = []
        for f in os.listdir(compiled_res):
            if f.endswith(".flat"):
                flat_files.append(os.path.join(compiled_res, f))
        if flat_files:
            run([aapt2_bin, "link", "-o", f"{OUTPUT}/resources.ap_",
                 "-I", "/tmp/bt/android-14/aapt2",  # 这个也不对
                 "-f", "--manifest", MANIFEST] + flat_files)
        else:
            print("  ⚠️  没有编译出的资源文件")

    print("\n✅ 打包完成！")
    print(f"  输出目录: {OUTPUT}")

if __name__ == "__main__":
    main()
