package com.github.odinmillion.gdbessentials.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointManager
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import java.awt.datatransfer.StringSelection
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.Icon

internal class GenerateBreakCommandFromBreakpointsAction : AnAction {

    constructor() : super()

    constructor(text: String?, description: String?, icon: Icon?) : super(text, description, icon)

    override fun actionPerformed(event: AnActionEvent) = try {

        var cmd = createCommandText(event, 1)
        CopyPasteManager.getInstance().setContents(StringSelection(cmd))
        Notifications.Bus.notify(
            Notification(
                "gdb_essentials",
                "GDB Essentials | Success",
                "Gdb command was copied into the clipboard: '$cmd'",
                NotificationType.INFORMATION
            )
        )
    } catch (e: @Suppress("TooGenericExceptionCaught") Throwable) {

        var message = createErrorMessage(e)
        Notifications.Bus.notify(
            Notification(
                "gdb_essentials",
                "GDB Essentials | Error",
                message,
                NotificationType.ERROR
            )
        )
    }

    private fun createErrorMessage(e: Throwable): String {

        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        val exceptionAsString = sw.toString()
        return "Can't create gdb command: $e $exceptionAsString"
    }

    private fun createCommandText(event: AnActionEvent, dropCount: Int): String? {

        val manager: XBreakpointManager = XDebuggerManager.getInstance(event.project!!).breakpointManager
        var breakpoints = manager.allBreakpoints.filterIsInstance<XLineBreakpointImpl<*>>()

        val id = (0..1000000).random()
        val name = "set_breakpoints_$id"
        var cmd = "define $name\n"
        for (breakpoint in breakpoints.filter { b -> b.isEnabled }) {
            var path = getRelativePath(event, breakpoint.file!!.path, dropCount)
            var line = breakpoint.line + 1
            cmd += "    break $path:$line\n"
        }
        cmd += "end\n"
        cmd += "$name\n"
        return cmd
    }

    fun getRelativePath(event: AnActionEvent, fullPath: String, dropCount: Int): String {

        var basePath = event.project!!.basePath
        val substring = fullPath.substring(basePath!!.length + 1)
        var split: List<String> = substring.split("/").drop(dropCount)
        return split.joinToString("/")
    }
}
