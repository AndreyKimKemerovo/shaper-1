/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper

fun registerHelpersDefaultFeature(handlebars: Handlebars) {

    handlebars.registerHelper("packagePathFeature", Helper<String> { _, options ->
        (
                "com.icerockdev.feature." + options.get<String>("featureName")
                    .snakeToLowerCamelCase()
                    .decapitalize()
                )
            .replace('.', '/')
    })

    handlebars.registerHelper("package", Helper<String> { _, options ->
        "com.icerockdev.feature." + options.get<String>("featureName").snakeToLowerCamelCase()
    })

    handlebars.registerHelper("cts", Helper<String> { context, _ ->
        context.camelToSnakeCase()
    })

    handlebars.registerHelper("stl", Helper<String> { context, _ ->
        context.snakeToLowerCamelCase()
            .decapitalize()
    })

    handlebars.registerHelper("stu", Helper<String> { context, _ ->
        context.snakeToUpperCamelCase()
    })

    handlebars.registerHelper("modelColumn", Helper<LinkedHashMap<String, Any>> { context, options ->
        val type = context["type"]
        val title = context["name"].toString()
        val length = context["length"]
        val (nullable, typeNullable) = if(context["nullable"] != null && context["nullable"] == true) {
            Pair(".nullable()", "?")
        } else {
            Pair("", "")
        }
        "val ${title}: " + when (type) {
            "string" -> "Column<String${typeNullable}> = varchar(\"${title.camelToSnakeCase()}\", ${length?:100})${nullable}"
            "integer" -> "Column<Int${typeNullable}> = integer(\"${title.camelToSnakeCase()}\")${nullable}"
            "boolean" -> "Column<Boolean${typeNullable}> = bool(\"${title.camelToSnakeCase()}\")${nullable}"
            else -> null
        }
    })

    handlebars.registerHelper("columnByFieldName", Helper<LinkedHashMap<String, Any>> { context, options ->
        val title = context["name"].toString()
        "\"${title}\" -> $title"
    })

    handlebars.registerHelper("toFeatureDto", Helper<LinkedHashMap<String, Any>> { context, options ->
        val title = context["name"].toString()
        val featureName = options.get<String>("featureName").capitalize()
        ",\n\t$title = this[${featureName}.${title}]"
    })

    handlebars.registerHelper("createDtoFields", Helper<LinkedHashMap<String, Any>> { context, options ->
        val title = context["name"].toString()
        ",\n\t\t$title = this.${title}"
    })

    handlebars.registerHelper("createFilterDtoFields", Helper<ArrayList<LinkedHashMap<String, Any>>> { context, options ->
        var result = ""
        context.forEach {

            val type = it["type"]
            val requestType = when (type) {
                "string" -> "String"
                "integer" -> "Int"
                "boolean" -> "Boolean"
                else -> null
            }

            val title = it["name"].toString().snakeToLowerCamelCase()
            result += when (type) {
                "string" -> "\n\t\t\t\t\t\t$title = call.parameters[\"${title}\"]"
                "integer" -> "\n\t\t\t\t\t\t$title = call.parameters[\"${title}\"]?.toInt()"
                "boolean" -> "\n\t\t\t\t\t\t$title = call.parameters[\"${title}\"] != null"
                else -> null
            }

            if (title != context.last()["name"]) {
                result += ","
            }

        }
        result
    })

    handlebars.registerHelper("applyData", Helper<LinkedHashMap<String, Any>> { context, options ->
        val title = context["name"].toString()
        val featureName = options.get<String>("featureName")
        "it[${title}] = ${featureName}Dto.${title}"
    })

    handlebars.registerHelper("columnByFieldName", Helper<LinkedHashMap<String, Any>> { context, options ->
        val title = context["name"].toString()
        "\"${title}\" -> $title"
    })

    handlebars.registerHelper("dtoFields", Helper<LinkedHashMap<String, Any>> { context, options ->
        val title = context["name"].toString()
        val type = context["type"]
        val typeNullable = if(context["nullable"] != null && context["nullable"] == true) {
            "?"
        } else {
            ""
        }
        ",\n    val ${title}: " + when (type) {
            "string" -> "String${typeNullable}"
            "integer" -> "Int${typeNullable}"
            "boolean" -> "Boolean${typeNullable}"
            else -> null
        } + if(context["nullable"] != null && context["nullable"] == true) {
            " = null"
        } else {
            ""
        }
    })

    handlebars.registerHelper("filterDtoFields", Helper<ArrayList<LinkedHashMap<String, Any>>> { context, options ->
        var result = "\n"
        context.forEach {
            val type = it["type"]
            val title = it["name"].toString()
            val nullable = it["nullable"]
            val requestType = when (type) {
                "string" -> "String"
                "integer" -> "Int"
                "boolean" -> "Boolean"
                else -> null
            }
            result += "\tval $title: $requestType? = null"
            if (title != context.last()["name"]) {
                result += ","
            }
            if (title != context.last()["name"]) {
                result += "\n"
            }
        }
        result
    })

    handlebars.registerHelper("modelMigrationColumns", Helper<LinkedHashMap<String, Any>> { context, _ ->
        val type = context["type"]
        val title = context["name"].toString()
        val length = context["length"]
        val nullable = if(context["nullable"] != null && context["nullable"] == true) {
            "true"
        } else {
            "false"
        }
        val migrationType = when (type) {
            "string" -> "varchar"
            "integer" -> "integer"
            "boolean" -> "boolean"
            else -> null
        }
        val properties = when (type) {
            "string" -> mutableListOf(
                "\"${title.camelToSnakeCase()}\"",
                "size = $length"
            )
            "integer" -> mutableListOf(
                "\"${title.camelToSnakeCase()}\""
            )
            "boolean" -> mutableListOf(
                "\"${title.camelToSnakeCase()}\""
            )
            else -> mutableListOf()
        }
        properties.add("nullable = $nullable")
        "$type(${properties.joinToString(", ")})"
    })

    handlebars.registerHelper("requestFields", Helper<ArrayList<LinkedHashMap<String, Any>>> { context, _ ->
        var result = ""
        context.forEach {
            val type = it["type"]
            val title = it["name"].toString()
            val length = it["length"] //ToDo валидация min max
            val nullable = it["nullable"]
            val requestType = when (type) {
                "string" -> "String"
                "integer" -> "Int"
                "boolean" -> "Boolean"
                else -> null
            }
            if (nullable != true) {
                result += "\n\t@field:NotNull(message = \"${title.capitalize()} is required\")"
            }
            result += "\n\tval $title: $requestType"
            if(nullable == true) {
                result += "?"
            }
            if (title != context.last()["name"]) {
                result += ",\n"
            }

        }
        result
    })

    handlebars.registerHelper("responseFields", Helper<ArrayList<LinkedHashMap<String, Any>>> { context, options ->
        var result = "\n"
        context.forEach {
            val type = it["type"]
            val title = it["name"].toString()
            val nullable = it["nullable"]
            val requestType = when (type) {
                "string" -> "String"
                "integer" -> "Int"
                "boolean" -> "Boolean"
                else -> null
            }
            result += "\tval $title: $requestType"
            if(nullable == true) {
                result += "?"
            }
            result += " = ${options.get<String>("featureName")}Dto.$title"
            if (title != context.last()["name"]) {
                result += "\n"
            }

        }
        result
    })
}

private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
private val snakeRegex = "_[a-zA-Z]".toRegex()

// String extensions
fun String.camelToSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.toLowerCase()
}

fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_","")
            .toUpperCase()
    }
}

fun String.snakeToUpperCamelCase(): String {
    return this.snakeToLowerCamelCase().capitalize()
}
