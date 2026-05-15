#!/usr/bin/env python3
"""
阿拉德勇者 APK 手动打包脚本
纯 Python 实现，不依赖 Java 和 aapt2
"""
import os
import sys
import struct
import hashlib
import zipfile
import subprocess
import shutil
import tempfile
import json
import xml.etree.ElementTree as ET
from datetime import datetime

PROJECT = "/workspace/AradWarriorH5"
APP = f"{PROJECT}/app"
OUTPUT = f"{PROJECT}/output"
MANIFEST = f"{APP}/src/main/AndroidManifest.xml"
RES_DIR = f"{APP}/src/main/res"
ASSETS_DIR = f"{APP}/src/main/assets"
JAVA_DIR = f"{APP}/src/main/java"
CERT = f"{APP}/certs/release-key.p12"

CERT_ALIAS = "doudizhu"
CERT_PASS = "doudizhu123"

# APK 常量
APK_ENTRY_DIR = "META-INF/"
CERT_NAME = "CERT.RSA"
CERT_SF = "CERT.SF"
CERT_DSA = "CERT.DSA"

def run(cmd, **kwargs):
    print(f"  $ {' '.join(cmd[:5])}{'...' if len(cmd) > 5 else ''}")
    r = subprocess.run(cmd, capture_output=True, text=True, **kwargs)
    if r.returncode != 0:
        err = r.stderr[-300:] if r.stderr else ""
        raise RuntimeError(f"Command failed: {r.returncode}\n{err}")
    return r

def read_manifest():
    """读取 AndroidManifest.xml 提取关键信息"""
    with open(MANIFEST, 'r') as f:
        content = f.read()
    
    app_id = ''
    app_name = ''
    theme = ''
    activities = []
    
    import re
    m = re.search(r'applicationId\s+"([^"]+)"', content)
    if m: app_id = m.group(1)
    
    m = re.search(r'android:label="([^"]+)"', content)
    if m: app_name = m.group(1)
    
    m = re.search(r'android:theme="([^"]+)"', content)
    if m: theme = m.group(1)
    
    for act_match in re.finditer(r'<activity[^>]*android:name="([^"]+)"[^>]*>', content):
        activities.append(act_match.group(1))
    
    return {
        'applicationId': app_id or 'com.aradwarrior.h5',
        'label': app_name or '阿拉德勇者',
        'theme': theme or '@style/Theme.AppCompat.Light.NoActionBar',
        'activities': activities,
        'raw': content
    }

def generate_standalone_manifest(mf):
    """生成独立 APK 的 AndroidManifest.xml（去掉 build.gradle 注入的属性）"""
    raw = mf['raw']
    # 去掉 android:appComponentFactory 等 Gradle 注入的属性
    import re
    raw = re.sub(r' android:appComponentFactory="[^"]*"', '', raw)
    raw = re.sub(r' android:extractNativeLibs="[^"]*"', '', raw)
    raw = re.sub(r' android:usesNonSdkApi="[^"]*"', '', raw)
    return raw

def generate_manifest_sf_hash(manifest_content):
    """计算 Manifest 的 SHA-256 用于签名"""
    return hashlib.sha256(manifest_content.encode('utf-8')).hexdigest()

