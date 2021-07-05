/*
 * Copyright (C) 2021 ParadiseHell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.paradisehell.convex.booster.task

import com.didiglobal.booster.transform.asm.defaultClinit
import com.didiglobal.booster.transform.asm.defaultInit
import com.didiglobal.booster.transform.asm.find
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.paradisehell.convex.CONVEX_REGISTRY
import org.paradisehell.convex.CONVEX_TRANSFORMER
import org.paradisehell.convex.FILED_REGISTRY
import org.paradisehell.convex.toInternalName
import java.io.File
import java.util.*
import java.util.jar.JarFile
import kotlin.collections.HashMap

/**
 * @author Tao Cheng (tao@paradisehell.org)
 */
@CacheableTask
open class GenerateConvexRegistryTask : DefaultTask() {
    private val transformers = LinkedList<String>()

    @get:InputFile
    lateinit var inputFile: File

    @get:OutputDirectories
    lateinit var outputDir: File

    private val registryFile by lazy {
        File(outputDir, CONVEX_REGISTRY.replace(".", File.separator) + ".class")
            .also { it.parentFile?.takeIf { parent -> parent.exists().not() }?.mkdirs() }
    }

    @TaskAction
    fun generate() {
        // Collect ConvexTransformers
        inputFile.takeIf { it.extension == "jar" }?.let { file ->
            JarFile(file).use { jar ->
                jar.entries().iterator().asSequence()
                    .filter { it.name.contains(CONFIG_FILE_REGEX) }
                    .map { jar.getInputStream(it).bufferedReader().readLines() }
                    .toList()
            }
        }?.flatten()?.forEach { transformers.add(it) }
        println("Find ${transformers.size} ConvexTransformers :")
        transformers.forEachIndexed { index, t ->
            println("${index + 1}. $t")
        }
        // Generate ConvexRegistry class
        generateConvexRegistryClass()
    }

    private fun generateConvexRegistryClass() {
        val klass = ClassNode()
        // 1. Define Class info
        klass.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
            CONVEX_REGISTRY.toInternalName(),
            null,
            "java/lang/Object",
            null
        )
        // 2. Add Constructor
        klass.defaultInit.also { it.access = Opcodes.ACC_PRIVATE }.let {
            klass.methods.add(it)
        }
        // 3. Add filed REGISTRY
        klass.fields.add(
            FieldNode(
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL,
                FILED_REGISTRY,
                "L${Type.getInternalName(Map::class.java)};",
                buildString {
                    append("Ljava/util/Map<")
                    append("Ljava/lang/Class<")
                    append("L${CONVEX_TRANSFORMER.toInternalName()};")
                    append(">;")
                    append("L${CONVEX_TRANSFORMER.toInternalName()};")
                    append(">;")
                },
                null
            )
        )
        // 4. add static block
        val clint = klass.defaultClinit.also { klass.methods.add(it) }
        clint.instructions.find(Opcodes.RETURN)?.let { ret ->
            clint.instructions.insertBefore(ret, InsnList().apply {
                // new HasMap(size)
                add(TypeInsnNode(Opcodes.NEW, Type.getInternalName(HashMap::class.java)))
                add(InsnNode(Opcodes.DUP))
                add(IntInsnNode(Opcodes.BIPUSH, transformers.size))
                add(
                    MethodInsnNode(
                        Opcodes.INVOKESPECIAL,
                        Type.getInternalName(HashMap::class.java),
                        "<init>",
                        "(I)V",
                        false
                    )
                )
                add(
                    FieldInsnNode(
                        Opcodes.PUTSTATIC,
                        CONVEX_REGISTRY.toInternalName(),
                        FILED_REGISTRY,
                        "L${Type.getInternalName(Map::class.java)};",
                    )
                )
                // add ConvexTransformer
                transformers.forEach { transformer ->
                    // get REGISTRY
                    add(
                        FieldInsnNode(
                            Opcodes.GETSTATIC,
                            CONVEX_REGISTRY.toInternalName(),
                            FILED_REGISTRY,
                            "L${Type.getInternalName(Map::class.java)};"
                        )
                    )
                    // ConvexTransformer.class
                    add(LdcInsnNode(Type.getObjectType(transformer.toInternalName())))
                    // new ConvexTransformer()
                    add(TypeInsnNode(Opcodes.NEW, transformer.toInternalName()))
                    add(InsnNode(Opcodes.DUP))
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            transformer.toInternalName(),
                            "<init>",
                            "()V",
                            false
                        )
                    )
                    // Map#put
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKEINTERFACE,
                            Type.getInternalName(Map::class.java),
                            "put",
                            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                            true
                        )
                    )
                    // pop
                    add(InsnNode(Opcodes.POP))
                }
            })
        }
        // 5. Write to file
        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        klass.accept(classWriter)
        registryFile.writeBytes(classWriter.toByteArray())
    }

    companion object {
        private val CONFIG_FILE_REGEX = Regex(
            "META-INF.*org\\.paradisehell\\.convex\\.transformer\\.ConvexTransformer"
        )
    }
}