#!/data/data/com.termux/files/usr/bin/bash
# 阿拉德勇者 H5 - Termux 一键打包脚本
# 使用方法：在 Termux 中运行 `bash termux_build.sh`

echo "🚀 开始准备 Termux 构建环境..."

# 1. 安装依赖 (如果未安装)
if ! command -v gradle &> /dev/null; then
  echo "📦 正在安装 Gradle 和 OpenJDK 17 (首次需下载，请耐心等待)..."
  pkg update -y
  pkg install -y openjdk-17 gradle zipalign apksigner
fi

# 2. 设置环境变量
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/openjdk-17
export ANDROID_HOME=/data/data/com.termux/files/usr/lib/android-sdk
# 注意：Termux 的 android-sdk 可能需要单独安装或 symlink
# 如果报错，请手动下载 sdk cmdline-tools 并解压到上述路径

# 3. 进入项目目录 (假设脚本在项目根目录运行)
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

echo "📂 项目目录：$PROJECT_DIR"

# 4. 赋予 Gradle 权限并运行
chmod +x ./gradlew
echo "🔨 开始构建 Release APK (首次构建会下载依赖，较慢)..."
./gradlew assembleRelease --no-daemon --stacktrace

# 5. 检查结果
if [ -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
    echo "✅ 构建成功！"
    echo "📍 APK 位置：app/build/outputs/apk/release/app-release-unsigned.apk"
    
    # 6. 自动签名 (使用 debug 签名或自建签名)
    # 这里为了简单，直接使用 debug 签名 (如果有 debug.keystore)
    # 或者使用我们项目里的 release-key.p12 (需要转换格式，较麻烦)
    # 简单起见，我们直接构建 Debug 版吧，功能一样，只是签名不同
    
    echo "⚠️ 上述为未签名包，建议直接构建 Debug 版用于测试："
    echo "   ./gradlew assembleDebug"
else
    echo "❌ 构建失败，请检查上方报错信息。"
    echo "💡 提示：如果是 PaX 错误，尝试在 gradle.properties 中添加 org.gradle.jvmargs=-Xmx2048m"
fi
