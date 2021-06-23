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
package org.paradisehell.convex.entity

import com.google.gson.annotations.SerializedName


/**
 *
 * @author Tao Cheng (tao@paradisehell.org)
 */
data class Article(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("link")
    val link: String? = null,
    @SerializedName("author")
    val author: String? = null,
    @SerializedName("superChapterName")
    val superChapterName: String? = null
)