package com.github.odinmillion.gdbessentials.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiFile
import java.awt.datatransfer.StringSelection
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.Icon

internal class CreateBreakCommandAction : AnAction {

    constructor() : super()

    constructor(text: String?, description: String?, icon: Icon?) : super(text, description, icon)

    override fun actionPerformed(event: AnActionEvent) = try {
        var cmd = createCommandText(event)
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

    private fun createCommandText(event: AnActionEvent): String? {

        fun getLineNumber(event: AnActionEvent): Int {

            var data: Caret? = event.getData(LangDataKeys.CARET)
            var visualPosition = data!!.caretModel.visualPosition
            return visualPosition.line + 1
        }

        fun getRelativePath(event: AnActionEvent): String {

            var basePath: String? = event.project?.basePath
            var file: PsiFile? = event.getData(LangDataKeys.PSI_FILE)
            var path = file!!.virtualFile.path
            return path.substring(basePath!!.length + 1)
        }

        var relativePath = getRelativePath(event)
        val lineNumber = getLineNumber(event)
        return "break $relativePath:$lineNumber"
    }
}
