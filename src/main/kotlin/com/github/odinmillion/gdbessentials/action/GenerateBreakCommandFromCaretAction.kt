package com.github.odinmillion.gdbessentials.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import java.awt.datatransfer.StringSelection
import javax.swing.Icon

internal class GenerateBreakCommandFromCaretAction : AnAction {

    constructor() : super()

    constructor(text: String?, description: String?, icon: Icon?) : super(text, description, icon)

    override fun actionPerformed(e: AnActionEvent) {

        val dataContext: DataContext = e.dataContext
        val editor = CommonDataKeys.EDITOR.getData(dataContext)
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        val elements = MyCopyReferenceUtil().getElementsToCopy(editor, dataContext)

        val name = getQualifiedName(editor, elements)
        if (name != null) {
            emitResult(name, project)
            return
        } else if (editor != null && project != null) {
            val document = editor.document
            val file = PsiDocumentManager.getInstance(project).getCachedPsiFile(document)
            if (file != null) {
                val location = MyCopyReferenceUtil().getFileFqn(file) + ":" + (editor.caretModel.logicalPosition.line + 1)
                emitResult(location, project)
            }
        }
    }

    private fun emitResult(location: String, project: Project?) {
        val cmd = "break $location"
        CopyPasteManager.getInstance().setContents(StringSelection(cmd))
        MyCopyReferenceUtil().setStatusBarText(project, "'$cmd' has been copied")
    }

    private fun getQualifiedName(editor: Editor?, elements: List<PsiElement?>): String? {
        return MyCopyReferenceUtil().doCopy(elements, editor)
    }
}
