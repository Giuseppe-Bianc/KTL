package  org.dersbian

import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
sealed class Expr {
    data class IntegerExpr(val value: Int) : Expr() {
        override fun toString(): String = "IntegerExpr(value=$value)"
    }
    data class RealExpr(val value: Double) : Expr() {
        override fun toString(): String = "RealExpr(value=$value)"
    }
    data class BinaryExpr(val left: Expr, val operator: Char, val right: Expr) : Expr() {

    }
    data class UnaryExpr(val operator: Char, val operand: Expr) : Expr()
    data class ParenthesizedExpr(val expression: Expr) : Expr()
}

class ArithmeticASTGenerator(private inline val input: String) {
    private val tokens: List<Token> = Lexer(input).lex()
    private var index = 0

    fun generate(): Expr {
        return parseExpression()
    }

    private fun parseExpression(): Expr {
        var expr = parseTerm()

        while (index < tokens.size) {
            val token = tokens[index] as? Token.Operator
            if (token == null || (token.operator != '+' && token.operator != '-'))
                break

            index++
            val right = parseTerm()
            expr = Expr.BinaryExpr(expr, token.operator, right)
        }

        return expr
    }

    private fun parseTerm(): Expr {
        var expr = parseFactor()

        while (index < tokens.size) {
            val token = tokens[index] as? Token.Operator
            if (token == null || (token.operator != '*' && token.operator != '/'))
                break

            index++
            val right = parseFactor()
            expr = Expr.BinaryExpr(expr, token.operator, right)
        }

        return expr
    }

    private fun parseFactor(): Expr {
        val token = tokens[index]

        return when (token) {
            is Token.Number.Integer -> {
                index++
                Expr.IntegerExpr(token.value)
            }
            is Token.Number.Real -> {
                index++
                Expr.RealExpr(token.value)
            }
            is Token.Operator -> {
                if (token.operator == '+' || token.operator == '-') {
                    index++
                    val operand = parseFactor()
                    Expr.UnaryExpr(token.operator, operand)
                } else if (token.operator == '(') {
                    index++
                    val expr = parseExpression()
                    if (tokens[index] is Token.Operator && (tokens[index] as Token.Operator).operator == ')') {
                        index++
                        Expr.ParenthesizedExpr(expr)
                    } else {
                        throw IllegalStateException("Mismatched parentheses")
                    }
                } else {
                    throw IllegalStateException("Unexpected operator token: $token")
                }
            }
            else -> throw IllegalStateException("Unexpected token: $token")
        }
    }
}
fun printAST(expr: Expr, indent: String = "", isLast: Boolean = true) {
    val marker:String = if (isLast) "└──" else "├──"

    val outputStream = System.out   // You can replace this with any other OutputStream
    val writer = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)

    writer.write(indent)
    writer.write(marker)
    writer.flush()

    val newIndent = "$indent${if (isLast) "   " else "│  "}"
    when (expr) {
        is Expr.IntegerExpr -> println(expr)
        is Expr.RealExpr -> println(expr)
        is Expr.BinaryExpr -> {
            println("BinaryExpr(operator='${expr.operator}')")
            printAST(expr.left, newIndent, false)
            printAST(expr.right, newIndent, true)
        }
        is Expr.UnaryExpr -> {
            println("UnaryExpr(operator='${expr.operator}')")
            printAST(expr.operand, newIndent, true)
        }
        is Expr.ParenthesizedExpr -> {
            println("ParenthesizedExpr")
            printAST(expr.expression, newIndent, true)
        }
    }
}
