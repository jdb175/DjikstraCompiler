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
	public ParseTreeProperty<DijkstraType> types = new ParseTreeProperty<DijkstraType>();
	
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
		types.put(ctx, BOOLEAN);
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
		types.put(ctx, BOOLEAN);
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
		types.put(ctx, BOOLEAN);
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
			types.put(ctx, FLOAT);
			return FLOAT;
		} else if(ctx.DIV() != null || ctx.MOD() != null) {
			if(first != null) {
				updateType(first, INT);
			}
			if(second != null) {
				updateType(second, INT);
			}
			types.put(ctx, INT);
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
				types.put(ctx, FLOAT);
				return FLOAT;
			} else {
				types.put(ctx, NUM);
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
		types.put(ctx, t);
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
		types.put(ctx, BOOLEAN);
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
		types.put(ctx, t);
		return t;
	}
	
	/* Primary Expression Types */
	@Override
	public DijkstraType visitArrayAccessor(@NotNull DijkstraParser.ArrayAccessorContext ctx) {
		DijkstraType t = arrays.get(ctx).getType();
		types.put(ctx, t);
		return t;
	}
	
	@Override
	public DijkstraType visitFunctionCall(@NotNull DijkstraParser.FunctionCallContext ctx) {
		DijkstraType t = functions.get(ctx).getType();
		if(t == UNDEFINED) {
			throw new DijkstraSymbolException("Attempted to call procedure " + ctx.ID().getText() + " as a function!");
		}
		//iterate over and check parameters
		ArgListContext args = ctx.argList();
		MethodSymbol method = (MethodSymbol) functions.get(ctx);
		int i = 0;
		while(args != null) {
			Symbol param = symbols.get(args.expression());
			if(param != null) {
				param.updateType(method.getParameter(i));
			}
			++i;
			args = args.argList();
		}
		types.put(ctx, t);
		return t;
	}
	
	@Override
	public DijkstraType visitProcedureCall(@NotNull DijkstraParser.ProcedureCallContext ctx) {
		DijkstraType t = functions.get(ctx).getType();
		if(t != UNDEFINED) {
			throw new DijkstraSymbolException("Attempted to call function " + ctx.ID().getText() + " as a procedure!");
		}
		//iterate over and check parameters
		ArgListContext args = ctx.argList();
		MethodSymbol method = (MethodSymbol) functions.get(ctx);
		int i = 0;
		while(args != null) {
			Symbol param = symbols.get(args.expression());
			if(param != null) {
				param.updateType(method.getParameter(i));
			}
			++i;
			args = args.argList();
		}
		types.put(ctx, t);
		return t;
	}
	
	@Override
	public DijkstraType visitInteger(@NotNull DijkstraParser.IntegerContext ctx) {
		types.put(ctx, INT);
		return INT;
	}
	
	@Override
	public DijkstraType visitFloat(@NotNull DijkstraParser.FloatContext ctx) {
		types.put(ctx, FLOAT);
		return FLOAT;
	}
	
	@Override
	public DijkstraType visitBool(@NotNull DijkstraParser.BoolContext ctx) {
		types.put(ctx, BOOLEAN);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitCompound(@NotNull DijkstraParser.CompoundContext ctx) {
		return ctx.expression().accept(this);
	}
	
	@Override
	public DijkstraType visitIdexp(@NotNull DijkstraParser.IdexpContext ctx) {
		DijkstraType t = symbols.get(ctx).getType();
		types.put(ctx, t);
		return t;
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
