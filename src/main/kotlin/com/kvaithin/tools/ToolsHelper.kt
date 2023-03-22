package com.kvaithin.tools

/**
 * Utility methods to aid in replacing values in complex json objects.
 */
class ToolsHelper {
    /**
     * Recursively check all String values in a json object and convert all UTF-8 ASCII values.
     * Example: Convert &#8225; to ‡
     */
    fun replaceAsciiValues(obj: Any?): Any? = when (obj) {
        is String -> {
            val regex = Regex("&#(\\d+);")
            regex.replace(obj) { matchResult ->
                matchResult.groupValues[1].toInt().toChar().toString()
            }
        }
        is List<*> -> obj.map { replaceAsciiValues(it) }
        is Map<*, *> -> obj.mapValues { replaceAsciiValues(it.value) }
        else -> obj
    }

    /**
     * Recursively check all String values in a json object and convert all tag values to the replacement tag.
     * Example: Convert <strike to <s> and </strike> to </s>
     */
    fun replaceTargetedTags(obj: Any?, originalTag: String, replacementTag: String): Any? = when (obj) {
        is String -> {
            val startTagRegex = Regex("<$originalTag>")
            val endTagRegex = Regex("</$originalTag>")
            endTagRegex.replace(startTagRegex.replace(obj, "<$replacementTag>")) { "</$replacementTag>" }
        }
        is List<*> -> obj.map { replaceTargetedTags(it, originalTag, replacementTag) }
        is Map<*, *> -> obj.mapValues { replaceTargetedTags(it.value, originalTag, replacementTag) }
        else -> obj
    }
}
