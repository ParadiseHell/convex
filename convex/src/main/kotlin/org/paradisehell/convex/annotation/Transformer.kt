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
package org.paradisehell.convex.annotation

import org.paradisehell.convex.transformer.ConvexTransformer
import kotlin.reflect.KClass


/**
 * The [Transformer] annotation is to define which [ConvexTransformer] is use to transform
 * service's method response.
 *
 * Usage :
 * ```
 * interface XXXService {
 *      @GET("xxx")
 *      @Transformer(XXXTransformer::class)
 *      fun xxxMethod() : Any
 * }
 * ```
 *
 * @author Tao Cheng (tao@paradisehell.org)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Transformer(val value: KClass<out ConvexTransformer>)
