package djikstra.semantic;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.RuleNode;

import dijkstra.lexparse.DijkstraBaseVisitor;
import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.*;
import dijkstra.symbol.DijkstraSymbolException;
import dijkstra.symbol.DijkstraSymbolVisitor;
import dijkstra.symbol.MethodSymbol;
import dijkstra.symbol.Symbol;
import dijkstra.utility.DijkstraType;
import static dijkstra.utility.DijkstraType.*;

public class DjikstraTypeResolutionVisitor extends DijkstraBaseVisitor<DijkstraType> {
	public ParseTreeProperty<Symbol> symbols = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> functions = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> arrays = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<DijkstraType> types = new ParseTreeProperty<DijkstraType>();
	
	private boolean changed = true;
	
	public DjikstraTypeResolutionVisitor(DijkstraSymbolVisitor oldVisitor) {
		super();
		symbols = oldVisitor.symbols;
		functions = oldVisitor.functions;
		arrays = oldVisitor.arrays;
		types = oldVisitor.types;
	}
	
	@Override
	public DijkstraType visitDijkstraText(@NotNull DijkstraParser.DijkstraTextContext ctx) {
		changed = false;
		visitChildren(ctx);
		return null;
	}
	
	@Override
	public DijkstraType visitArrayDeclaration(@NotNull DijkstraParser.ArrayDeclarationContext ctx) {
		DijkstraType t = arrays.get(ctx).getType();
		ctx.expression().accept(this);
		updateType(ctx.expression(), INT);
		return t;
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
			var.accept(this);
			//Get type from expression
			DijkstraType t = exprList.expression().accept(this);
			//Now get id
			updateType(var, t);
			varList = varList.varList();
			exprList = exprList.expressionList();
		}
		
