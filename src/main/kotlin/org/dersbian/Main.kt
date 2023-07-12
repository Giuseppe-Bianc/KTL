package org.dersbian

import kotlin.time.TimedValue
import kotlin.time.measureTime

fun main() {
    val input = """-2 + 3.14 * 4 - -1 + (-2 + 3.14) * (4 - -1)"""
    val lexer = Lexer(input)
    val lexDur = measureTime {
        lexer.lex()
    }
    println("lexer.lex() execution time $lexDur ms o ${lexDur.inWholeNanoseconds} ns")
    val astGenerator = ArithmeticASTGenerator(input)
    var ast:Expr

    val ASTGDur = measureTime {
        ast = astGenerator.generate()
    }
    println("astGenerator.generate() execution time $ASTGDur ms o ${ASTGDur.inWholeNanoseconds} ns")

    val ASTPDur = measureTime {
        printAST(ast)
    }
    println("printAST(ast) execution time $ASTPDur ms o ${ASTPDur.inWholeNanoseconds} ns")
}

fun TimedValue<List<Token>>.customFormat(): String = buildString {
    value.joinTo(this, separator = "\n")
    append('\n')
    append("lexer.lex() execution time $duration ms o ${duration.inWholeNanoseconds} ns")
}