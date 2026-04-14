# MinimalTimer

一款基于 Jetpack Compose 开发的 Android 极简翻页计时器应用。

`MinimalTimer` 模拟了经典的 **Fliqlo** 翻页时钟视觉风格，专为作为桌面摆件、专注计时或摄影背景而设计。它通过极其简洁的交互逻辑和沉浸式的视觉体验，将您的安卓设备变成一个优雅的数字时钟。

## ✨ 项目特性

- **经典翻页视觉**：高度仿真的 Fliqlo 样式翻页动画，数字切换带有 3D 空间感。
- **物理常亮支持**：默认开启物理常亮（Keep Screen On），非常适合作为充电时的电子摆件。
- **全沉浸体验**：自动隐藏系统状态栏与导航栏（边缘到边缘设计），无任何 UI 干扰。
- **双重触感反馈**：
    - 集成系统级触感与自定义振动器。
    - **单击**：开始/暂停计时，伴随短促的物理震感。
    - **长按**：归零重置计时，伴随长振动确认。
- **响应式布局**：
    - **横屏模式**：大字体水平排列，适合平板或横放的手机。
    - **竖屏模式**：垂直卡片堆叠，充分利用屏幕高度。
- **高性能 UI**：完全采用 Compose 原生绘制与动画 API，运行流畅且省电。

## 🛠️ 技术栈

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Animation**: `androidx.compose.animation.core` (Animatable, Tween)
- **Architecture**: Single Activity with ComponentActivity
- **System**: WindowInsetsController (隐藏系统栏), VibrationManager (多版本兼容震动)

## 🚀 快速上手

### 环境要求
- Android Studio Ladybug 或更高版本。
- JDK 17。
- 设备或模拟器运行 Android 8.0 (API 26) 及以上系统。

### 安装步骤
1. 克隆本项目：
   ```bash
   git clone [https://github.com/](https://github.com/)[your-username]/MinimalTimer.git
   
1. **在 Android Studio 中打开项目。**
2. **等待 Gradle 同步完成后，点击 Run 部署到您的设备。**

## 🎮 使用方法

| 动作 | 功能 | 反馈 |
| :--- | :--- | :--- |
| **轻触屏幕** | 开始 / 暂停计时 | 快速点击震感 |
| **长按屏幕** | 计时归零 | 强力长震感 |
| **旋转设备** | 自动切换横竖屏布局 | - |
| **边缘滑动** | 唤出系统导航栏 | - |

## 🏗️ 核心逻辑说明

* **翻页核心 (FliqloCard)**：利用 `graphicsLayer` 旋转和 `clipRect` 剪裁技术，将数字分为上下两层进行 3D 翻转模拟。
* **计时机制**：使用 `LaunchedEffect` 与 `delay` 构成的协程循环进行精准计时。
* **震动执行器 (performStrongVibration)**：封装了 `VibratorManager`，确保在 Android 12 (S) 及旧版本上都有高振幅的触感体验。

## 📜 许可证

本项目基于 **MIT License** 协议。

---

*由 Jetpack Compose 驱动，追求极致的简约。*
