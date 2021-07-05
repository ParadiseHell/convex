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
import com.didiglobal.booster.transform.asm.isInterface
import com.google.auto.service.AutoService
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

/**
 * A [ServiceTransformer] will auto add @Transformer annotation to all service's methods.
 *
 * Example as following:
 * ```
 * @Transformer(XXX:class)
 * interface TestService {
 *     fun a()
 *     fun b()
 * }
 * ```
 *
 * So we don't need to add @Transformer to every method.
 *
 * @author Tao Cheng (tao@paradisehell.org)
 */
@AutoService(ClassTransformer::class)
class ServiceTransformer : ClassTransformer {
    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        return klass.takeIf { it.isInterface }?.visibleAnnotations
            ?.firstOrNull { it.desc == TRANSFORMER_DEC }
            ?.let {
                addTransformerAnnotationToServiceMethod(klass, it)
            } ?: super.transform(context, klass)
    }

    private fun addTransformerAnnotationToServiceMethod(
        klass: ClassNode,
        transformerAnnotation: AnnotationNode
    ): ClassNode {
        println(
            "Service [${klass.name.replace("/", ".")}] " +
                    "add @Transformer annotation to its methods."
        )
        klass.methods
            .filter { method ->
                method.visibleAnnotations.none { it.desc == TRANSFORMER_DEC }
            }
            .forEach { method ->
                method.visibleAnnotations.add(transformerAnnotation)
            }
        return klass
    }

    companion object {
        private const val TRANSFORMER_DEC = "Lorg/paradisehell/convex/annotation/Transformer;"
    }
}