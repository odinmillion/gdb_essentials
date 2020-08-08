package com.github.odinmillion.gdbessentials.services

import com.github.odinmillion.gdbessentials.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
