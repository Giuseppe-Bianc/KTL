package org.dersbian

sealed class Token(open val type: String, open val line: Int, open val column: Int) {
    sealed class Number(override val line: Int, override val column: Int) : Token("Number", line, column) {
        data class Integer(val value: Int, override val line: Int, override val column: Int) : Number(line, column) {
            override fun toString(): String = "Integer(value=$value, line=$line, column=$column)"
        }

        data class Real(val value: Double, override val line: Int, override val column: Int) : Number(line, column) {
            override fun toString(): String = "Real(value=$value, line=$line, column=$column)"
        }
    }

    data class EOF(override val line: Int, override val column: Int) : Token("EOF", line, column) {
        override fun toString(): String = "EOF(line=$line, column=$column)"
    }

    data class Operator(val operator: Char, override val line: Int, override val column: Int) :
        Token("Operator", line, column) {
        override fun toString(): String = "Operator(operator='$operator', line=$line, column=$column)"
    }

    data class Identifier(val name: String, override val line: Int, override val column: Int) :
        Token("Identifier", line, column) {
        override fun toString(): String = "Identifier(name='$name', line=$line, column=$column)"
    }

    data class KeyWord(val name: String, override val line: Int, override val column: Int) :
        Token("keyWord", line, column) {
        override fun toString(): String = "KeyWord(name='$name', line=$line, column=$column)"
    }

    data class STRING(val name: String, override val line: Int, override val column: Int) :
        Token("keyWord", line, column) {
        override fun toString(): String = "STRING(name='$name', line=$line, column=$column)"
    }

    override fun toString(): String = "Token(type='$type', line=$line, column=$column)"
}