package io.chaofan.sts.intentgraph;

import io.chaofan.sts.intentgraph.parser.RuleLexer;
import io.chaofan.sts.intentgraph.parser.RuleParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.*;

public class ParserTest {
    public static void main(String[] args) {
        RuleLexer lexer = new RuleLexer(CharStreams.fromString("m.a < (5 && a) > 10 >"));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        RuleParser parser = new RuleParser(tokenStream);
        ParseTree tree = parser.prog().expr();
        TerminalNode node = (TerminalNode) tree.getChild(1);
        node.getSymbol().getText();
        System.out.println(tree.getText());
    }
}
