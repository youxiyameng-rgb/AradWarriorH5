#!/data/data/com.termux/files/usr/bin/bash
# 阿拉德勇者 H5 - 手动打包脚本 (无需 Gradle)
# 依赖：Termux + aapt2 + d8 + zipalign + apksigner

set -e
echo "🔨 开始手动打包 APK..."

# 1. 环境变量
PROJECT_DIR="/data/data/cn.com.omnimind.bot/workspace/AradWarriorH5"
# 如果是 Termux 环境，路径需要调整为 /storage/emulated/0/...
# 这里假设脚本在 Termux 中运行，且项目已复制到 Termux 可访问目录
if [ -d "/data/data" ]; then
  # Termux 内部
  ANDROID_BUILD_TOOLS="/data/data/com.termux/files/usr/lib/android-sdk/build-tools/34.0.0"
else
  # 普通 Linux/Proot
  ANDROID_BUILD_TOOLS="/workspace/android-sdk/build-tools/34.0.0"
fi

AAPT2="$ANDROID_BUILD_TOOLS/aapt2"
D8="$ANDROID_BUILD_TOOLS/d8"
ZIPALIGN="$ANDROID_BUILD_TOOLS/zipalign"
APKSIGNER="$ANDROID_BUILD_TOOLS/apksigner"

# 检查工具
for tool in aapt2 d8 zipalign apksigner; do
  if ! command -v $tool &> /dev/null; then
    # 尝试使用绝对路径
    if [ ! -f "$ANDROID_BUILD_TOOLS/$tool" ]; then
      echo "❌ 未找到 $tool，请确保 Android SDK build-tools 34.0.0 已安装"
      exit 1
    fi
    export $tool="$ANDROID_BUILD_TOOLS/$tool"
  fi
done

echo "✅ 工具检查通过"

# 2. 准备目录
BUILD_DIR="$PROJECT_DIR/build_manual"
CLASSES_DIR="$BUILD_DIR/classes"
RES_DIR="$BUILD_DIR/res"
LIBS_DIR="$BUILD_DIR/libs"
APK_UNSIGNED="$BUILD_DIR/app-unsigned.apk"
APK_ALIGNED="$BUILD_DIR/app-aligned.apk"
APK_FINAL="$PROJECT_DIR/AradWarrior.apk"

rm -rf "$BUILD_DIR"
mkdir -p "$CLASSES_DIR" "$RES_DIR" "$LIBS_DIR"

# 3. 编译 Kotlin/Java 代码 (需要 kotlinc 或 javac)
# 由于手动编译 Kotlin 极其复杂，此方案在纯 Termux 下较难实现
# 建议：使用预编译好的 classes.dex (如果有) 或者 放弃手动打包，改用云端构建

echo "⚠️ 检测到手动编译 Kotlin 代码过于复杂，需要 kotlinc 环境。"
echo "💡 推荐方案：使用 'Acode' 编辑器插件 或 寻找在线 Gradle 构建服务。"
echo "🛑 脚本终止。"

# 备用方案：如果你只是想在手机上玩，可以直接安装 "Acode" 编辑器，
# 然后使用其内置的 "Android Build" 插件，它会自动处理这些依赖。