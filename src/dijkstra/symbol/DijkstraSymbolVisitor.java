package dijkstra.symbol;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import dijkstra.lexparse.DijkstraBaseVisitor;
import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.*;
import dijkstra.semantic.DijkstraSemanticException;
import dijkstra.utility.DijkstraType;
import static dijkstra.utility.DijkstraType.*;

public class DijkstraSymbolVisitor extends DijkstraBaseVisitor<DijkstraType> {
	public ParseTreeProperty<Symbol> symbols = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> functions = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> arrays = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<DijkstraType> types = new ParseTreeProperty<DijkstraType>();

	private SymbolTableManager stm = SymbolTableManager.getInstance();
	
	@Override 
	public DijkstraType visitVariableDeclaration(@NotNull DijkstraParser.VariableDeclarationContext ctx) 
	{ 
		DijkstraType t;
		//Get the type
		TypeContext type = ctx.type();
		t = type.accept(this);
		//Now get all of the ids and add them as symbols
		IdListContext idlist = ctx.idList();
		while(idlist != null) {
			String id = idlist.ID().getText();
			Symbol symbol = stm.add(id, t);		
			symbols.put(idlist, symbol);
			idlist = idlist.idList();
		}
		types.put(ctx, t);
		return t;
	}
	
	@Override
	public DijkstraType visitParameter(@NotNull DijkstraParser.ParameterContext ctx) {
		DijkstraType t = UNDEFINED;
		if(ctx.type() != null) {
			t = ctx.type().accept(this);
		}
		String id = ctx.ID().getText();
		Symbol s = stm.add(id, t);
		symbols.put(ctx, s);
		types.put(ctx, t);
		return t;
	}
	
	@Override
	public DijkstraType visitArrayDeclaration(@NotNull DijkstraParser.ArrayDeclarationContext ctx) 
	{
		DijkstraType t;
		//Get the type
		TypeContext type = ctx.type();
		t = type.accept(this);
		//Now get all of the ids and add them as symbols
		ctx.expression().accept(this);
		IdListContext idlist = ctx.idList();
		while(idlist != null) {
			String id = idlist.ID().getText();
			Symbol symbol = stm.addArray(id, t);
			arrays.put(ctx, symbol);
			symbols.put(idlist, symbol);
			idlist = idlist.idList();
		}
		types.put(ctx, t);
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
			if(exprList == null) {
				throw new DijkstraSemanticException("Too few expressions in assign statement [" + ctx.getStart().getLine() + "]");
			}
			DijkstraType t = exprList.expression().accept(this);
			//Now get id
			String id;
			if(var.ID() != null) {
				id = var.ID().getText();
				//create the symbol if it is not an accessor
				//addifNew
				Symbol symbol = stm.getSymbol(id);
				if(symbol == null) {
					symbol = stm.add(id, t);
				} else {
					symbol.updateType(t);
				}
				symbols.put(var, symbol);
			}
			varList = varList.varList();
			exprList = exprList.expressionList();
		}
		
		if(exprList != null) {
			throw new DijkstraSemanticException("Too many expressions in assign statement [" + ctx.getStart().getLine() + "]");
		}
		
