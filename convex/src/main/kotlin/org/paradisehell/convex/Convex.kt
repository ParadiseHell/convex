package org.paradisehell.convex

import org.paradisehell.convex.transformer.ConvexTransformer
import java.util.*
import kotlin.collections.HashMap

/**
 * A [Convex] is a [ConvexTransformer] Registry.
 *
 * @author Tao Cheng (tao@paradisehell.org)
 */
object Convex {
    private val transformers = HashMap<Class<out ConvexTransformer>, ConvexTransformer>()

    /**
     * Register a [ConvexTransformer]
     *
     * @param transformer a [ConvexTransformer] instance.
     */
    @JvmStatic
    fun registerConvexTransformer(transformer: ConvexTransformer) {
        transformers[transformer::class.java] = transformer
    }

    /**
     * Get a [ConvexTransformer] by its class
     *
     * @return a [ConvexTransformer] or null if it doesn't exist.
     */
    fun getConvexTransformer(clazz: Class<out ConvexTransformer>): ConvexTransformer {
        return try {
            transformers.getOrPut(clazz) {
                clazz.getDeclaredConstructor().newInstance().also {
                    transformers[clazz] = it
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException(
                "Cannot find ${clazz.name} from Convex, " +
                        "please call Convex#addConvexTransformer first."
            )
        }
    }

    /**
     * Get all [ConvexTransformer]s
     *
     * @return A list of [ConvexTransformer]
     */
    @JvmStatic
    fun getConvexTransformers(): List<ConvexTransformer> {
        return Collections.unmodifiableList(transformers.values.toList())
    }
}