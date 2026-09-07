package com.example.androidterminal

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.androidterminal.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val commandHistory = mutableListOf<String>()
    private var historyIndex = -1
    private var currentWorkingDir = "/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentWorkingDir = filesDir.absolutePath

        binding.btnRun.setOnClickListener { executeCommand() }
        binding.btnClear.setOnClickListener {
            binding.tvOutput.text = getString(R.string.welcome)
        }

        binding.etCommand.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                executeCommand()
                true
            } else {
                false
            }
        }

        appendOutput("工作目录: $currentWorkingDir\n")
        appendOutput("提示: 输入 help 查看内置命令\n\n")
    }

    private fun executeCommand() {
        val cmd = binding.etCommand.text.toString().trim()
        if (cmd.isEmpty()) return

        commandHistory.add(cmd)
        historyIndex = commandHistory.size

        appendOutput("\n$ $cmd\n")
        binding.etCommand.setText("")

        lifecycleScope.launch {
            val result = runShellCommand(cmd)
            appendOutput(result)
            scrollToBottom()
        }
    }

    private suspend fun runShellCommand(cmd: String): String = withContext(Dispatchers.IO) {
        try {
            // 内置命令处理
            when {
                cmd == "help" -> return@withContext """
                    |内置命令:
                    |  help          - 显示此帮助
                    |  clear         - 清空屏幕
                    |  pwd           - 当前目录
                    |  cd <dir>      - 切换目录
                    |  ls [path]     - 列出文件
                    |  cat <file>    - 查看文件内容
                    |  echo <text>   - 输出文本
                    |  whoami        - 当前用户
                    |  uname         - 系统信息
                    |  id            - 用户ID信息
                    |  df            - 磁盘空间
                    |  free          - 内存信息
                    |  ps            - 进程列表
                    |  date          - 当前时间
                    |  env           - 环境变量
                    |
                    |其他命令会尝试通过系统 shell 执行。
                    |注意: 无 root 权限时很多系统命令会失败。
                    |推荐使用 Termux 获取完整 Linux 环境。
                """.trimMargin()

                cmd == "clear" -> {
                    withContext(Dispatchers.Main) {
                        binding.tvOutput.text = getString(R.string.welcome)
                    }
                    return@withContext ""
                }

                cmd == "pwd" -> return@withContext currentWorkingDir + "\n"

                cmd.startsWith("cd ") -> {
                    val target = cmd.removePrefix("cd ").trim()
                    val newDir = if (target.startsWith("/")) {
                        File(target)
                    } else {
                        File(currentWorkingDir, target)
                    }
                    return@withContext if (newDir.exists() && newDir.isDirectory) {
                        currentWorkingDir = newDir.canonicalPath
                        "切换到: $currentWorkingDir\n"
                    } else {
                        "目录不存在: $target\n"
                    }
                }

                cmd == "ls" || cmd.startsWith("ls ") -> {
                    val path = if (cmd == "ls") currentWorkingDir else {
                        val p = cmd.removePrefix("ls ").trim()
                        if (p.startsWith("/")) p else File(currentWorkingDir, p).absolutePath
                    }
                    val dir = File(path)
                    if (!dir.exists()) return@withContext "路径不存在: $path\n"
                    if (!dir.isDirectory) return@withContext "不是目录: $path\n"
                    val files = dir.listFiles()?.sortedBy { it.name } ?: emptyList()
                    return@withContext files.joinToString("\n") {
                        val type = if (it.isDirectory) "d" else "-"
                        val size = if (it.isFile) it.length().toString() else ""
                        "$type  ${it.name.padEnd(30)} $size"
                    } + "\n"
                }

                cmd.startsWith("cat ") -> {
                    val filePath = cmd.removePrefix("cat ").trim()
                    val file = if (filePath.startsWith("/")) File(filePath) else File(currentWorkingDir, filePath)
                    return@withContext if (file.exists() && file.isFile) {
                        try {
                            file.readText() + "\n"
                        } catch (e: Exception) {
                            "读取失败: ${e.message}\n"
                        }
                    } else {
                        "文件不存在: $filePath\n"
                    }
                }

                cmd.startsWith("echo ") -> return@withContext cmd.removePrefix("echo ") + "\n"

                cmd == "whoami" -> return@withContext "u0_a${android.os.Process.myUid() % 10000}\n"
                cmd == "id" -> return@withContext "uid=${android.os.Process.myUid()} gid=${android.os.Process.myUid()}\n"
                cmd == "date" -> return@withContext java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()) + "\n"
                cmd == "uname" || cmd == "uname -a" -> return@withContext "Android ${android.os.Build.VERSION.RELEASE} ${android.os.Build.HARDWARE} ${android.os.Build.SUPPORTED_ABIS.joinToString()}\n"
            }

            // 尝试执行系统命令
            val processBuilder = ProcessBuilder("sh", "-c", cmd)
            processBuilder.directory(File(currentWorkingDir))
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            if (output.isEmpty()) {
                "命令执行完成 (无输出)\n"
            } else {
                output.toString()
            }
        } catch (e: Exception) {
            "错误: ${e.message}\n"
        }
    }

    private fun appendOutput(text: String) {
        binding.tvOutput.append(text)
    }

    private fun scrollToBottom() {
        binding.scrollView.post {
            binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}
