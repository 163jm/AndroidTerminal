package com.example.androidterminal

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import java.io.File

/**
 * 真实 PTY 终端（Termux terminal-emulator + terminal-view）。
 * 全屏 TerminalView，无底部输入框；键盘直接进入伪终端。
 */
class MainActivity : AppCompatActivity(), TerminalSessionClient, TerminalViewClient {

    private lateinit var terminalView: TerminalView
    private var session: TerminalSession? = null
    private var fontSize = 28

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        terminalView = findViewById(R.id.terminal_view)
        terminalView.setTerminalViewClient(this)
        terminalView.setTextSize(fontSize)
        terminalView.setBackgroundColor(Color.BLACK)
        terminalView.keepScreenOn = true

        startShellSession()

        terminalView.setOnClickListener {
            terminalView.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(terminalView, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun startShellSession() {
        val home = filesDir.absolutePath
        File(home).mkdirs()

        val shell = "/system/bin/sh"
        val args = arrayOf("sh", "-")
        val env = arrayOf(
            "TERM=xterm-256color",
            "COLORTERM=truecolor",
            "HOME=$home",
            "PREFIX=$home",
            "TMPDIR=$cacheDir",
            "PATH=/system/bin:/system/xbin:/vendor/bin:$home/bin",
            "LANG=en_US.UTF-8"
        )

        session?.finishIfRunning()
        session = TerminalSession(shell, home, args, env, 3000, this)
        terminalView.attachSession(session)
        terminalView.requestFocus()
    }

    override fun onTextChanged(changedSession: TerminalSession) {
        terminalView.onScreenUpdated()
    }

    override fun onTitleChanged(changedSession: TerminalSession) {}

    override fun onSessionFinished(finishedSession: TerminalSession) {
        runOnUiThread {
            Toast.makeText(this, "Shell 已退出，正在重启…", Toast.LENGTH_SHORT).show()
            terminalView.postDelayed({ startShellSession() }, 400)
        }
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText("terminal", text))
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val text = cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString() ?: return
        session?.write(text)
    }

    override fun onBell(session: TerminalSession) {}
    override fun onColorsChanged(session: TerminalSession) {
        terminalView.onScreenUpdated()
    }

    override fun onTerminalCursorStateChange(state: Boolean) {}
    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}
    override fun getTerminalCursorStyle(): Int? = null

    override fun logError(tag: String, message: String) {}
    override fun logWarn(tag: String, message: String) {}
    override fun logInfo(tag: String, message: String) {}
    override fun logDebug(tag: String, message: String) {}
    override fun logVerbose(tag: String, message: String) {}
    override fun logStackTraceWithMessage(tag: String, message: String, e: Exception?) {}
    override fun logStackTrace(tag: String, e: Exception?) {}

    override fun onScale(scale: Float): Float {
        if (scale < 0.9f || scale > 1.1f) {
            val increase = scale > 1f
            fontSize += if (increase) 2 else -2
            fontSize = fontSize.coerceIn(18, 56)
            terminalView.setTextSize(fontSize)
        }
        return 1f
    }

    override fun onSingleTapUp(e: MotionEvent) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(terminalView, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean = false
    override fun shouldEnforceCharBasedInput(): Boolean = false
    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
    override fun isTerminalViewSelected(): Boolean = true
    override fun copyModeChanged(copyMode: Boolean) {}

    override fun onKeyDown(keyCode: Int, e: KeyEvent, session: TerminalSession): Boolean = false
    override fun onKeyUp(keyCode: Int, e: KeyEvent): Boolean = false
    override fun onLongPress(event: MotionEvent): Boolean = false

    override fun readControlKey(): Boolean = false
    override fun readAltKey(): Boolean = false
    override fun readShiftKey(): Boolean = false
    override fun readFnKey(): Boolean = false

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession): Boolean = false
    override fun onEmulatorSet() {}

    override fun onDestroy() {
        session?.finishIfRunning()
        super.onDestroy()
    }
}
