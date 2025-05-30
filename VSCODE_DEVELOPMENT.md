# 🛠️ 使用 VS Code 开发此 Android 项目

虽然 Android Studio 是官方推荐的 IDE，但您也可以使用 VS Code 进行开发。以下是详细的配置和使用指南。

## 📋 环境准备

### 1. 安装必要软件

**Java Development Kit (JDK)**
```bash
# 下载安装 JDK 11 或更高版本
# 设置 JAVA_HOME 环境变量
```

**Android SDK**
```bash
# 下载 Android SDK Command Line Tools
# 解压到合适目录，如 C:\Android\Sdk
# 设置 ANDROID_HOME 环境变量
# 将 %ANDROID_HOME%\platform-tools 添加到 PATH
```

**Gradle**
```bash
# 项目已包含 Gradle Wrapper，通常不需要单独安装
# 如需要：下载 Gradle 7.4+ 并配置环境变量
```

### 2. VS Code 插件安装

打开 VS Code，安装以下插件：

```json
{
  "必装插件": [
    "Extension Pack for Java",        // Java 开发包
    "Kotlin Language",                // Kotlin 语言支持
    "Gradle for Java",                // Gradle 构建支持
    "Android iOS Emulator",           // 模拟器支持
    "XML Tools"                       // XML 文件支持
  ],
  "推荐插件": [
    "Git Graph",                      // Git 图形化
    "Bracket Pair Colorizer",         // 括号配对
    "Auto Rename Tag",                // 标签重命名
    "Material Icon Theme",            // 图标主题
    "Thunder Client"                  // API 测试（如需要）
  ]
}
```

## 🔧 项目配置

### 1. VS Code 工作区配置

打开项目后，VS Code 会自动创建 `.vscode` 文件夹，包含：

- **extensions.json**: 推荐插件列表
- **settings.json**: 项目特定设置
- **launch.json**: 调试配置
- **tasks.json**: 构建任务

### 2. Java 路径配置

按 `Ctrl+Shift+P` 打开命令面板，运行：
```
Java: Configure Java Runtime
```

确保指向正确的 JDK 路径。

### 3. Android SDK 配置

在用户设置中添加：
```json
{
  "android.sdk.path": "C:/Android/Sdk",
  "android.gradle.path": "./gradlew"
}
```

## 🚀 开发工作流

### 1. 打开项目
```bash
# 命令行打开
code /path/to/Weigh-Tracker-Android

# 或在 VS Code 中 File > Open Folder
```

### 2. 构建项目
```bash
# 在终端中运行（Ctrl+`）
./gradlew assembleDebug

# 或使用 VS Code 命令面板
Ctrl+Shift+P > "Gradle: Run a Gradle task"
```

### 3. 安装到设备
```bash
# 确保设备已连接并开启 USB 调试
./gradlew installDebug

# 卸载应用
./gradlew uninstallDebug
```

### 4. 查看日志
```bash
# 实时查看应用日志
adb logcat -s "WeightTracker"

# 查看所有日志
adb logcat
```

## 🔍 调试功能

### 1. Kotlin/Java 代码调试
- 在代码行号左侧点击设置断点
- 按 `F5` 开始调试（需要配置 launch.json）
- 使用调试控制台查看变量

### 2. 布局预览
❌ **VS Code 限制**: 没有可视化布局编辑器
✅ **解决方案**: 
- 使用在线 Android Layout Previewer
- 或临时使用 Android Studio 预览布局

### 3. 设备调试
```bash
# 连接设备
adb devices

# 无线调试（Android 11+）
adb pair <IP>:<PORT>
adb connect <IP>:<PORT>
```

## 📱 常用命令

### Gradle 构建命令
```bash
# 清理项目
./gradlew clean

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK  
./gradlew assembleRelease

# 运行测试
./gradlew test

# 检查依赖
./gradlew dependencies
```

### ADB 设备管理
```bash
# 列出设备
adb devices

# 安装 APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 启动应用
adb shell am start -n com.weighttracker/.MainActivity

# 停止应用
adb shell am force-stop com.weighttracker
```

### 模拟器操作
```bash
# 列出可用模拟器
emulator -list-avds

# 启动模拟器
emulator -avd <AVD_NAME>

# 冷启动模拟器
emulator -avd <AVD_NAME> -cold-boot
```

## ⚡ VS Code 快捷键

### 编辑快捷键
- `Ctrl+Space`: 代码补全
- `F12`: 跳转到定义
- `Shift+F12`: 查找引用
- `Ctrl+Shift+F`: 全项目搜索
- `Ctrl+Shift+R`: 重构/重命名

### 调试快捷键
- `F5`: 开始调试
- `F9`: 切换断点
- `F10`: 单步跳过
- `F11`: 单步进入
- `Shift+F5`: 停止调试

### 终端快捷键
- `Ctrl+``: 打开终端
- `Ctrl+Shift+``: 新建终端
- `Ctrl+C`: 中断正在运行的命令

## 🎯 VS Code 优化技巧

### 1. 性能优化
```json
{
  "java.compile.nullAnalysis.mode": "automatic",
  "java.configuration.updateBuildConfiguration": "interactive",
  "files.exclude": {
    "**/build/": true,
    "**/.gradle/": true
  }
}
```

### 2. 代码格式化
安装 Kotlin 和 Java 格式化工具：
```
Ext install mathiasfrohlich.kotlin
```

### 3. Git 集成
VS Code 内置 Git 支持，可以：
- 查看文件变更
- 提交代码
- 切换分支
- 合并冲突

## ⚠️ VS Code 的限制

### 无法使用的功能
1. **布局编辑器**: 无可视化 XML 布局编辑
2. **资源管理**: 不如 Android Studio 直观
3. **APK 分析**: 缺少 APK Analyzer
4. **性能分析**: 无内置性能监控
5. **设备管理**: 设备管理不如 Android Studio 完善

### 推荐的混合方案
- **日常开发**: 使用 VS Code 编写代码
- **布局设计**: 使用 Android Studio 设计界面
- **调试优化**: 使用 Android Studio 的高级调试工具
- **发布构建**: 使用 Android Studio 构建和签名

## 🔧 故障排除

### 常见问题

**1. Gradle 同步失败**
```bash
# 清理 Gradle 缓存
./gradlew clean
rm -rf .gradle/
./gradlew build
```

**2. SDK 路径错误**
```bash
# 检查环境变量
echo $ANDROID_HOME
# 或在 Windows 上
echo %ANDROID_HOME%
```

**3. 设备连接问题**
```bash
# 重启 ADB 服务
adb kill-server
adb start-server
```

**4. 编译错误**
- 检查 JDK 版本是否兼容
- 确认 Android SDK 已正确安装
- 验证项目依赖是否完整

## 📖 学习资源

- [VS Code Java 开发指南](https://code.visualstudio.com/docs/java/java-tutorial)
- [Android 开发者文档](https://developer.android.com/docs)
- [Gradle 用户指南](https://docs.gradle.org/current/userguide/userguide.html)
- [Kotlin 语言参考](https://kotlinlang.org/docs/reference/)

---

✨ **总结**: VS Code 可以进行 Android 开发，但体验不如 Android Studio 完整。建议初学者还是使用 Android Studio，熟练后可以考虑 VS Code + Android Studio 的混合开发模式。 