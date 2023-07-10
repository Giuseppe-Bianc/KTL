package org.dersbian

import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

fun main() {
    val lexer = Lexer(code)
    val exNs :TimedValue<List<Token>> = measureTimedValue {
        lexer.lex()
    }
    println(exNs.customFormat())
}

fun TimedValue<List<Token>>.customFormat(): String = buildString {
    value.joinTo(this, separator = "\n")
    append('\n')
    append("execution time $duration ms o ${duration.inWholeNanoseconds} ns")
}