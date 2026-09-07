# 补全 Termux 模块源码

仓库已配置好工程结构。因体积原因，请用下面命令把 Termux 官方源码拷进工程：

```bash
cd /path/to/AndroidTerminal
git clone --depth 1 https://github.com/termux/termux-app.git /tmp/termux-app

# 覆盖/合并模块（保留本仓库已改的 build.gradle）
cp -a /tmp/termux-app/terminal-emulator/src ./terminal-emulator/
cp -a /tmp/termux-app/terminal-view/src ./terminal-view/

# 若 build.gradle 被覆盖，请恢复本仓库版本（见 git status）
```

然后用 Android Studio 打开并 Sync（需安装 NDK）。

本地已完整拷贝的路径（若你从本机 artifacts 同步）：
`AndroidTerminal/terminal-emulator` 与 `AndroidTerminal/terminal-view`。
