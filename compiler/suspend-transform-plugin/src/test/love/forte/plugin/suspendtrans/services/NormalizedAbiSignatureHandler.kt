package love.forte.plugin.suspendtrans.services

import org.jetbrains.kotlin.codegen.getClassFiles
import org.jetbrains.kotlin.test.backend.handlers.JvmBinaryArtifactHandler
import org.jetbrains.kotlin.test.directives.assertEqualsToDump
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.JvmClassFileArtifact
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.utils.MultiModuleInfoDumper
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

/**
 * ABI oracle: compares only the normalized public signature set of the generated classes.
 *
 * For every generated class the handler records one normalized class line and one line
 * per public/protected member (JVM name + descriptor + flags + annotation descriptors).
 * All lines are sorted before comparison, so unrelated ordering changes in the generated
 * output are tolerated while the public member set, flags and annotation metadata stay
 * pinned. Compiler temporary directories and rendered diagnostic text never leak into
 * the golden files.
 */
class NormalizedAbiSignatureHandler(testServices: TestServices) : JvmBinaryArtifactHandler(testServices) {
    private val dumper = MultiModuleInfoDumper()

    override fun processModule(module: TestModule, info: BinaryArtifacts.Jvm) {
        val builder = dumper.builderForModule(module)
        val classBlocks = sortedSetOf<String>()
        for (outputFile in (info as JvmClassFileArtifact).classFileFactory.getClassFiles()) {
            if (!outputFile.relativePath.endsWith(".class")) continue
            classBlocks += normalizeClass(outputFile.asByteArray())
        }
        classBlocks.forEach { builder.append(it) }
    }

    override fun processAfterAllModules(someModulesWereProcessed: Boolean) {
        if (dumper.isEmpty()) return
        assertEqualsToDump("abi.txt", dumper.generateResultingDump())
    }

    private fun normalizeClass(bytes: ByteArray): String {
        val reader = ClassReader(bytes)
        val collector = AbiClassCollector()
        reader.accept(collector, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        return collector.render()
    }

    private class AbiClassCollector : ClassVisitor(Opcodes.ASM9) {
        private var classLine: String = ""
        private val memberLines = sortedSetOf<String>()
        private val classAnnotations = sortedSetOf<String>()

        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            val ifcs = interfaces.orEmpty().sorted().joinToString(",", "[", "]")
            classLine = "class $name flags=${renderFlags(access)} super=${superName ?: "<none>"} interfaces=$ifcs"
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): org.jetbrains.org.objectweb.asm.AnnotationVisitor? {
            classAnnotations += descriptor
            return null
        }

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {
            if (access and (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED) == 0) return null
            val annotations = sortedSetOf<String>()
            val header = "  method $name$descriptor flags=${renderFlags(access)}"
            return object : MethodVisitor(Opcodes.ASM9) {
                override fun visitAnnotation(descriptor: String, visible: Boolean): org.jetbrains.org.objectweb.asm.AnnotationVisitor? {
                    annotations += descriptor
                    return null
                }

                override fun visitEnd() {
                    memberLines += "$header annotations=${annotations.joinToString(",", "[", "]")}"
                }
            }
        }

        override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
            if (access and (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED) == 0) return null
            val annotations = sortedSetOf<String>()
            val header = "  field $name $descriptor flags=${renderFlags(access)}"
            return object : FieldVisitor(Opcodes.ASM9) {
                override fun visitAnnotation(descriptor: String, visible: Boolean): org.jetbrains.org.objectweb.asm.AnnotationVisitor? {
                    annotations += descriptor
                    return null
                }

                override fun visitEnd() {
                    memberLines += "$header annotations=${annotations.joinToString(",", "[", "]")}"
                }
            }
        }

        fun render(): String = buildString {
            append(classLine)
            append(" annotations=")
            append(classAnnotations.joinToString(",", "[", "]"))
            append('\n')
            memberLines.forEach { append(it).append('\n') }
        }
    }

    companion object {
        private val FLAG_NAMES: List<Pair<Int, String>> = listOf(
            Opcodes.ACC_PUBLIC to "public",
            Opcodes.ACC_PROTECTED to "protected",
            Opcodes.ACC_PRIVATE to "private",
            Opcodes.ACC_FINAL to "final",
            Opcodes.ACC_STATIC to "static",
            Opcodes.ACC_ABSTRACT to "abstract",
            Opcodes.ACC_SYNCHRONIZED to "synchronized",
            Opcodes.ACC_VOLATILE to "volatile",
            Opcodes.ACC_TRANSIENT to "transient",
            Opcodes.ACC_NATIVE to "native",
            Opcodes.ACC_INTERFACE to "interface",
            Opcodes.ACC_ANNOTATION to "annotation",
            Opcodes.ACC_ENUM to "enum",
            Opcodes.ACC_SYNTHETIC to "synthetic",
            Opcodes.ACC_BRIDGE to "bridge",
            Opcodes.ACC_VARARGS to "varargs",
            Opcodes.ACC_STRICT to "strict",
            Opcodes.ACC_SUPER to "super",
        )

        private fun renderFlags(access: Int): String =
            FLAG_NAMES.filter { (mask, _) -> access and mask != 0 }
                .joinToString(",", "[", "]") { (_, name) -> name }
    }
}
