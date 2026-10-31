package fr.euphyllia.fidorial.gradle

import com.diffplug.spotless.FormatterFunc
import com.github.javaparser.JavaParser
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.EnclosedExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.LambdaExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.TypePatternExpr
import com.github.javaparser.ast.expr.UnaryExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.CatchClause
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.SwitchEntry
import com.github.javaparser.ast.type.UnknownType

class FinalizeVariables : FormatterFunc, java.io.Serializable {
    override fun apply(source: String): String {
        val result = JavaParser(
            ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.valueOf("JAVA_25")) // Gradle pulls in an older version shadowing the one we bundle
                .setAttributeComments(false)
                .setTabSize(1),
        ).parse(source)
        val unit = result.result.orElseThrow {
            IllegalStateException(result.problems.joinToString("\n"))
        }

        val lineStarts = lineStarts(source)
        val insertions = sortedSetOf<Int>()

        unit.findAll(Parameter::class.java).forEach { parameter ->
            val scope = parameterScope(parameter) ?: return@forEach
            val name = parameter.nameAsString
            if (!parameter.isFinal && name != "_" && !isReassigned(listOf(scope), name)) {
                insertions += insertionOffset(lineStarts, parameter, parameter.type)
            }
        }

        unit.findAll(VariableDeclarationExpr::class.java).forEach { declaration ->
            val scope = localScope(declaration)
            val names = declaration.variables.map { it.nameAsString }
            if (!declaration.isFinal && "_" !in names && names.none { isReassigned(scope, it) }) {
                insertions += insertionOffset(lineStarts, declaration, declaration.elementType)
            }
        }

        unit.findAll(TypePatternExpr::class.java).forEach { pattern ->
            val scope = pattern.findAncestor(SwitchEntry::class.java)
                .filter { entry -> entry.labels.any { it === pattern || it.isAncestorOf(pattern) } }
                .map<Node> { it }
                .or { pattern.findAncestor(BodyDeclaration::class.java) }
                .orElse(null) ?: return@forEach

            val name = pattern.nameAsString
            if (!pattern.isFinal && name != "_" && !isReassigned(listOf(scope), name)) {
                insertions += insertionOffset(lineStarts, pattern, pattern.type)
            }
        }

        val builder = StringBuilder(source)
        insertions.descendingSet().forEach { builder.insert(it, "final ") }
        return builder.toString()
    }

    // Null for record components, bodiless methods and implicitly typed lambda parameters.
    private fun parameterScope(parameter: Parameter): Node? =
        when (val owner = parameter.parentNode.orElse(null)) {
            is MethodDeclaration -> owner.body.orElse(null)
            is ConstructorDeclaration -> owner.body
            is LambdaExpr -> if (parameter.type is UnknownType) null else owner.body
            is CatchClause -> owner.body
            null -> null
            else -> null
        }

    private fun localScope(declaration: VariableDeclarationExpr): List<Node> {
        val parent = declaration.parentNode.orElse(null) ?: return emptyList()
        if (parent !is ExpressionStmt) {
            return listOf(parent)
        }

        return when (val container = parent.parentNode.orElse(null)) {
            is BlockStmt -> container.statements.dropWhile { it !== parent }
            is SwitchEntry -> listOfNotNull(container.parentNode.orElse(null))
            null -> emptyList()
            else -> listOf(container)
        }
    }

    private fun isReassigned(scope: List<Node>, name: String): Boolean =
        scope.isEmpty() ||
                scope.any { node ->
                    node.findFirst(AssignExpr::class.java) { targets(it.target, name) }.isPresent ||
                            node.findFirst(UnaryExpr::class.java) {
                                it.operator in MUTATING_OPERATORS && targets(it.expression, name)
                            }.isPresent
                }

    private fun targets(expression: Expression, name: String): Boolean =
        when (expression) {
            is NameExpr -> expression.nameAsString == name
            is EnclosedExpr -> targets(expression.inner, name)
            else -> false
        }

    private fun insertionOffset(lineStarts: IntArray, declaration: Node, type: Node): Int {
        val start = declaration.begin.get()
        val typeStart = type.begin.get()
        val position = if (typeStart.line > start.line) typeStart else start
        return lineStarts[position.line - 1] + position.column - 1
    }

    private fun lineStarts(source: String): IntArray =
        buildList {
            add(0)
            source.forEachIndexed { index, char ->
                if (char == '\n') {
                    add(index + 1)
                }
            }
        }.toIntArray()

    private companion object {
        private val MUTATING_OPERATORS = setOf(
            UnaryExpr.Operator.PREFIX_INCREMENT,
            UnaryExpr.Operator.PREFIX_DECREMENT,
            UnaryExpr.Operator.POSTFIX_INCREMENT,
            UnaryExpr.Operator.POSTFIX_DECREMENT,
        )
    }
}
