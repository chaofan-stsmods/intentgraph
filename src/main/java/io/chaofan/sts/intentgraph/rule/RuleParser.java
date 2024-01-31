package io.chaofan.sts.intentgraph.rule;

import io.chaofan.sts.intentgraph.parser.RuleLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;

public class RuleParser {
    private static final HashMap<String, IRule.Operator> operatorMap = new HashMap<>();

    static {
        operatorMap.put(">", IRule.Operator.GT);
        operatorMap.put("<", IRule.Operator.LT);
        operatorMap.put("==", IRule.Operator.EQ);
        operatorMap.put(">=", IRule.Operator.GE);
        operatorMap.put("<=", IRule.Operator.LE);
        operatorMap.put("!=", IRule.Operator.NE);
        operatorMap.put("&&", IRule.Operator.AND);
        operatorMap.put("||", IRule.Operator.OR);
    }

    public static IRule parse(String expression, IRuleContext ruleContext) {
        if (expression.matches("\\d+")) {
            expression = "ascension >= " + expression;
        }

        RuleLexer lexer = new RuleLexer(CharStreams.fromString(expression));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        io.chaofan.sts.intentgraph.parser.RuleParser parser = new io.chaofan.sts.intentgraph.parser.RuleParser(tokenStream);
        ParseTree tree = parser.prog().expr();
        return expr(tree, ruleContext);
    }

    private static IRule expr(ParseTree tree, IRuleContext ruleContext) {
        if (tree.getChildCount() == 1) {
            TerminalNode node = (TerminalNode) tree.getChild(0);
            Token token = node.getSymbol();
            if (token.getType() == RuleLexer.VAR) {
                return new VariableOperand(token.getText(), ruleContext);
            } else if (token.getType() == RuleLexer.INT) {
                return new ValueOperand(Integer.parseInt(token.getText()));
            } else if (token.getType() == RuleLexer.BOOL) {
                return new ValueOperand(Boolean.parseBoolean(token.getText()));
            }
        } else if (tree.getChildCount() == 2) {
            TerminalNode node = (TerminalNode) tree.getChild(0);
            if (node.getSymbol().getText().equals("!")) {
                IRule expr = expr(tree.getChild(1), ruleContext);
                if (expr != null) {
                    return new OneOperandRule(IRule.Operator.NOT, expr);
                }
            }
        } else if (tree.getChildCount() == 3) {
            ParseTree first = tree.getChild(0);
            ParseTree second = tree.getChild(1);
            if (first instanceof TerminalNode) {
                TerminalNode node = (TerminalNode) first;
                if (node.getSymbol().getText().equals("(")) {
                    return expr(tree.getChild(1), ruleContext);
                }
            } else if (second instanceof TerminalNode) {
                TerminalNode node = (TerminalNode) second;
                IRule.Operator operator = operatorMap.get(node.getSymbol().getText());
                if (operator != null) {
                    IRule expr1 = expr(first, ruleContext);
                    IRule expr2 = expr(tree.getChild(2), ruleContext);
                    if (expr1 != null && expr2 != null) {
                        return new TwoOperandRule(expr1, operator, expr2);
                    }
                }
            }
        }
        return null;
    }
}
