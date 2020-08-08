package com.github.odinmillion.gdbessentials.services

import com.intellij.openapi.project.Project
import com.github.odinmillion.gdbessentials.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