		return null;
	}
	
	@Override
	public DijkstraType visitVar(@NotNull VarContext ctx) {
		DijkstraType t;
		if(ctx.ID()!=null) {
			t = ctx.ID().accept(this);
		} else {
			t = ctx.arrayAccessor().accept(this);
		}
		if(t != types.get(ctx)) {
			types.put(ctx, t);
			changed = true;
		}
		return t;
	}

	/* Complex Expression Types */
	@Override
	public DijkstraType visitEqual(@NotNull DijkstraParser.EqualContext ctx) {
		//Figure out types
		DijkstraType t1 = ctx.expression(0).accept(this);
		DijkstraType t2 = ctx.expression(1).accept(this);
		
		if(t2 != UNDEFINED) {
			updateType(ctx.expression(0), t2);
		} 
		if(t1 != UNDEFINED) {
			updateType(ctx.expression(1), t1);
		}
		types.put(ctx, BOOLEAN);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitOr(@NotNull DijkstraParser.OrContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		updateType(ctx.expression(0), BOOLEAN);
		updateType(ctx.expression(1), BOOLEAN);
		types.put(ctx, BOOLEAN);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitAnd(@NotNull DijkstraParser.AndContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		updateType(ctx.expression(0), BOOLEAN);
		updateType(ctx.expression(1), BOOLEAN);
		types.put(ctx, BOOLEAN);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitMult(@NotNull DijkstraParser.MultContext ctx) {
		DijkstraType t1 = ctx.expression(0).accept(this);
		DijkstraType t2 = ctx.expression(1).accept(this);

		if(ctx.SLASH() != null) {
			updateType(ctx.expression(0), FLOAT);
			updateType(ctx.expression(1), FLOAT);
			types.put(ctx, FLOAT);
			return FLOAT;
		} else if(ctx.DIV() != null || ctx.MOD() != null) {
			updateType(ctx.expression(0), INT);
			updateType(ctx.expression(1), INT);
			types.put(ctx, INT);
			return INT;
		} else {
			updateType(ctx.expression(0), NUM);
			updateType(ctx.expression(1), NUM);
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
	public DijkstraType visitOutputStatement(@NotNull OutputStatementContext ctx) {
		DijkstraType t = ctx.expression().accept(this);
		types.put(ctx,  t);
		return t;
	}
	
	@Override
	public DijkstraType visitGuard(@NotNull DijkstraParser.GuardContext ctx) {
		updateType(ctx.expression(), BOOLEAN);
		ctx.statement().accept(this);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitAdd(@NotNull DijkstraParser.AddContext ctx) {
		DijkstraType t1 = ctx.expression(0).accept(this);
		DijkstraType t2 = ctx.expression(1).accept(this);
		DijkstraType t = NUM;
		if(t1 == FLOAT || t2 == FLOAT) {
			t = FLOAT;
		} else if (t1 == INT && t2 == INT) {
			t = INT;
		}
		updateType(ctx.expression(0), t);
		updateType(ctx.expression(1), t);
		types.put(ctx, t);
		return t;
	}
	
	@Override
	public DijkstraType visitRelational(@NotNull DijkstraParser.RelationalContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		updateType(ctx.expression(0), NUM);
		updateType(ctx.expression(1), NUM);
		types.put(ctx, BOOLEAN);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitUnary(@NotNull DijkstraParser.UnaryContext ctx) {
		DijkstraType t = BOOLEAN;
		if(ctx.getChild(0).getText().equals("-")) {
			t = ctx.expression().accept(this);
			if(t == UNDEFINED) {
				t = NUM;
			}
		}

		updateType(ctx.expression(), t);
		types.put(ctx, t);
		return t;
	}
	
	/* Primary Expression Types */
	@Override
	public DijkstraType visitArrayAccessor(@NotNull DijkstraParser.ArrayAccessorContext ctx) {
		DijkstraType t = arrays.get(ctx).getType();
		ctx.expression().accept(this);
		Symbol s = symbols.get(ctx.expression());
		if(s != null) {
			s.updateType(INT);
		}
		types.put(ctx, t);
		return t;
	}
	
	/* Primary Expression Types */
	@Override
	public DijkstraType visitArrayAccess(@NotNull DijkstraParser.ArrayAccessContext ctx) {
		DijkstraType t = ctx.arrayAccessor().accept(this);
		types.put(ctx, t);
		return t;
	}
	
	@Override
	public DijkstraType visitFunctionCall(@NotNull DijkstraParser.FunctionCallContext ctx) {
		DijkstraType t = functions.get(ctx).getType();
		if(t == PROCEDURE) {
			throw new DijkstraSymbolException("Attempted to call procedure " + ctx.ID().getText() + " as a function!");
		}
		//iterate over and check parameters
		ArgListContext args = ctx.argList();
		MethodSymbol method = (MethodSymbol) functions.get(ctx);
		int i = 0;
		while(args != null) {
			args.expression().accept(this);
			updateType(args.expression(), method.getParameter(i));
			++i;
			args = args.argList();
		}
		types.put(ctx, t);
		//Check contents
		return t;
	}
	
	@Override
	public DijkstraType visitFCall(@NotNull DijkstraParser.FCallContext ctx) {
		DijkstraType t = ctx.functionCall().accept(this);
		types.put(ctx, t);
		return t;
	}
	
	@Override
	public DijkstraType visitProcedureCall(@NotNull DijkstraParser.ProcedureCallContext ctx) {
		DijkstraType t = functions.get(ctx).getType();
		if(t != PROCEDURE) {
			throw new DijkstraSymbolException("Attempted to call function " + ctx.ID().getText() + " as a procedure!");
		}
		//iterate over and check parameters
		ArgListContext args = ctx.argList();
		MethodSymbol method = (MethodSymbol) functions.get(ctx);
		int i = 0;
		while(args != null) {
			args.expression().accept(this);
			updateType(args.expression(), method.getParameter(i));
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
		DijkstraType t = ctx.expression().accept(this);
		types.put(ctx, t);
		return t;
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
	private void updateType(RuleNode node, DijkstraType t) {
		Symbol symbol = symbols.get(node);
		if(symbol != null) {
			changed = symbol.updateType(t) || changed;
		} else {
			//Handle primitives etc
			DijkstraType existingType = types.get(node);
			if(existingType == NUM || existingType == FLOAT || existingType == INT){
				if(t == BOOLEAN) {
					throw new DijkstraSemanticException("Attempted to use type " + existingType + " for " + t);
				}
			} else if (existingType == BOOLEAN) {
				if(t == NUM || t == FLOAT || t == INT){
					throw new DijkstraSemanticException("Attempted to use type " + existingType + " for " + t);
				}
			}
			
		}
	}
	
}
