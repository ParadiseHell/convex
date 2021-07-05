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

package org.paradisehell.convex.booster.processor

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.tasks.MergeJavaResourceTask
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.tasks.compile.JavaCompile
import org.paradisehell.convex.booster.Build
import org.paradisehell.convex.booster.task.GenerateConvexRegistryTask

/**
 * @author Tao Cheng (tao@paradisehell.org)
 */
@Suppress("DefaultLocale", "UnstableApiUsage")
@AutoService(VariantProcessor::class)
class AutoTransformerVariantProcessor : VariantProcessor {
    override fun process(variant: BaseVariant) {
        variant.project.dependencies.add("kapt", "${Build.GROUP}:convex-compiler:${Build.VERSION}")
        if (variant !is ApplicationVariant) {
            return
        }
        val variantName = variant.name.capitalize()
        val generateTask = variant.project.tasks.create(
            "generate${variantName}ConvexRegistry",
            GenerateConvexRegistryTask::class.java
        )
        variant.project.tasks.findByName("merge${variantName}JavaResource")
            ?.also { generateTask.dependsOn(it) }
            ?.let { it as? MergeJavaResourceTask }?.outputFile?.asFile?.orNull?.let {
                generateTask.inputFile = it
            }
        variant.project.tasks.findByName("compile${variantName}JavaWithJavac")
            ?.let { it as? JavaCompile }?.destinationDirectory?.asFile?.orNull?.let {
                generateTask.outputDir = it
            }
        variant.project.tasks.filterIsInstance<TransformTask>()
            .filter { it.name.contains(Regex("transformClassesWith.*For${variantName}")) }
            .forEach { it.dependsOn(generateTask) }
    }
}