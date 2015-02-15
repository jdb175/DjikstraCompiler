package dijkstra.symbol;

import org.antlr.v4.runtime.misc.NotNull;

import dijkstra.lexparse.DijkstraBaseVisitor;
import dijkstra.lexparse.DijkstraParser;
import static dijkstra.utility.DijkstraType.*;
import dijkstra.utility.DijkstraType;

//This class sets all NUM symbols to INT, and throws an error if there are any undefined symbols
public class DijkstraSymbolFinalizer extends DijkstraBaseVisitor<DijkstraType> {
	public DijkstraType visitParameter(@NotNull DijkstraParser.ParameterContext ctx) {
		return null;
	}
}
