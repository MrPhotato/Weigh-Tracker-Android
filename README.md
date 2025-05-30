# 体重记录器 (Weight Tracker)

一个简洁、实用的安卓体重记录应用，帮助您轻松追踪和管理体重变化。

## 🌟 功能特色

### 📝 体重记录
- 简单直观的体重录入界面
- 每日体重记录，支持小数点精度
- 实时显示今日体重状态
- 智能数据验证，确保输入有效

### 📊 数据管理
- 完整的历史记录查看
- 支持编辑和删除历史数据
- 数据按时间倒序排列，最新记录在前
- 本地数据库存储，数据安全可靠

### 📈 趋势图表
- 精美的折线图展示体重变化趋势
- 多时间范围选择：7天、30天、90天、全部
- 支持图表缩放和拖拽查看
- 清晰的日期标签和数值显示

### ⏰ 智能提醒
- 自定义每日提醒时间
- 开机自动恢复提醒设置
- 友好的通知消息
- 一键开启/关闭提醒功能

## 🏗️ 技术架构

### 架构模式
- **MVVM架构**: 使用ViewModel + LiveData + Repository模式
- **单一Activity架构**: 使用Navigation Component进行页面导航
- **数据绑定**: 使用ViewBinding提高开发效率

### 核心技术栈
- **开发语言**: Kotlin
- **UI框架**: Material Design 3
- **数据库**: Room (SQLite)
- **图表库**: MPAndroidChart
- **导航**: Navigation Component
- **异步处理**: Kotlin Coroutines
- **架构组件**: ViewModel, LiveData

### 项目结构
```
app/src/main/java/com/weighttracker/
├── data/                   # 数据层
│   ├── Weight.kt          # 数据模型
│   ├── WeightDao.kt       # 数据访问对象
│   ├── WeightDatabase.kt  # 数据库配置
│   └── Converters.kt      # 类型转换器
├── repository/            # 仓库层
│   └── WeightRepository.kt
├── ui/                    # 界面层
│   ├── entry/            # 体重录入
│   ├── history/          # 历史记录
│   ├── chart/            # 图表显示
│   └── settings/         # 设置页面
├── notification/         # 通知功能
│   ├── NotificationReceiver.kt
│   ├── NotificationScheduler.kt
│   └── BootReceiver.kt
├── util/                 # 工具类
│   └── PreferenceManager.kt
└── MainActivity.kt       # 主Activity
```

## 🚀 快速开始

### 环境要求
- Android Studio Arctic Fox 或更高版本
- Android SDK 24 (Android 7.0) 或更高版本
- Kotlin 1.9.10 或更高版本

### 构建步骤
1. 克隆项目到本地
```bash
git clone [项目地址]
cd Weigh-Tracker-Android
```

2. 使用Android Studio打开项目

3. 等待Gradle同步完成

4. 连接Android设备或启动模拟器

5. 点击运行按钮构建并安装应用

### 权限说明
应用需要以下权限：
- `POST_NOTIFICATIONS`: 发送提醒通知
- `RECEIVE_BOOT_COMPLETED`: 开机启动恢复提醒
- `SCHEDULE_EXACT_ALARM`: 设置精确的定时提醒

## 📱 使用指南

### 首次使用
1. 打开应用，进入体重录入页面
2. 输入当前体重，点击保存
3. 在设置页面开启每日提醒功能
4. 设置合适的提醒时间

### 日常使用
1. 每天同一时间测量体重
2. 打开应用输入体重数据
3. 查看历史记录了解体重变化
4. 通过图表分析长期趋势

### 数据管理
- 在历史记录页面可以编辑或删除错误数据
- 在图表页面可以选择不同时间范围查看趋势
- 所有数据本地存储，卸载应用后会丢失

## 🎨 界面设计

### 设计原则
- **简洁性**: 界面简洁明了，操作直观
- **一致性**: 遵循Material Design设计规范
- **可用性**: 考虑不同用户群体的使用习惯
- **美观性**: 现代化的界面设计和配色方案

### 主要页面
1. **体重录入**: 大号输入框，清晰的保存按钮
2. **历史记录**: 卡片式布局，支持编辑删除
3. **趋势图表**: 交互式图表，多时间维度
4. **设置页面**: 分组设置项，开关状态清晰

## 🔧 开发最佳实践

### 代码规范
- 遵循Kotlin官方编码规范
- 使用有意义的变量和函数命名
- 添加必要的注释说明
- 保持代码结构清晰

### 性能优化
- 使用ViewBinding避免findViewById
- 使用LiveData观察数据变化
- 数据库操作在后台线程执行
- 图表数据按需加载

### 错误处理
- 用户输入验证和错误提示
- 数据库操作异常处理
- 网络请求异常处理（如有）
- 崩溃日志收集（建议添加）

## 📈 未来扩展

### 计划功能
- [ ] 体重目标设定和进度跟踪
- [ ] BMI计算和健康建议
- [ ] 数据导出和备份功能
- [ ] 更多图表类型和统计信息
- [ ] 社交分享功能
- [ ] 多用户支持

### 技术优化
- [ ] 数据库迁移策略
- [ ] 更完善的错误处理
- [ ] 性能监控和分析
- [ ] 自动化测试
- [ ] CI/CD集成

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情


⭐ 如果这个项目对您有帮助，请给一个星标支持！ 