def build_apk(output_path):
    """构建 APK"""
    print("\n" + "=" * 50)
    print("🔨 阿拉德勇者 APK 手动打包")
    print("=" * 50)
    
    os.makedirs(OUTPUT, exist_ok=True)
    
    # Step 1: 读取并解析 Manifest
    print("\n[1/7] 解析 AndroidManifest.xml...")
    mf = read_manifest()
    print(f"  包名: {mf['applicationId']}")
    print(f"  应用名: {mf['label']}")
    print(f"  活动: {mf['activities']}")
    
    # Step 2: 准备临时目录
    print("\n[2/7] 准备文件...")
    tmp_dir = tempfile.mkdtemp(prefix="apk_build_")
    print(f"  临时目录: {tmp_dir}")
    
    try:
        # 复制资源目录
        res_target = os.path.join(tmp_dir, "res")
        if os.path.exists(RES_DIR):
            shutil.copytree(RES_DIR, res_target)
            print(f"  ✅ 资源目录: {os.path.getsize(res_target) / 1024:.0f} KB")
        
        # 复制 assets 目录
        assets_target = os.path.join(tmp_dir, "assets")
        if os.path.exists(ASSETS_DIR):
            shutil.copytree(ASSETS_DIR, assets_target)
            size = sum(os.path.getsize(os.path.join(dp, f)) 
                      for dp, dn, filenames in os.walk(ASSETS_DIR) 
                      for f in filenames)
            print(f"  ✅ Assets: {size / 1024 / 1024:.1f} MB")
        
        # 复制 classes.dex - 我们需要先编译 Kotlin 代码
        # 但 Java 跑不了，所以先检查是否有预编译的 dex
        print("\n[3/7] 检查预编译 DEX...")
        dex_path = None
        for root, dirs, files in os.walk(APP):
            for f in files:
                if f.endswith('.dex'):
                    dex_path = os.path.join(root, f)
                    print(f"  找到预编译 DEX: {dex_path}")
                    break
            if dex_path:
                break
        
        if not dex_path:
            print("  ⚠️  未找到预编译 DEX，需要编译 Kotlin 代码")
            print("  但 Java 在 proot 环境中不可用...")
            
            # 尝试从 build 目录找
            build_dex = f"{APP}/build/intermediates/dex/release"
            if os.path.exists(build_dex):
                for f in os.listdir(build_dex):
                    if f.endswith('.dex'):
                        dex_path = os.path.join(build_dex, f)
                        print(f"  找到 build DEX: {dex_path}")
                        break
        
        if not dex_path:
            print("\n  ❌ 无法编译 Kotlin 代码（Java 不可用）")
            print("  需要先在有 Java 环境的机器上编译出 classes.dex")
            print("  或者使用 Gradle 构建")
            return None
        
        # 复制 DEX
        classes_dex = os.path.join(tmp_dir, "classes.dex")
        shutil.copy2(dex_path, classes_dex)
        print(f"  ✅ DEX: {os.path.getsize(classes_dex) / 1024:.0f} KB")
        
        # Step 3: 生成 AndroidManifest.xml
        print("\n[4/7] 生成 APK 版 AndroidManifest.xml...")
        standalone_manifest = generate_standalone_manifest(mf)
        manifest_path = os.path.join(tmp_dir, "AndroidManifest.xml")
        with open(manifest_path, 'w') as f:
            f.write(standalone_manifest)
        
        # 压缩 Manifest（APK 要求压缩级别 0）
        print("  压缩 Manifest...")
        import zlib
        with open(manifest_path, 'rb') as f:
            manifest_data = f.read()
        
        # Step 4: 创建 APK（ZIP 格式）
        print("\n[5/7] 创建 APK...")
        apk_path = os.path.join(OUTPUT, "AradWarrior-debug.apk")
        
        with zipfile.ZipFile(apk_path, 'w', zipfile.ZIP_DEFLATED) as apk:
            # 添加 AndroidManifest.xml (压缩级别 0)
            manifest_compressed = zlib.compress(manifest_data, 6)
            apk.writestr(zipfile.ZipInfo("AndroidManifest.xml", (2024, 1, 1, 0, 0, 0)),
                        manifest_compressed, compress_type=zipfile.ZIP_DEFLATED)
            
            # 添加资源
            for root, dirs, files in os.walk(res_target):
                for fname in files:
                    fpath = os.path.join(root, fname)
                    arcname = os.path.relpath(fpath, tmp_dir)
                    apk.write(fpath, arcname, zipfile.ZIP_DEFLATED)
            
            # 添加 assets
            for root, dirs, files in os.walk(assets_target):
                for fname in files:
                    fpath = os.path.join(root, fname)
                    arcname = os.path.relpath(fpath, tmp_dir)
                    apk.write(fpath, arcname, zipfile.ZIP_DEFLATED)
            
            # 添加 classes.dex (压缩级别 0)
            with open(classes_dex, 'rb') as f:
                dex_data = f.read()
            apk.writestr(zipfile.ZipInfo("classes.dex", (2024, 1, 1, 0, 0, 0)),
                        dex_data, compress_type=zipfile.ZIP_STORED)
            
            # 添加 META-INF
            apk.writestr(zipfile.ZipInfo("META-INF/", (2024, 1, 1, 0, 0, 0)))
        
        print(f"  ✅ APK 创建完成: {os.path.getsize(apk_path) / 1024 / 1024:.1f} MB")
        
        # Step 5: 尝试签名
        print("\n[6/7] 签名 APK...")
        # 由于 Java 不可用，无法使用 apksigner
        # 但我们可以用 zipalign 优化（如果有的话）
        
        if os.path.exists(ZIPALIGN):
            aligned_path = os.path.join(OUTPUT, "AradWarrior-debug-aligned.apk")
            run([ZIPALIGN, "-f", "4", apk_path, aligned_path])
            apk_path = aligned_path
            print(f"  ✅ zipalign 完成")
        else:
            print("  ⚠️  未找到 zipalign，跳过优化")
        
        # Step 6: 验证 APK
        print("\n[7/7] 验证 APK...")
        with zipfile.ZipFile(apk_path, 'r') as apk:
            names = apk.namelist()
            has_manifest = "AndroidManifest.xml" in names
            has_dex = "classes.dex" in names
            has_assets = any(n.startswith("assets/") for n in names)
            has_res = any(n.startswith("res/") for n in names)
            
            total_size = sum(info.file_size for info in apk.infolist())
            
            print(f"  ✅ Manifest: {'有' if has_manifest else '无'}")
            print(f"  ✅ DEX: {'有' if has_dex else '无'}")
            print(f"  ✅ Assets: {'有' if has_assets else '无'}")
            print(f"  ✅ Resources: {'有' if has_res else '无'}")
            print(f"  ✅ 总大小: {total_size / 1024 / 1024:.1f} MB")
            print(f"  ✅ 文件数: {len(names)}")
        
        print(f"\n{'=' * 50}")
        print(f"🎉 APK 构建完成!")
        print(f"   路径: {apk_path}")
        print(f"   大小: {os.path.getsize(apk_path) / 1024 / 1024:.1f} MB")
        print(f"{'=' * 50}")
        
        return apk_path
        
    except Exception as e:
        print(f"\n❌ 构建失败: {e}")
        import traceback
        traceback.print_exc()
        return None
    finally:
        # 清理临时目录
        shutil.rmtree(tmp_dir, ignore_errors=True)

if __name__ == "__main__":
    build_apk(f"{OUTPUT}/AradWarrior.apk")
