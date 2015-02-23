package dijkstra.utility;

import static org.junit.Assert.assertTrue;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;

import dijkstra.lexparse.DijkstraParser;
import dijkstra.symbol.DijkstraSymbolVisitor;
import dijkstra.symbol.SymbolTableManager;
import djikstra.semantic.DijkstraTypeCheckVisitor;
import djikstra.semantic.DjikstraTypeFinalizerVisitor;
import djikstra.semantic.DjikstraTypeResolutionVisitor;

public class TypeCheckRunner {
	
	private static ParserRuleContext doParse(String programText)
	{
		DijkstraParser parser = DijkstraFactory.makeParser(new ANTLRInputStream(programText));
		ParserRuleContext tree = parser.dijkstraText();
		return tree;
	}
	
	public static void Check(String programText) {
		SymbolTableManager.getInstance().reset();
		ParserRuleContext tree = doParse(programText);
		DijkstraSymbolVisitor visitor = new DijkstraSymbolVisitor();
		tree.accept(visitor);
		DjikstraTypeResolutionVisitor resolver = new DjikstraTypeResolutionVisitor(visitor);
		while(!resolver.isComplete()) {
			tree.accept(resolver);
		}
		DjikstraTypeFinalizerVisitor finalizer = new DjikstraTypeFinalizerVisitor(resolver);
		tree.accept(finalizer);
		DijkstraTypeCheckVisitor checker = new DijkstraTypeCheckVisitor(resolver);
		tree.accept(checker);
	}
}
