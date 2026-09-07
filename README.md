# Android Terminal

持续会话式 Android 终端模拟器（接近常规终端体验）。

## 与上一版的区别

| 点 | 旧版 | 新版 |
|----|------|------|
| 会话 | 每次命令新建进程 | **常驻 shell 进程**，连续会话 |
| 输出 | 等命令结束后一次显示 | **实时流式输出** |
| 输入 | 点按钮执行 | 回车直接发送到 shell stdin |
| 中断 | 无 | 支持发送 Ctrl+C |
| 清屏 | 有 | 有（按钮 / `clear`） |

## 使用方式

1. 打开 App → 自动启动交互式 shell（`/system/bin/sh -i`）
2. 在底部输入命令，按 **回车** 或点「发送」
3. 输出会实时出现在上方绿色终端区
4. 「Ctrl+C」尝试中断当前命令
5. 「清屏」或输入 `clear` 清空屏幕
6. 输入 `exit` 会结束当前会话（会自动重启）

## 限制（重要）

当前实现 **没有真实 PTY（伪终端）**：

- `vi` / `nano` / `top` / `htop` 等全屏交互程序体验差或不可用
- 需要密码输入的 `ssh` / `su` 交互可能异常
- 颜色 / 光标定位等 ANSI 序列被简单过滤

若需要完整终端体验（PTY + VT100 + 包管理），请使用：

- [Termux](https://termux.dev/)
- [ZeroAicy/MTermEx](https://github.com/ZeroAicy/MTermEx)（MT 风格扩展）

## 编译

用 **Android Studio** 打开本仓库 → Sync → Run / Build APK。

命令行（需先生成 gradle wrapper）：

```bash
./gradlew assembleDebug
```

APK 路径：`app/build/outputs/apk/debug/app-debug.apk`

## License

MIT