		return null;
	}
	
	@Override
	public DijkstraType visitInputStatement(@NotNull DijkstraParser.InputStatementContext ctx) {
		IdListContext idlist = ctx.idList();
		while(idlist != null) {
			String id = idlist.ID().getText();
			Symbol symbol = stm.getSymbol(id);
			if(symbol == null)
				symbol = stm.add(id);
			symbols.put(idlist, symbol);
			idlist = idlist.idList();
		}
		return null;
	}
	
	/* Scope changing declarations */
	@Override
	public DijkstraType visitProcedureDeclaration(@NotNull DijkstraParser.ProcedureDeclarationContext ctx) {
		MethodSymbol symbol = stm.addProcedure(ctx.ID().getText(), PROCEDURE);
		symbols.put(ctx, symbol);
		stm.enterScope();
		stm.setInMethod(true);
		//Handle parameters
		ParameterListContext param = ctx.parameterList();
		while(param != null) {
			DijkstraType p_t = param.parameter().accept(this);
			symbol.addParameter(p_t);
			param = param.parameterList();
		}
		//Visit body
		ctx.compoundStatement().accept(this);
		stm.setInMethod(false);
		stm.exitScope();
		return null;
	}
	
	/* Scope changing declarations */
	@Override
	public DijkstraType visitFunctionDeclaration(@NotNull DijkstraParser.FunctionDeclarationContext ctx) {
		DijkstraType t = ctx.type().accept(this);
		MethodSymbol symbol = stm.addFunction(ctx.ID().getText(), t);
		functions.put(ctx, symbol);
		stm.enterScope();
		stm.setInMethod(true);
		//Handle parameters
		ParameterListContext param = ctx.parameterList();
		while(param != null) {
			DijkstraType p_t = param.parameter().accept(this);
			symbol.addParameter(p_t);
			param = param.parameterList();
		}
		//Visit body
		ctx.compoundStatement().accept(this);
		stm.setInMethod(false);
		stm.exitScope();
		return null;
	}
	
	
	@Override
	public DijkstraType visitCompoundStatement(@NotNull CompoundStatementContext ctx) {
		//if the parent is a function or procedure then we are already in the right context
		stm.enterScope();
		visitChildren(ctx);
		stm.exitScope();
		return null;
	}
	
	/* Easy Expression Types */
	@Override
	public DijkstraType visitEqual(@NotNull DijkstraParser.EqualContext ctx) {
		//Figure out types
		visitChildren(ctx);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitOr(@NotNull DijkstraParser.OrContext ctx) {
		visitChildren(ctx);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitAnd(@NotNull DijkstraParser.AndContext ctx) {
		visitChildren(ctx);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitMult(@NotNull DijkstraParser.MultContext ctx) {
		visitChildren(ctx);
		return NUM;
	}
	
	@Override
	public DijkstraType visitAdd(@NotNull DijkstraParser.AddContext ctx) {
		visitChildren(ctx);
		return NUM;
	}
	
	@Override
	public DijkstraType visitRelational(@NotNull DijkstraParser.RelationalContext ctx) {
		visitChildren(ctx);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitUnary(@NotNull DijkstraParser.UnaryContext ctx) {
		DijkstraType t = BOOLEAN;
		if(ctx.getChild(0).getText().equals("-")) {
			t = NUM;
		}
		ctx.expression().accept(this);
		return t;
	}
	
	/* Primary Expression Types */
	@Override
	public DijkstraType visitArrayAccessor(@NotNull DijkstraParser.ArrayAccessorContext ctx) {
		Symbol arr = stm.getArray(ctx.ID().getText());
		if(arr == null) {
			throw new DijkstraSymbolException("No array with name " + ctx.ID().getText() + " has been defined");
		}
		ctx.expression().accept(this);
		arrays.put(ctx, arr);
		types.put(ctx, arr.getType());
		return arr.getType();
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
		Symbol fun = stm.getFunction(ctx.ID().getText());
		if(fun == null) {
			throw new DijkstraSymbolException("No function with name " + ctx.ID().getText() + " has been defined");
		}
		functions.put(ctx, fun);
		if(ctx.argList() != null) {
			ctx.argList().accept(this);
		}
		types.put(ctx, fun.getType());
		return fun.getType();
	}
	
	@Override
	public DijkstraType visitProcedureCall(@NotNull DijkstraParser.ProcedureCallContext ctx) {
		Symbol proc = stm.getProcedure(ctx.ID().getText());
		if(proc == null) {
			throw new DijkstraSymbolException("No procedure with name " + ctx.ID().getText() + " has been defined");
		}
		symbols.put(ctx, proc);
		if(ctx.argList() != null) {
			ctx.argList().accept(this);
		}
		return proc.getType();
	}
	
	@Override
	public DijkstraType visitArgList(@NotNull ArgListContext ctx) {
		ctx.expression().accept(this);
		if(ctx.argList() != null) {
			ctx.argList().accept(this);
		}
		return null;
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
	public DijkstraType visitIdexp(@NotNull DijkstraParser.IdexpContext ctx) {
		Symbol s = stm.getSymbol(ctx.ID().getText());
		if(s == null){
			throw new DijkstraSymbolException("Reference to symbol " + ctx.ID().getText() + ", which does not exist.");
		}
		symbols.put(ctx, s);
		types.put(ctx, s.getType());
		return s.getType();
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
	
}
