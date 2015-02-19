package djikstra.semantic;

import dijkstra.lexparse.DijkstraBaseVisitor;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.RuleNode;

import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.*;
import dijkstra.symbol.DijkstraSymbolException;
import dijkstra.symbol.Symbol;
import dijkstra.utility.DijkstraType;
import static dijkstra.utility.DijkstraType.*;

/**
 * Class for all type checking missed in previous passes. That means cases where operands being checked 
 * are normally compatible (float & int), which aren't visited during type resolution (return), or
 * require knowledge of the final type (equals). It would be possible to resolve these inline during
 * resolution but it would be excessively complicating to that visitor.
 * @author Jason Whitehouse
 *
 */
public class DijkstraTypeCheckVisitor extends DijkstraBaseVisitor<DijkstraType> {
	public ParseTreeProperty<Symbol> symbols = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> functions = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> arrays = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<DijkstraType> types = new ParseTreeProperty<DijkstraType>();
	
	
	public DijkstraTypeCheckVisitor(DjikstraTypeResolutionVisitor oldVisitor) {
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
			throw new DijkstraSemanticException("Invalid return type, must be " + t);
		}
		ctx.expression().accept(this);
		return t;
	}
	
	@Override
	public DijkstraType visitMult(@NotNull MultContext ctx) {
		if(ctx.DIV() != null || ctx.MOD() != null) {
			DijkstraType first = types.get(ctx.expression(0));
			DijkstraType second = types.get(ctx.expression(1));
			if(first != INT || second != INT) {
				throw new DijkstraSemanticException("Cannot use div or mod with non-integer operands");
			}
		}
		return null;
	}
	
	@Override
	public DijkstraType visitEqual(@NotNull EqualContext ctx) {
		DijkstraType first = types.get(ctx.expression(0));
		DijkstraType second = types.get(ctx.expression(1));
		if(first != second) {
			throw new DijkstraSemanticException("'=' and '~=' can only be used with operands of the same type");
		}
		return null;
	}
	
	@Override
	public DijkstraType visitArrayAccessor(@NotNull DijkstraParser.ArrayAccessorContext ctx) {
		DijkstraType aType = types.get(ctx.expression());
		if(aType != INT) {
			throw new DijkstraSemanticException("Arrays can only be accessed with integer types!");
		}
		return null;
	}
}
