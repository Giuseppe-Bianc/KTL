package  org.dersbian

import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

sealed class Expr {
    data class IntegerExpr(val value: Int) : Expr()
    data class RealExpr(val value: Double) : Expr()
    data class BinaryExpr(val left: Expr, val operator: Char, val right: Expr) : Expr()
    data class UnaryExpr(val operator: Char, val operand: Expr) : Expr()

    data class ParenthesizedExpr(val expression: Expr) : Expr()
}

class Parser(private inline val input: String) {
    private val tokens: List<Token> = Lexer(input).lex()
    private var index = 0

    fun parse(): Expr {
        return parseExpression()
    }

    private fun parseExpression(): Expr {
        return parseBinaryExpression()
    }

    private fun parseBinaryExpression(precedence: Int = 0): Expr {
        var leftExpr = parseUnaryExpression()

        while (index < tokens.size) {
            val token = tokens[index]

            if (token is Token.Operator && token.operator in "+-*/^" && getPrecedence(token.operator) >= precedence) {
                index++
                val rightExpr = parseBinaryExpression(getPrecedence(token.operator) + 1)
                leftExpr = Expr.BinaryExpr(leftExpr, token.operator, rightExpr)
            } else {
                break
            }
        }

        return leftExpr
    }

    private fun parseUnaryExpression(): Expr {
        val token = tokens[index]

        if (token is Token.Operator && token.operator in "+-") {
            index++
            val operand = parseUnaryExpression()
            return Expr.UnaryExpr(token.operator, operand)
        }

        return parsePrimaryExpression()
    }

    private fun parsePrimaryExpression(): Expr {
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
                if (token.operator == '(') {
                    index++
                    val expr = parseExpression()
                    require(tokens[index] is Token.Operator && (tokens[index] as Token.Operator).operator == ')') {
                        "Expected closing parenthesis ')'"
                    }
                    index++
                    Expr.ParenthesizedExpr(expr)
                } else {
                    error("Unexpected operator: ${token.operator}")
                }
            }

            else -> error("Unexpected token: $token")
        }
    }

    private fun getPrecedence(operator: Char): Int {
        return when (operator) {
            '+', '-' -> 1
            '*', '/' -> 2
            '^' -> 3
            else -> 0
        }
    }
}

fun printAST(expr: Expr, indent: String = "", isLast: Boolean = true) {
    val marker: String = if (isLast) "└──" else "├──"

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
