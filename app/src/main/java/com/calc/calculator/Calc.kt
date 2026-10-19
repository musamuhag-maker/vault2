package com.calc.calculator

import java.math.BigDecimal
import java.math.MathContext

object Calc {
    fun eval(input: String): Double {
        val s = input.replace('×', '*').replace('÷', '/').replace('−', '-')
        val p = Parser(s)
        val v = p.expr()
        if (p.i < s.length) throw IllegalArgumentException()
        return v
    }

    fun format(v: Double): String {
        if (v.isNaN() || v.isInfinite()) throw ArithmeticException()
        return BigDecimal(v).round(MathContext(12)).stripTrailingZeros().toPlainString()
    }

    private class Parser(val s: String) {
        var i = 0
        fun peek(): Char = if (i < s.length) s[i] else '\u0000'
        fun expr(): Double {
            var v = term()
            while (peek() == '+' || peek() == '-') {
                val o = s[i++]; val t = term()
                v = if (o == '+') v + t else v - t
            }
            return v
        }
        fun term(): Double {
            var v = unary()
            while (peek() == '*' || peek() == '/') {
                val o = s[i++]; val t = unary()
                v = if (o == '*') v * t else v / t
            }
            return v
        }
        fun unary(): Double {
            if (peek() == '-') { i++; return -unary() }
            if (peek() == '+') { i++; return unary() }
            var v = num()
            while (peek() == '%') { i++; v /= 100 }
            return v
        }
        fun num(): Double {
            val st = i
            while (peek().isDigit() || peek() == '.') i++
            if (st == i) throw IllegalArgumentException()
            return s.substring(st, i).toDouble()
        }
    }
}
