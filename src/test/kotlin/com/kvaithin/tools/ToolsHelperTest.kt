package com.kvaithin.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ToolsHelperTest {
    private val toolsHelper = ToolsHelper()

    @Test
    fun `replaceAsciiValues should return null when passed null`() {
        val actual = toolsHelper.replaceAsciiValues(null)
        assertNull(actual)
    }

    @Test
    fun `replaceAsciiValues should replace ASCII values in a string`() {
        val input = "The ASCII value of 'A' is &#65; and '‡' is &#8225;."
        val expected = "The ASCII value of 'A' is A and '‡' is ‡."
        val actual = toolsHelper.replaceAsciiValues(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `replaceAsciiValues should return same object when object is not String, List or Map`() {
        val obj = 1234
        val actual = toolsHelper.replaceAsciiValues(obj)
        assertEquals(obj, actual)
    }

    @Test
    fun `replaceAsciiValues should replace ASCII values in a list`() {
        val input = listOf("The ASCII value of 'A' is &#65;.", "&#72;ello World")
        val expected = listOf("The ASCII value of 'A' is A.", "Hello World")
        val actual = toolsHelper.replaceAsciiValues(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `replaceAsciiValues should replace ASCII values in a map`() {
        val input = mapOf("key1" to "The ASCII value of 'A' is &#65;.", "key2" to "&#72;ello World")
        val expected = mapOf("key1" to "The ASCII value of 'A' is A.", "key2" to "Hello World")
        val actual = toolsHelper.replaceAsciiValues(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `test replaceAsciiValues on complex JSON String`() {
        val json = """
            {
              "name": "John &#8225;",
              "age": 30,
              "address": {
                "street": "123 Main St. &#8225;",
                "city": "Anytown &#8225;",
                "state": "CA &#8225;"
              },
              "phoneNumbers": [
                {
                  "type": "home &#8225;",
                  "number": "&#49;&#50;&#51;"
                },
                {
                  "type": "work &#8225;",
                  "number": "&#52;&#53;&#54;"
                }
              ]
            }
        """.trimIndent()

        val expected = """
            {
              "name": "John ‡",
              "age": 30,
              "address": {
                "street": "123 Main St. ‡",
                "city": "Anytown ‡",
                "state": "CA ‡"
              },
              "phoneNumbers": [
                {
                  "type": "home ‡",
                  "number": "123"
                },
                {
                  "type": "work ‡",
                  "number": "456"
                }
              ]
            }
        """.trimIndent()

        val actual = toolsHelper.replaceAsciiValues(json)

        assertEquals(expected, actual)
    }

    @Test
    fun `test replaceAsciiValues on complex JSON Object`() {
        val input = mapOf(
            "array" to listOf("hello", "&#72;&#101;&#108;&#108;&#111;", "world"),
            "nestedMap" to mapOf(
                "nestedArray" to listOf(
                    "&#72;&#101;&#108;&#108;&#111;",
                    mapOf("key" to "&#119;&#111;&#114;&#108;&#100;"),
                    "&#33;"
                ),
                "nestedString" to "&#72;&#101;&#108;&#108;&#111;"
            ),
            "string" to "&#72;&#101;&#108;&#108;&#111;"
        )

        val expectedOutput = mapOf(
            "array" to listOf("hello", "Hello", "world"),
            "nestedMap" to mapOf(
                "nestedArray" to listOf(
                    "Hello",
                    mapOf("key" to "world"),
                    "!"
                ),
                "nestedString" to "Hello"
            ),
            "string" to "Hello"
        )

        val actualOutput = toolsHelper.replaceAsciiValues(input)

        assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `test replacing a single tag in a string`() {
        val input = "<p>Hello, <strike>World</strike>!</p>"
        val expectedOutput = "<p>Hello, <s>World</s>!</p>"
        val actualOutput = toolsHelper.replaceTargetedTags(input, "strike", "s") as String
        assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `test replacing tags in a nested list`() {
        val input = listOf("Hello", listOf("Universe", mapOf("name" to "John <strike>abc</strike>", "age" to 30)))
        val expectedOutput = listOf("Hello", listOf("Universe", mapOf("name" to "John <s>abc</s>", "age" to 30)))
        val actualOutput = toolsHelper.replaceTargetedTags(input, "strike", "s") as List<*>
        assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `test replacing tags in a map`() {
        val input = mapOf("name" to "John", "age" to 30, "info" to mapOf("gender" to "Male <strike>abc</strike>", "city" to "New York"))
        val expectedOutput = mapOf("name" to "John", "age" to 30, "info" to mapOf("gender" to "Male <s>abc</s>", "city" to "New York"))
        val actualOutput = toolsHelper.replaceTargetedTags(input, "strike", "s") as Map<*, *>
        assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `test replacing tags in a mixed-type nested structure`() {
        val input = listOf(
            mapOf("name" to "John", "age" to 30),
            mapOf("name" to "Mary", "age" to 25, "address" to mapOf("city" to "Los Angeles <h1>LA</h1>", "state" to "California")),
            "Hello, <b>World</b>!"
        )
        val expectedOutput = listOf(
            mapOf("name" to "John", "age" to 30),
            mapOf("name" to "Mary", "age" to 25, "address" to mapOf("city" to "Los Angeles <h2>LA</h2>", "state" to "California")),
            "Hello, <b>World</b>!"
        )
        val actualOutput = toolsHelper.replaceTargetedTags(input, "h1", "h2") as List<*>
        assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `test replacing tags in a list of maps`() {
        val input = listOf(
            mapOf("name" to "John", "age" to 30),
            mapOf("name" to "Mary <strike>M</strike>", "age" to 25, "address" to mapOf("city" to "Los Angeles", "state" to "California <strike>C</strike>"))
        )
        val expectedOutput = listOf(
            mapOf("name" to "John", "age" to 30),
            mapOf("name" to "Mary <s>M</s>", "age" to 25, "address" to mapOf("city" to "Los Angeles", "state" to "California <s>C</s>"))
        )
        val actualOutput = toolsHelper.replaceTargetedTags(input, "strike", "s") as List<*>
        assertEquals(expectedOutput, actualOutput)
    }
}
