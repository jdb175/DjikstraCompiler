package dijkstra.symbol;

import dijkstra.lexparse.DijkstraBaseVisitor;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.RuleNode;

import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.*;
import dijkstra.utility.DijkstraType;
import static dijkstra.utility.DijkstraType.*;

public class DijkstraTypeCheckVisitor extends DijkstraBaseVisitor<DijkstraType> {
	public ParseTreeProperty<Symbol> symbols = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> functions = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> arrays = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<DijkstraType> types = new ParseTreeProperty<DijkstraType>();
	
	
	public DijkstraTypeCheckVisitor(DijkstraResolutionVisitor oldVisitor) {
		super();
		symbols = oldVisitor.symbols;
		functions = oldVisitor.functions;
		arrays = oldVisitor.arrays;
		types = oldVisitor.types;
	}
	
	@Override
	public DijkstraType visitReturnStatement (@NotNull ReturnStatementContext ctx) {
		RuleNode p = ctx.getParent();
		while(p != null && p != ctx) {
			if(p instanceof FunctionDeclarationContext) {
				break;
			}
			p = (RuleNode) p.getParent();
		}
		if(p == null) {
			throw new DijkstraSymbolException("Return call outside of a function!");
		}
		Symbol func = functions.get(p);
		DijkstraType t = func.getType();
		if(t != types.get(ctx.expression())) {
			throw new DijkstraTypeException("Invalid return type, must be " + t);
		}
		ctx.expression().accept(this);
		return t;
	}
	
	@Override
	public DijkstraType visitMult(@NotNull MultContext ctx) {
		if(ctx.DIV() != null || ctx.MOD() != null) {
			Symbol first = symbols.get(ctx.expression(0));
			Symbol second = symbols.get(ctx.expression(1));
			if(first.getType() != INT || second.getType() != INT) {
				throw new DijkstraTypeException("Cannot use div or mod with non-integer operands");
			}
		}
		return null;
	}
}
