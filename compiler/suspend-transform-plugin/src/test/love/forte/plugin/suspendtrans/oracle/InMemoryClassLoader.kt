package love.forte.plugin.suspendtrans.oracle

import org.jetbrains.kotlin.codegen.ClassFileFactory

/**
 * Defines compiled classes straight from the in-memory [ClassFileFactory] so
 * oracle tests never touch compiler temporary output directories.
 *
 * A fresh instance is created per module and per handler, which keeps compiled
 * classes (and their static state) isolated between test cases.
 */
internal class InMemoryClassLoader(
    private val classes: Map<String, ByteArray>,
    parent: ClassLoader,
) : ClassLoader(parent) {
    override fun findClass(name: String): Class<*> {
        val bytes = classes[name.replace('.', '/') + ".class"]
            ?: throw ClassNotFoundException(name)
        return defineClass(name, bytes, 0, bytes.size)
    }

    companion object {
        fun from(factory: ClassFileFactory, parent: ClassLoader): InMemoryClassLoader {
            val classes = factory.asList().associate { file ->
                file.relativePath to file.asByteArray()
            }
            return InMemoryClassLoader(classes, parent)
        }
    }
}
