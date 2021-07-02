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

package org.paradisehell.convex.booster.transformer

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.defaultClinit
import com.didiglobal.booster.transform.asm.findAll
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.paradisehell.convex.CONVEX_REGISTRY
import org.paradisehell.convex.FILED_REGISTRY
import org.paradisehell.convex.toInternalName

/**
 * @author Tao Cheng (tao@paradisehell.org)
 */
@AutoService(ClassTransformer::class)
class AutoTransformerTransformer : ClassTransformer {
    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name == CONVEX) {
            return transformConvex(klass)
        }
        return super.transform(context, klass)
    }

    private fun transformConvex(klass: ClassNode): ClassNode {
        val clinit = klass.methods.find {
            "${it.name}${it.desc}" == "<clinit>()V"
        } ?: klass.defaultClinit.also {
            klass.methods.add(it)
        }
        clinit.instructions.findAll(Opcodes.RETURN).forEach { ret ->
            clinit.instructions.insertBefore(ret, InsnList().apply {
                // Get transformers
                add(
                    FieldInsnNode(
                        Opcodes.GETSTATIC,
                        CONVEX,
                        "transformers",
                        "L${Type.getInternalName(HashMap::class.java)};"
                    )
                )
                // Get Convex#REGISTRY
                add(
                    FieldInsnNode(
                        Opcodes.GETSTATIC,
                        CONVEX_REGISTRY.toInternalName(),
                        FILED_REGISTRY,
                        "L${Type.getInternalName(Map::class.java)};"
                    )
                )
                // invoke map#putAll
                add(
                    MethodInsnNode(
                        Opcodes.INVOKEINTERFACE,
                        Type.getInternalName(Map::class.java),
                        "putAll",
                        "(L${Type.getInternalName(Map::class.java)};)V",
                        true
                    )
                )
            })
        }
        return klass
    }

    companion object {
        private const val CONVEX = "org/paradisehell/convex/Convex"
    }
}