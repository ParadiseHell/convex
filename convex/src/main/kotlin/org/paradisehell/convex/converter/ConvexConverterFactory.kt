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
import org.paradisehell.convex.annotation.DisableTransformer
import org.paradisehell.convex.annotation.Transformer
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.InputStream
import java.lang.reflect.Type

/**
 * A [ConvexConverterFactory] which transforms original response [InputStream] to business
 * data [InputStream].
 *
 * @author Tao Cheng (tao@paradisehell.org)
 */
class ConvexConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        // Check whether service's method annotated with DisableTransformer or not
        annotations.find { it is DisableTransformer }?.let {
            return null
        }
        // Find Transformer
        return annotations
            .filterIsInstance<Transformer>()
            .firstOrNull()?.value
            ?.let { transformerClazz ->
                retrofit.nextResponseBodyConverter<Any>(
                    this, type, annotations
                )?.let { converter ->
                    ConvexConverter<Any>(transformerClazz, converter)
                }
            }
    }
}