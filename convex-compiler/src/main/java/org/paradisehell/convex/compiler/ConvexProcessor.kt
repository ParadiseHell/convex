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

package org.paradisehell.convex.compiler

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation

/**
 * @author Tao Cheng (tao@paradisehell.org)
 */
@SupportedAnnotationTypes("org.paradisehell.convex.annotation.AutoTransformer")
@SupportedOptions("verbose")
class ConvexProcessor : AbstractProcessor() {
    private lateinit var messager: Messager
    private lateinit var filer: Filer

    private var verbose = false
    private val transformers = mutableSetOf<String>()

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        messager = processingEnv.messager
        filer = processingEnv.filer
        verbose = processingEnv.options["verbose"]?.toBoolean() == true

        if (roundEnv.processingOver() && annotations.isNotEmpty()) {
            error("Unexpected processing state: annotations still available after processing over")
            return false
        }
        if (annotations.isEmpty()) {
            return false
        }
        collectTransformers(annotations, roundEnv)
        generateConfigFiles()
        return true
    }

    private fun collectTransformers(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ) {
        annotations
            // find all class annotated with AutoTransformer
            .find { it.qualifiedName.toString() == AUTO_TRANSFORMER }
            ?.let { roundEnv.getElementsAnnotatedWith(it) }
            ?.filterIsInstance<TypeElement>()
            // check class interface and modifiers
            ?.filter { it.interfaces.find { itf -> itf.toString() == CONVEX_TRANSFORMER } != null }
            ?.filter {
                // public but not abstract
                it.modifiers.contains(Modifier.PUBLIC)
                        && it.modifiers.contains(Modifier.ABSTRACT).not()
            }
            // collect
            ?.map { it.toString() }
            ?.let { transformers.addAll(it) }
    }

    private fun generateConfigFiles() {
        if (transformers.isEmpty()) {
            return
        }
        val oldTransformers = HashSet<String>()
        // Get old ConvexTransformers
        runCatching {
            filer.getResource(StandardLocation.CLASS_OUTPUT, "", CONFIG_FILES)
                ?.openReader(false)?.use { reader ->
                    reader.readText().split("\n").let { transformers ->
                        oldTransformers.addAll(transformers)
                    }
                }
        }
        if (oldTransformers.containsAll(transformers)) {
            return
        }
        // Log all found ConvexTransformers
        val allTransformers = transformers + oldTransformers
        info("Found ${allTransformers.size} ConvexTransformers as following :")
        allTransformers.forEachIndexed { index, transformer ->
            info("${index + 1}. $transformer")
        }
        // Write back to config file
        runCatching {
            filer.createResource(StandardLocation.CLASS_OUTPUT, "", CONFIG_FILES)
                ?.openWriter()?.use {
                    it.write(allTransformers.joinToString("\n"))
                }
        }.onFailure {
            error("Unable to create $CONFIG_FILES : $it")
        }
    }

    private fun info(content: String) {
        if (verbose) {
            messager.printMessage(Diagnostic.Kind.NOTE, "$content\n")
        }
    }

    private fun error(content: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, "$content\n")
    }

    companion object {
        private const val AUTO_TRANSFORMER = "org.paradisehell.convex.annotation.AutoTransformer"
        private const val CONVEX_TRANSFORMER =
            "org.paradisehell.convex.transformer.ConvexTransformer"
        private const val CONFIG_FILES = "META-INF/services/$CONVEX_TRANSFORMER"
    }
}
