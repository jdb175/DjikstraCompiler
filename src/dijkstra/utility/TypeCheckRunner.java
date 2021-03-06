package dijkstra.utility;

import static org.junit.Assert.assertTrue;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;

import dijkstra.lexparse.DijkstraParser;
import dijkstra.semantic.DijkstraTypeCheckVisitor;
import dijkstra.semantic.DjikstraTypeFinalizerVisitor;
import dijkstra.semantic.DjikstraTypeResolutionVisitor;
import dijkstra.symbol.DijkstraSymbolVisitor;
import dijkstra.symbol.SymbolTableManager;

public class TypeCheckRunner {
	
	private static ParserRuleContext doParse(String programText)
	{
		DijkstraParser parser = DijkstraFactory.makeParser(new ANTLRInputStream(programText));
		ParserRuleContext tree = parser.dijkstraText();
		return tree;
	}
	
	public static void check(String programText) {
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
		DijkstraTypeCheckVisitor checker = new DijkstraTypeCheckVisitor(finalizer);
		tree.accept(checker);
	}
}
