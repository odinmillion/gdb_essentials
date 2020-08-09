package com.github.odinmillion.gdbessentials.action

// copy-pasted from com.intellij.util.containers.ContainerUtil
class MyContainerUtil {
    fun <T> addIfNotNull(result: MutableList<T>, element: T?) {
        if (element != null) {
            result.add(element)
        }
    }

    fun <T> addAll(c: MutableList<T>, vararg elements: T): Boolean {
        var result = false
        for (element in elements) result = result or c.add(element)
        return result
    }
}
