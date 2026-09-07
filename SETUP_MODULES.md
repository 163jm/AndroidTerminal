# 补全 Termux 模块（本地编译必做）

GitHub Actions 会在构建时自动 `git clone` Termux 源码。

本地用 Android Studio 编译时，请先执行：

```bash
git clone --depth 1 https://github.com/termux/termux-app.git /tmp/termux-app
rm -rf terminal-emulator/src terminal-view/src
cp -a /tmp/termux-app/terminal-emulator/src ./terminal-emulator/
cp -a /tmp/termux-app/terminal-view/src ./terminal-view/
```

保留本仓库的 `terminal-emulator/build.gradle` 与 `terminal-view/build.gradle`。

需要已安装 **NDK**（见 `gradle.properties` 中 `ndkVersion`）。
