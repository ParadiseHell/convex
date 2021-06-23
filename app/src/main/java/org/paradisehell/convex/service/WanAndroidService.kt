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
package org.paradisehell.convex.service

import org.paradisehell.convex.annotation.Transformer
import org.paradisehell.convex.entity.WeChatArticle
import org.paradisehell.convex.transformer.WanAndroidConvexTransformer
import retrofit2.http.GET


/**
 *
 * @author Tao Cheng (tao@paradisehell.org)
 */
@Transformer(WanAndroidConvexTransformer::class)
interface WanAndroidService {
    @GET("/wxarticle/chapters/json")
    suspend fun listWechatArticles(): List<WeChatArticle>
}