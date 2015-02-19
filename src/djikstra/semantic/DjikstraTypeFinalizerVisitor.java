package djikstra.semantic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.RuleNode;

import dijkstra.lexparse.DijkstraBaseVisitor;
import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.*;
import static dijkstra.utility.DijkstraType.*;
import dijkstra.symbol.DijkstraSymbolException;
import dijkstra.symbol.Symbol;
import dijkstra.utility.DijkstraType;

//This class sets all NUM symbols to INT, and throws an error if there are any undefined symbols
public class DjikstraTypeFinalizerVisitor extends DijkstraBaseVisitor<DijkstraType> {
	public ParseTreeProperty<Symbol> symbols = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> functions = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> arrays = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<DijkstraType> types = new ParseTreeProperty<DijkstraType>();
	
	public DjikstraTypeFinalizerVisitor(DjikstraTypeResolutionVisitor oldVisitor) {
		super();
		symbols = oldVisitor.symbols;
		functions = oldVisitor.functions;
		arrays = oldVisitor.arrays;
		types = oldVisitor.types;
	}
	
	@Override
	public DijkstraType visitChildren(@NotNull RuleNode arg0) {
		Symbol cur = symbols.get(arg0);
		if(cur != null) {
			if(cur.getType() == NUM) {
				cur.updateType(INT);
			} else if (cur.getType() == UNDEFINED) {
				throw new DijkstraSymbolException("Unable to infer type of identifier " + cur.getId());
			}
		}
		DijkstraType curType = types.get(arg0);
		if(curType == NUM) {
			types.put(arg0, INT);
		}
		return super.visitChildren(arg0);
	}
}
