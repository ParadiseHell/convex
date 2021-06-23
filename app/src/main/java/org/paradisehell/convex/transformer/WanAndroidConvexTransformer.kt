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
package org.paradisehell.convex.transformer

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.paradisehell.convex.entity.BaseResponse
import java.io.IOException
import java.io.InputStream


/**
 *
 * @author Tao Cheng (tao@paradisehell.org)
 */
class WanAndroidConvexTransformer : ConvexTransformer {
    private val gson = Gson()

    @Throws(IOException::class)
    override fun transform(original: InputStream): InputStream {
        val response = gson.fromJson<BaseResponse<JsonElement>>(
            original.reader(),
            object : TypeToken<BaseResponse<JsonElement>>() {
            }.type
        )
        if (response.errorCode == 0 && response.data != null) {
            return response.data.toString().byteInputStream()
        }
        throw IOException(
            "errorCode : " + response.errorCode + " ; errorMsg : " + response.errorMsg
        )
    }
}
