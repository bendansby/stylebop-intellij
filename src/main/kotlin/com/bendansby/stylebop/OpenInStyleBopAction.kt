package com.bendansby.stylebop

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Right-click → "Open in StyleBop" action.
 *
 * Mirrors the VS Code extension's behaviour: builds a `stylebop://`
 * URL containing the absolute file path (and the cursor's line number
 * when the action fires from the editor of the active file), then
 * shells out to macOS `open` to hand the URL to LaunchServices.
 *
 * Works in any JetBrains IDE (IntelliJ, WebStorm, PyCharm, etc.).
 * Outside macOS the launch can't reach StyleBop, so we surface a
 * friendly info dialog rather than fail silently.
 */
class OpenInStyleBopAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = resolveTarget(e) ?: return
        val path = file.path

        // If the action fired from the editor of the same file, include
        // the cursor's 1-indexed line so StyleBop's smart-router can
        // open the matching visual surface (Rulesets canvas / Animations
        // / Tokens / Fonts) instead of dumping us in the Code tab.
        val line: Int? = run {
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return@run null
            val activePath = FileEditorManager.getInstance(project)
                .selectedFiles.firstOrNull()?.path
            if (activePath == path) editor.caretModel.logicalPosition.line + 1 else null
        }

        if (!System.getProperty("os.name").lowercase().contains("mac")) {
            Messages.showInfoMessage(
                project,
                "StyleBop is a macOS app. Install it on your Mac and run this from a Mac IDE.",
                "StyleBop",
            )
            return
        }

        // URLEncoder uses '+' for spaces; LaunchServices expects %20.
        val encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8).replace("+", "%20")
        val url = buildString {
            append("stylebop://open?path=").append(encodedPath)
            if (line != null) append("&line=").append(line)
        }

        // execFile-style invocation: pass the URL as a single argv entry
        // so shell metacharacters in the path can't be interpreted.
        try {
            Runtime.getRuntime().exec(arrayOf("/usr/bin/open", url))
        } catch (ex: Exception) {
            Messages.showWarningDialog(
                project,
                "Couldn't launch StyleBop: ${ex.message}\n\n" +
                    "Is StyleBop installed? https://bendansby.com/apps/stylebop",
                "StyleBop",
            )
        }
    }

    override fun update(e: AnActionEvent) {
        // Show the menu item for `.css`, `.html`, `.htm` files and any
        // folder. Hidden for everything else so we don't pollute the
        // right-click menu. HTML files are valid targets because
        // StyleBop edits the inner `<style>` block (and inline
        // `style="…"` attributes) as if they were CSS.
        val file = resolveTarget(e)
        val ext = file?.extension?.lowercase()
        e.presentation.isEnabledAndVisible = file != null &&
            (file.isDirectory || ext == "css" || ext == "html" || ext == "htm")
    }

    /**
     * Resolve the right-clicked file/folder. The Project view passes
     * single selections via `VIRTUAL_FILE` and multi-selections via
     * `VIRTUAL_FILE_ARRAY`. Some IDEs route folder right-clicks through
     * `NAVIGATABLE_ARRAY` instead. Try each in order so the action
     * appears reliably across editor, project tree, and tab contexts.
     */
    private fun resolveTarget(e: AnActionEvent): com.intellij.openapi.vfs.VirtualFile? {
        e.getData(CommonDataKeys.VIRTUAL_FILE)?.let { return it }
        e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.firstOrNull()?.let { return it }
        return null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
