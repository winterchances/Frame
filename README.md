# 影匣（Frame）

本仓库保留原版网页原型，并在 `native-android` 分支推进 Kotlin 原生 Android 版本。

## 分支说明

- `main`：原始 HTML/CSS/JavaScript 版本，作为视觉与交互对照基准。
- `native-android`：Kotlin + Jetpack Compose 原生 Android 迁移分支。

## 当前迁移进度

- [x] 原版功能审计与迁移清单：`docs/MIGRATION_AUDIT.md`
- [x] 创建独立迁移分支，未修改 `main`
- [x] 建立 Android Gradle / Kotlin / Compose 工程结构
- [x] 原生四个主页面的第一轮 UI 框架
- [x] 接入系统视频文档选择器，并请求持久化 URI 读取权限
- [x] 建立仅针对 `native-android` 分支的 APK 构建工作流
- [ ] Room 数据库与跨重启持久化
- [ ] 视频缩略图生成与自定义封面持久化
- [ ] 完整分类、收藏、重命名、删除、排序和观影记录编辑
- [ ] Media3 播放器与续播/进度保存
- [ ] 倍速、长按倍速、双击快进快退、亮度/音量/进度手势
- [ ] 锁屏防误触、横屏切换、后台播放与常亮
- [ ] 与原版逐项视觉和交互回归测试

> 当前提交是原生工程与 UI 的第一阶段基座，不代表完整播放器已经完成。后续功能按迁移清单逐项实现，不会通过 WebView 或静态页面冒充原生功能。

## 构建

GitHub Actions 工作流：`.github/workflows/android-native.yml`。推送到 `native-android` 分支后会构建 Debug APK，并上传为 `Frame-native-debug` artifact。
