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

package com.example.mylibrary

import org.paradisehell.convex.annotation.AutoTransformer
import org.paradisehell.convex.transformer.ConvexTransformer
import java.io.InputStream

/**
 * @author Tao Cheng (tao@paradisehell.org)
 */
@AutoTransformer
class TestConvexTransformer : ConvexTransformer {
    override fun transform(original: InputStream): InputStream {
        return original
    }
}