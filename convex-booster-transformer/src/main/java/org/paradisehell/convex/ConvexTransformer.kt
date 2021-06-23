package org.paradisehell.convex

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.isInterface
import com.google.auto.service.AutoService
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

@AutoService(ClassTransformer::class)
class ConvexTransformer : ClassTransformer {
    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        return klass.takeIf { it.isInterface }?.visibleAnnotations
            ?.onEach {
                println("AnnotationNode : ${it.desc}")
            }
            ?.firstOrNull { it.desc == TRANSFORMER_DEC }
            ?.let {
                addTransformerAnnotationToServiceMethod(klass, it)
            } ?: super.transform(context, klass)
    }

    private fun addTransformerAnnotationToServiceMethod(
        klass: ClassNode,
        transformerAnnotation: AnnotationNode
    ): ClassNode {
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
        private const val TRANSFORMER_DEC = "org.paradisehell.convex.annotation.Transformer"
    }
}