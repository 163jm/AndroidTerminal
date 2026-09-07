# Android Terminal (PTY)

基于 **Termux `terminal-emulator` + `terminal-view`** 的真实伪终端 APK。

## 体验

- **全屏终端**，没有底部输入框
- 键盘直接写入 **真实 PTY**
- 支持颜色、光标、滚动、双指缩放字体
- 点击屏幕弹出软键盘
- Shell 退出后自动重启会话

## 架构

```
app/                  # 本应用
terminal-emulator/    # Termux PTY + VT 解析（含 JNI）
terminal-view/        # Termux 终端渲染 View
```

当前启动系统 `/system/bin/sh`。后续可接 bootstrap / Extra Keys / 多会话。

## 编译要求

1. Android Studio + **NDK**（含 native 代码）
2. Sync 后编译 `libtermux.so`
3. 若 NDK 版本不符，改 `gradle.properties` 中 `ndkVersion`

## License

- 本应用：MIT
- terminal-emulator / terminal-view：Apache-2.0（termux/termux-app）
