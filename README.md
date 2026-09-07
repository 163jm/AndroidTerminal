# Android Terminal

一个简单的 Android 终端模拟器 APK，风格参考 MT 管理器终端。

## 功能

- 本地 shell 命令执行
- 内置常用命令：`ls`、`cd`、`pwd`、`cat`、`echo`、`help` 等
- 支持通过系统 `sh` 执行更多命令
- 绿色终端风格界面
- 命令历史记录

## 编译方法

### 方法一：Android Studio

1. 用 Android Studio 打开本项目
2. 等待 Gradle 同步完成
3. 点击 Run 或 Build → Build Bundle(s) / APK(s) → Build APK(s)

### 方法二：命令行

```bash
chmod +x gradlew
./gradlew assembleDebug
```

生成的 APK 位于：`app/build/outputs/apk/debug/app-debug.apk`

### 方法三：GitHub Actions（推荐）

推送代码后可在仓库的 Actions 页面自动编译，或手动触发。

## 注意事项

1. **这不是 MT 管理器官方终端扩展包**（`bin.mt.termex`）。官方扩展包是闭源的，且专门适配 MT 管理器内部终端环境。
2. 本项目是**独立终端 APK**，可直接安装使用。
3. 无 root 权限时，很多系统命令（如修改系统文件）会失败，这是 Android 安全限制。
4. 如果需要完整的 Linux 环境 + 大量工具（类似 MT 终端扩展包的效果），强烈推荐使用：
   - [Termux](https://termux.dev/)（最强大）
   - [ZeroAicy/MTermEx](https://github.com/ZeroAicy/MTermEx)（非官方 MT 终端扩展，基于 Termux 环境）

## 与 MT 终端扩展包的对比

| 特性 | 本项目 | MT 官方终端扩展包 |
|------|--------|------------------|
| 类型 | 独立 APK | MT 管理器扩展包 |
| 命令数量 | 基础 + 系统 shell | bash + coreutils + ssh/curl 等大量工具 |
| 安装方式 | 普通安装 | 需在 MT 内安装扩展 |
| 开源 | 是 | 否 |
| 适用场景 | 学习 / 简单终端 | 重度玩机 / 逆向 |

## License

MIT
