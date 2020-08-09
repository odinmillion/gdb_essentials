package com.github.odinmillion.gdbessentials.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointManager
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import java.awt.datatransfer.StringSelection
import javax.swing.Icon

internal class GenerateBreakCommandFromBreakpointsAction : AnAction {

    constructor() : super()

    constructor(text: String?, description: String?, icon: Icon?) : super(text, description, icon)

    override fun actionPerformed(event: AnActionEvent) {

        val dataContext: DataContext = event.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        var cmd: String = createCommandText(project) ?: return
        CopyPasteManager.getInstance().setContents(StringSelection(cmd))
        MyCopyReferenceUtil().setStatusBarText(project, "'$cmd' has been copied")
    }

    private fun createCommandText(project: Project): String? {

        val manager: XBreakpointManager = XDebuggerManager.getInstance(project).breakpointManager
        var breakpoints = manager.allBreakpoints.filterIsInstance<XLineBreakpointImpl<*>>()
        if (breakpoints.isEmpty()) return null

        val id = (0..1000000).random()
        val name = "set_breakpoints_$id"
        var cmd = "define $name\n"
        for (breakpoint in breakpoints.filter { b -> b.isEnabled }) {
            var path: String = getRelativePath(project, breakpoint.file) ?: continue
            var line = breakpoint.line + 1
            cmd += "    break $path:$line\n"
        }
        cmd += "end\n"
        cmd += "$name\n"
        return cmd
    }

    private fun getRelativePath(project: Project, virtualFile: VirtualFile?): String? {
        if (virtualFile == null) return null
        val psiFile: PsiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return null
        return MyCopyReferenceUtil().getFileFqn(psiFile)
    }
}
