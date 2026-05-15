# 阿拉德勇者 H5 (带 GM 后台)

## 🎮 项目说明
这是一个基于 Cocos Creator H5 游戏的 Android 壳项目，内置**隐藏 GM 后台**。
- **游戏来源**: 本地 assets/game
- **GM 功能**: 等级/金币/装备/爆率 修改
- **激活方式**: 游戏界面**右上角**快速连击 **5 次**

## 📦 如何打包 APK

由于当前 Alpine 环境限制，无法直接运行 Gradle。请按以下步骤操作：

### 方法 A: 命令行打包 (推荐)
1. 确保安装了 JDK 17+ 和 Android SDK。
2. 设置环境变量 `ANDROID_HOME` 指向你的 SDK 路径。
3. 在项目根目录执行：
   ```bash
   ./gradlew assembleRelease
   ```
4. APK 生成位置：`app/build/outputs/apk/release/app-release-unsigned.apk` (需用签名工具签名)
   或 `app/build/outputs/apk/debug/app-debug.apk` (可直接安装测试)

### 方法 B: Android Studio
1. 打开 Android Studio。
2. 选择 `Open an existing project`，选中本目录。
3. 等待 Gradle 同步完成。
4. 点击顶部菜单 `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`。

## 🔓 GM 后台使用说明

1. **启动游戏**后，等待游戏加载完成。
2. 在手机屏幕的**右上角区域**（约 10% 范围）。
3. **快速连续点击 5 次**（1 秒内完成）。
4. GM 控制台会弹出，包含以下功能：
   - **等级 999**: 直接升满级。
   - **金币 +9w**: 增加 99999 金币。
   - **送史诗**: 背包添加一件史诗武器。
   - **背包满**: 一键填满 50 件神装。
   - **爆率调整**: 下拉选择 1x ~ 1000x 爆率。

## 📁 项目结构
```
AradWarriorH5/
├── app/
│   ├── build.gradle          # App 构建配置
│   ├── certs/                # 签名文件 (doudizhu 签名)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/.../MainActivity.kt  # 含 GM 逻辑
│       ├── assets/game/              # H5 游戏资源 (126MB)
│       └── res/                      # 资源文件
├── build.gradle              # 根构建
├── gradle.properties
├── local.properties
└── gradlew                   # Gradle 包装脚本
```

## ⚠️ 注意事项
- 签名文件 `release-key.p12` 已包含在 `app/certs/` 目录。
- 游戏资源较大 (126MB)，首次构建可能稍慢。
- GM 功能通过 WebView JavaScript 接口实现，仅对本地 H5 有效。
