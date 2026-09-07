package com.example.androidterminal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.example.androidterminal.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 持续会话式终端：保持一个常驻 shell 进程，实时流式输出，
 * 输入回车后直接写入进程 stdin，接近常规终端体验。
 *
 * 注意：无真实 PTY，交互式程序（vi / top / 需要密码的 ssh 等）体验有限。
 * 完整 PTY + VT100 需要 NDK 或 Termux 类库。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainHandler = Handler(Looper.getMainLooper())
    private val ioExecutor = Executors.newCachedThreadPool()

    private var shellProcess: Process? = null
    private var shellWriter: OutputStreamWriter? = null
    private val running = AtomicBoolean(false)
    private val outputBuffer = SpannableStringBuilder()

    private var homeDir: String = "/"
    private var prompt = "$ "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeDir = filesDir.absolutePath

        binding.btnClear.setOnClickListener {
            outputBuffer.clear()
            binding.tvOutput.text = ""
            appendText("终端已清空\n")
            showPrompt()
        }

        binding.btnCtrlC.setOnClickListener {
            shellProcess?.let {
                try {
                    shellWriter?.write("\u0003")
                    shellWriter?.flush()
                    appendText("^C\n")
                } catch (_: Exception) {
                    appendText("[无法发送 Ctrl+C]\n")
                }
            }
        }

        binding.etCommand.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                sendCommand()
                true
            } else {
                false
            }
        }

        binding.btnSend.setOnClickListener { sendCommand() }

        startShell()
    }

    private fun startShell() {
        if (running.get()) return
        running.set(true)

        ioExecutor.execute {
            try {
                val pb = ProcessBuilder("/system/bin/sh", "-i")
                pb.directory(File(homeDir))
                pb.redirectErrorStream(true)
                val env = pb.environment()
                env["HOME"] = homeDir
                env["TERM"] = "xterm-256color"
                env["PATH"] = "/system/bin:/system/xbin:/vendor/bin:$homeDir"
                env["PS1"] = "\\$ "

                val process = pb.start()
                shellProcess = process
                shellWriter = OutputStreamWriter(process.outputStream)

                mainHandler.post {
                    appendText("Android Terminal — 持续会话模式\n")
                    appendText("工作目录: $homeDir\n")
                    appendText("提示: 输入命令后按回车；clear 清屏；exit 退出会话\n")
                    appendText("注意: 无真实 PTY，vi/top 等全屏程序体验有限\n\n")
                    showPrompt()
                }

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val buf = CharArray(4096)
                while (running.get()) {
                    val n = try {
                        reader.read(buf)
                    } catch (_: Exception) {
                        -1
                    }
                    if (n < 0) break
                    if (n > 0) {
                        val chunk = String(buf, 0, n)
                        mainHandler.post { appendText(chunk) }
                    }
                }

                val exit = try {
                    process.waitFor()
                } catch (_: Exception) {
                    -1
                }
                mainHandler.post {
                    appendText("\n[shell 已退出, code=$exit]\n")
                    appendText("正在重新启动会话...\n")
                    running.set(false)
                    shellProcess = null
                    shellWriter = null
                    mainHandler.postDelayed({ startShell() }, 800)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    appendText("启动 shell 失败: ${e.message}\n")
                    running.set(false)
                }
            }
        }
    }

    private fun sendCommand() {
        val cmd = binding.etCommand.text.toString()
        binding.etCommand.setText("")

        when (cmd.trim()) {
            "clear", "cls" -> {
                outputBuffer.clear()
                binding.tvOutput.text = ""
                showPrompt()
                return
            }
            "exit", "logout" -> {
                appendText("$cmd\n")
                try {
                    shellWriter?.write("exit\n")
                    shellWriter?.flush()
                } catch (_: Exception) {
                }
                return
            }
        }

        appendText(cmd + "\n")

        val writer = shellWriter
        if (writer == null) {
            appendText("[shell 未就绪，正在启动...]\n")
            startShell()
            return
        }

        ioExecutor.execute {
            try {
                writer.write(cmd + "\n")
                writer.flush()
            } catch (e: Exception) {
                mainHandler.post {
                    appendText("[写入失败: ${e.message}]\n")
                }
            }
        }
    }

    private fun showPrompt() {
        appendText(prompt)
    }

    private fun appendText(text: String) {
        val cleaned = text
            .replace("\u001B\\[[0-9;]*[a-zA-Z]".toRegex(), "")
            .replace("\r", "")

        outputBuffer.append(cleaned)
        if (outputBuffer.length > 80_000) {
            outputBuffer.delete(0, outputBuffer.length - 60_000)
        }
        binding.tvOutput.text = outputBuffer
        scrollToBottom()
    }

    private fun scrollToBottom() {
        binding.scrollView.post {
            binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onDestroy() {
        running.set(false)
        try {
            shellWriter?.write("exit\n")
            shellWriter?.flush()
        } catch (_: Exception) {
        }
        shellProcess?.destroy()
        ioExecutor.shutdownNow()
        super.onDestroy()
    }
}
