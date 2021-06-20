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
package org.paradisehell.convex.converter

import okhttp3.ResponseBody
import org.paradisehell.convex.Convex
import org.paradisehell.convex.transformer.ConvexTransformer
import retrofit2.Converter
import java.io.IOException
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * A [ConvexConverter] which finds [ConvexTransformer] candidate from [Convex] and convert
 * business data [InputStream] to data.
 *
 * @author Tao Cheng (tao@paradisehell.org)
 */
open class ConvexConverter<T> constructor(
    private val transformerClazz: KClass<out ConvexTransformer>,
    private val candidateConverter: Converter<ResponseBody, *>
) : Converter<ResponseBody, T?> {

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T? {
        return Convex.getConvexTransformers()
            .find { it::class.java == transformerClazz.java }
            .also {
                if (it == null) {
                    throw IOException(
                        "Cannot find ${transformerClazz.java.name} from Convex, " +
                                "please call Convex#addConvexTransformer first."
                    )
                }
            }?.let { transformer ->
                transformer.transform(value.byteStream()).let { responseStream ->
                    ResponseBody.create(value.contentType(), responseStream.readBytes())
                }
            }?.let { responseBody ->
                candidateConverter.convert(responseBody) as T?
            }
    }
}