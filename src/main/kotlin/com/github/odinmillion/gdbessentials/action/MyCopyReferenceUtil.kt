package com.github.odinmillion.gdbessentials.action

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.codeInsight.daemon.impl.IdentifierUtil
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.ide.actions.CopyReferenceAction
import com.intellij.ide.actions.QualifiedNameProviderUtil
import com.intellij.ide.scratch.RootType
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.ex.StatusBarEx
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.containers.ContainerUtil
import java.io.File

// copy-pasted from com.intellij.ide.actions.CopyReferenceUtil
class MyCopyReferenceUtil {
    fun highlight(editor: Editor?, project: Project?, elements: List<PsiElement>) {
        val highlightManager = HighlightManager.getInstance(project)
        if (elements.size == 1 && editor != null && project != null) {
            val element = elements[0]
            val nameIdentifier = IdentifierUtil.getNameIdentifier(element)
            if (nameIdentifier != null) {
                highlightManager
                        .addOccurrenceHighlights(editor, arrayOf(nameIdentifier), EditorColors.SEARCH_RESULT_ATTRIBUTES, true, null)
            } else {
                val reference = TargetElementUtil.findReference(editor, editor.caretModel.offset)
                if (reference != null) {
                    highlightManager
                            .addOccurrenceHighlights(editor, arrayOf(reference), EditorColors.SEARCH_RESULT_ATTRIBUTES, true, null)
                } else if (element !== PsiDocumentManager.getInstance(project).getCachedPsiFile(editor.document)) {
                    highlightManager.addOccurrenceHighlights(editor, arrayOf(element), EditorColors.SEARCH_RESULT_ATTRIBUTES, true, null)
                }
            }
        }
    }

    fun getElementsToCopy(editor: Editor?, dataContext: DataContext?): List<PsiElement?> {
        val elements: MutableList<PsiElement> = ArrayList()
        if (editor != null) {
            val reference = TargetElementUtil.findReference(editor)
            if (reference != null) {
                MyContainerUtil().addIfNotNull(elements, reference.element)
            }
        }
        if (elements.isEmpty()) {
            val psiElements = LangDataKeys.PSI_ELEMENT_ARRAY.getData(dataContext!!)
            if (psiElements != null) {
                MyContainerUtil().addAll(elements, *psiElements)
            }
        }
        if (elements.isEmpty()) {
            MyContainerUtil().addIfNotNull(elements, CommonDataKeys.PSI_ELEMENT.getData(dataContext!!))
        }
        if (elements.isEmpty() && editor == null) {
            val project = CommonDataKeys.PROJECT.getData(dataContext!!)
            val files = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext)
            if (project != null && files != null) {
                for (file in files) {
                    MyContainerUtil().addIfNotNull(elements, PsiManager.getInstance(project).findFile(file!!))
                }
            }
        }
        return ContainerUtil.mapNotNull(elements) { element: PsiElement? -> if (element is PsiFile && !element.viewProvider.isPhysical) null else adjustElement(element) }
    }

    fun adjustElement(element: PsiElement?): PsiElement {
        val adjustedElement = QualifiedNameProviderUtil.adjustElementToCopy(element!!)
        return adjustedElement ?: element
    }

    fun setStatusBarText(project: Project?, message: String?) {
        if (project != null) {
            val statusBar = WindowManager.getInstance().getStatusBar(project) as StatusBarEx
            if (statusBar != null) {
                statusBar.info = message
            }
        }
    }

    fun getQualifiedNameFromProviders(element: PsiElement?): String? {
        return if (element == null) null else DumbService.getInstance(element.project).computeWithAlternativeResolveEnabled<String?, RuntimeException> { QualifiedNameProviderUtil.getQualifiedName(element) }
    }

    fun doCopy(elements: List<PsiElement?>, editor: Editor?): String? {
        if (elements.isEmpty()) return null
        val fqns: MutableList<String> = ArrayList()
        for (element in elements) {
            val fqn = elementToFqn(element, editor) ?: return null
            fqns.add(fqn)
        }
        return StringUtil.join(fqns, "\n")
    }

    fun elementToFqn(element: PsiElement?, editor: Editor?): String? {
        var result = getQualifiedNameFromProviders(element)
        if (result != null) return result
        if (editor != null) { // IDEA-70346
            val reference = TargetElementUtil.findReference(editor, editor.caretModel.offset)
            if (reference != null) {
                result = getQualifiedNameFromProviders(reference.resolve())
                if (result != null) return result
            }
        }
        if (element is PsiFile) {
            return FileUtil.toSystemIndependentName(getFileFqn(element))
        }
        return if (element is PsiDirectory) {
            FileUtil.toSystemIndependentName(getVirtualFileFqn(element.virtualFile, element.getProject()))
        } else null
    }

    fun getFileFqn(file: PsiFile): String {
        val virtualFile = file.virtualFile
        return if (virtualFile == null) file.name else getVirtualFileFqn(virtualFile, file.project)
    }

    fun getVirtualFileFqn(virtualFile: VirtualFile, project: Project): String {
        for (provider in CopyReferenceAction.VirtualFileQualifiedNameProvider.EP_NAME.extensionList) {
            val qualifiedName = provider.getQualifiedName(project, virtualFile)
            if (qualifiedName != null) {
                return qualifiedName
            }
        }
        val module = ProjectFileIndex.getInstance(project).getModuleForFile(virtualFile, false)
        if (module != null) {
            for (root in ModuleRootManager.getInstance(module).contentRoots) {
                val relativePath = VfsUtilCore.getRelativePath(virtualFile, root)
                if (relativePath != null) {
                    return relativePath
                }
            }
        }
        val dir = project.baseDir ?: return virtualFile.path
        val relativePath = VfsUtilCore.getRelativePath(virtualFile, dir)
        if (relativePath != null) {
            return relativePath
        }
        val rootType = RootType.forFile(virtualFile)
        if (rootType != null) {
            val scratchRootVirtualFile = VfsUtil.findFileByIoFile(File(ScratchFileService.getInstance().getRootPath(rootType)), false)
            if (scratchRootVirtualFile != null) {
                val scratchRelativePath = VfsUtilCore.getRelativePath(virtualFile, scratchRootVirtualFile)
                if (scratchRelativePath != null) {
                    return scratchRelativePath
                }
            }
        }
        return virtualFile.path
    }
}
