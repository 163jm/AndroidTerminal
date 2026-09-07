# Android Terminal (PTY)

基于 **Termux `terminal-emulator` + `terminal-view`** 的真实伪终端。

## 体验

- 全屏终端，**无底部输入框**
- 键盘直接写入 **真实 PTY**
- VT100/xterm 颜色与光标
- 点击屏幕弹出软键盘，双指缩放字体
- Shell 退出后自动重启

## 编译

### GitHub Actions（推荐）

推送 `main` 后自动构建。Actions 会：

1. 拉取完整 Termux 模块源码
2. 安装 NDK 并编译 `libtermux.so`
3. 产出 `app-debug` Artifact APK

在仓库 **Actions** 页面下载 APK。

### 本地 Android Studio

```bash
git clone https://github.com/163jm/AndroidTerminal.git
cd AndroidTerminal

# 补全 Termux 源码（必做）
git clone --depth 1 https://github.com/termux/termux-app.git /tmp/termux-app
rm -rf terminal-emulator/src terminal-view/src
cp -a /tmp/termux-app/terminal-emulator/src ./terminal-emulator/
cp -a /tmp/termux-app/terminal-view/src ./terminal-view/
```

用 Android Studio 打开 → 安装 NDK → Sync → Run。

详见 [SETUP_MODULES.md](SETUP_MODULES.md)。

## 架构

```
app/                  # 全屏 TerminalView + PTY 会话
terminal-emulator/    # PTY + VT 解析（JNI libtermux）
terminal-view/        # 终端渲染
```

当前 shell：`/system/bin/sh`（后续可接 bootstrap 包环境）。

## License

- 本应用：MIT
- terminal-emulator / terminal-view：Apache-2.0（[termux/termux-app](https://github.com/termux/termux-app)）
