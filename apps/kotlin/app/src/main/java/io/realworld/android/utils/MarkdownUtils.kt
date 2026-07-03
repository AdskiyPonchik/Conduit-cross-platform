package io.realworld.android.utils

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object MarkdownUtils {
    private val parser: Parser = Parser.builder().build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder().build()

    fun markdownToHtml(markdown: String): String {
        val safeMarkdown = markdown.ifBlank { "" }
        return renderer.render(parser.parse(safeMarkdown))
    }

    fun wrapArticleHtml(htmlBody: String): String {
        return """
            <html>
              <head>
                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
                <style>
                    body { font-family: sans-serif; margin: 0; padding: 0; line-height: 1.5; }
                    img { max-width: 100%; height: auto; }
                    pre { white-space: pre-wrap; word-break: break-word; }
                </style>
              </head>
              <body>$htmlBody</body>
            </html>
        """.trimIndent()
    }
}

