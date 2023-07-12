package org.dersbian

import org.apache.commons.lang3.StringUtils
import java.util.*

class Lexer(private inline val input: String) {
    private val keywords = hashSetOf(
        "val", "var", "fun", "if",
        "else", "when", "is", "for",
        "while", "return", "null", "true",
        "false",  "this", "super", "class",
        "interface", "object", "package", "import",
        "function"
    )
    private val tokens = mutableListOf<Token>()
    private var index = 0
    private var line = 1
    private var column = 1
    private var inputLen = input.length

    fun lex(): List<Token> {
        while (index < inputLen) {
            when (val char = input[index]) {
                '+','-','*','(',')','=',':','{','}',',','<','>','.','[',']',';' -> {
                    tokens.add(Token.Operator(char, line, column))
                    index++
                    column++
                }
                '/' ->handleCommentOrSlash(char)
                '"' -> handleStrings()
                in '0'..'9' -> handleNumber()
                in 'a'..'z', in 'A'..'Z' -> handleIdentifierOrKeyword()
                '\r'->{
                    if (index < inputLen && input[index+1] == '\n'){
                        index+=2
                        line+=2
                        column = 1
                    }
                }
                '\n' -> {
                    index++
                    line++
                    column = 1
                }
                else -> {
                    require(char.isWhitespace()) {  "Carattere non valido: $char (linea: $line, colonna: $column)" }
                    index++
                    column++
                }
            }
        }
        tokens.add(Token.EOF(line, column))
        return tokens
    }

    private fun handleIdentifierOrKeyword() {
        val nameBuilder = StringBuilder()
        while (index < inputLen && isValidChar(input[index])) {
            nameBuilder.append(input[index])
            index++
            column++
        }
        val name = nameBuilder.toString()
        tokens.add(
            if (keywords.contains(name)) {
                Token.KeyWord(name, line, column - name.length)
            } else {
                Token.Identifier(name, line, column - name.length)
            }
        )
    }

    private fun handleStrings() {
        val stringBuilder = StringBuilder()
        stringBuilder.append(input[index])
        index++ // Move past the opening double quote
        column++
        while (index < inputLen && input[index] != '"') {
            when (input[index]) {
                '\n' -> {
                    line++
                    column = 1
                }
                else -> {
                    stringBuilder.append(input[index])
                    column++
                }
            }
            index++
        }
        stringBuilder.append(input[index])
        index++
        column++
        tokens.add(Token.STRING(stringBuilder.toString(), line, column - stringBuilder.length))
    }

    private fun handleCommentOrSlash(char: Char) {
        if (index + 1 < inputLen && input[index + 1] == '/') {
            // Ignore the rest of the line as a comment
            while (index < inputLen && input[index] != '\n') {
                index++
            }
            line++
            column = 1
        } else {
            tokens.add(Token.Operator(char, line, column))
            index++
            column++
        }
    }
    private fun handleNumber() {
        val start = index
        val number: Number
        incrementIndexAndColumn()
        if (index < inputLen && input[index] == '.') {
            number = handleFloat(start)
            tokens.add(Token.Number.Real(number, line, column - number.toString().length))
        } else {
            number = input.substring(start, index).toInt()
            tokens.add(Token.Number.Integer(number, line, column - number.toString().length))
        }
    }

    private fun handleFloat(start: Int): Double {
        index++
        column++
        incrementIndexAndColumn()

        if (index < inputLen && ((input[index] == 'e' || input[index] == 'E') && (input[index + 1] == '+' || input[index + 1] == '-'))) {
            index += 2
            column += 2
            incrementIndexAndColumn()
        }
        return input.substring(start, index).toDouble()
    }

    private fun incrementIndexAndColumn() {
        while (index < inputLen && input[index] in '0'..'9') {
            index++
            column++
        }
    }
    private fun isValidChar(c: Char): Boolean =  c.isLetterOrDigit() || c == '_'
    private fun Char.isWS(): Boolean = StringUtils.isWhitespace(this.toString())
}
