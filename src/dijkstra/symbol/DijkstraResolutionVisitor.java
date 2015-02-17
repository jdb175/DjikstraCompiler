package dijkstra.symbol;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import dijkstra.lexparse.DijkstraBaseVisitor;
import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.*;
import dijkstra.utility.DijkstraType;
import static dijkstra.utility.DijkstraType.*;

public class DijkstraResolutionVisitor extends DijkstraBaseVisitor<DijkstraType> {
	public ParseTreeProperty<Symbol> symbols = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> functions = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> arrays = new ParseTreeProperty<Symbol>();
	
	private boolean changed = true;
	
	public DijkstraResolutionVisitor(DijkstraSymbolVisitor oldVisitor) {
		super();
		symbols = oldVisitor.symbols;
		functions = oldVisitor.functions;
		arrays = oldVisitor.arrays;
	}
	
	@Override
	public DijkstraType visitDijkstraText(@NotNull DijkstraParser.DijkstraTextContext ctx) {
		changed = false;
		visitChildren(ctx);
		return null;
	}
	
	@Override
	public DijkstraType visitAssignStatement(@NotNull DijkstraParser.AssignStatementContext ctx)
	{
		//iterate over var list and expressionList
		VarListContext varList = ctx.varList();
		ExpressionListContext exprList = ctx.expressionList();
		while(varList != null) {
			//Get name from var
			VarContext var = varList.var();
			//Get type from expression
			DijkstraType t = exprList.expression().accept(this);
			//Now get id
			String id;
			if(var.ID() != null) {
				//create the symbol if it is not an accessor
				Symbol symbol = symbols.get(var);
				updateType(symbol, t);
			}
			varList = varList.varList();
			exprList = exprList.expressionList();
		}
		
		return null;
	}

	/* Complex Expression Types */
	@Override
	public DijkstraType visitEqual(@NotNull DijkstraParser.EqualContext ctx) {
		//Figure out types
		DijkstraType t1 = ctx.expression(0).accept(this);
		DijkstraType t2 = ctx.expression(1).accept(this);
		
		Symbol first = symbols.get(ctx.expression(0));
		Symbol second = symbols.get(ctx.expression(1));
		if(t2 != UNDEFINED && first != null) {
			updateType(first, t2);
		} 
		if(t1 != UNDEFINED && second != null) {
			updateType(second, t1);
		}
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitOr(@NotNull DijkstraParser.OrContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		Symbol first = symbols.get(ctx.expression(0));
		if(first != null) {
			updateType(first, BOOLEAN);
		}
		Symbol second = symbols.get(ctx.expression(1));
		if(second != null) {
			updateType(second, BOOLEAN);
		}
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitAnd(@NotNull DijkstraParser.AndContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		Symbol first = symbols.get(ctx.expression(0));
		if(first != null) {
			updateType(first, BOOLEAN);
		}
		Symbol second = symbols.get(ctx.expression(1));
		if(second != null) {
			updateType(second, BOOLEAN);
		}
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitMult(@NotNull DijkstraParser.MultContext ctx) {
		Symbol first = symbols.get(ctx.expression(0));
		Symbol second = symbols.get(ctx.expression(1));

		if(ctx.SLASH() != null) {
			if(first != null) {
				updateType(first, FLOAT);
			}
			if(second != null) {
				updateType(second, FLOAT);
			}
			return FLOAT;
		} else if(ctx.DIV() != null || ctx.MOD() != null) {
			if(first != null) {
				updateType(first, INT);
			}
			if(second != null) {
				updateType(second, INT);
			}
			return INT;
		} else {
			DijkstraType t1 = ctx.expression(0).accept(this);
			DijkstraType t2 = ctx.expression(1).accept(this);
			if(first != null) {
				updateType(first, NUM);
			}
			if(second != null) {
				updateType(second, NUM);
			}
			if(t1 == FLOAT || t2 == FLOAT) {
				return FLOAT;
			} else {
				return NUM;
			}
		}
	}
	
	@Override
	public DijkstraType visitGuard(@NotNull DijkstraParser.GuardContext ctx) {
		Symbol first = symbols.get(ctx.expression());
		if(first != null) {
			updateType(first, BOOLEAN);

		}
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitAdd(@NotNull DijkstraParser.AddContext ctx) {
		DijkstraType t1 = ctx.expression(0).accept(this);
		DijkstraType t2 = ctx.expression(1).accept(this);
		Symbol first = symbols.get(ctx.expression(0));
		Symbol second = symbols.get(ctx.expression(1));
		DijkstraType t = NUM;
		if(t1 == FLOAT || t2 == FLOAT) {
			t = FLOAT;
		} else if (t1 == INT && t2 == INT) {
			t = INT;
		}
		if(first != null) {
			updateType(first, t);
		}
		if(second != null) {
			updateType(second, t);
		}
		
		return t;
	}
	
	@Override
	public DijkstraType visitRelational(@NotNull DijkstraParser.RelationalContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		Symbol first = symbols.get(ctx.expression(0));
		if(first != null) {
			updateType(first, NUM);
		}
		Symbol second = symbols.get(ctx.expression(1));
		if(second != null) {
			updateType(second, NUM);
		}
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitUnary(@NotNull DijkstraParser.UnaryContext ctx) {
		DijkstraType t = BOOLEAN;
		if(ctx.getChild(0).getText().equals("-")) {
			t = NUM;
		}
		ctx.expression().accept(this);
		Symbol first = symbols.get(ctx.expression());
		if(first != null) {
			updateType(first, t);
		}
		return t;
	}
	
	/* Primary Expression Types */
	@Override
	public DijkstraType visitArrayAccessor(@NotNull DijkstraParser.ArrayAccessorContext ctx) {
		return arrays.get(ctx).getType();
	}
	
	@Override
	public DijkstraType visitFunctionCall(@NotNull DijkstraParser.FunctionCallContext ctx) {
		
		return functions.get(ctx).getType();
	}
	
	@Override
	public DijkstraType visitInteger(@NotNull DijkstraParser.IntegerContext ctx) {
		return INT;
	}
	
	@Override
	public DijkstraType visitFloat(@NotNull DijkstraParser.FloatContext ctx) {
		return FLOAT;
	}
	
	@Override
	public DijkstraType visitBool(@NotNull DijkstraParser.BoolContext ctx) {
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitCompound(@NotNull DijkstraParser.CompoundContext ctx) {
		return ctx.expression().accept(this);
	}
	
	@Override
	public DijkstraType visitIdexp(@NotNull DijkstraParser.IdexpContext ctx) {
		return symbols.get(ctx).getType();
	}
	
	/* Type */
	
	@Override 
	public DijkstraType visitType(TypeContext ctx) 
	{
		DijkstraType t;
		if (ctx.INT() != null) {
			t = INT;
		} else if (ctx.FLOAT() != null) {
			t = FLOAT;
		} else {
			t = BOOLEAN;
		}
		return t;
	}

	/**
	 * Is this stage complete (i.e. there were no changes)
	 * @return
	 */
	public boolean isComplete() {
		return !changed;
	}
	
	/**
	 * Updates the given symbol type, and records if that means we've changed
	 * @param symbol symbol to update
	 * @param t target type
	 */
	private void updateType(Symbol symbol, DijkstraType t) {
		changed = symbol.updateType(t) || changed;
	}
	
}
