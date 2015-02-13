package dijkstra.symbol;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import dijkstra.lexparse.DijkstraBaseVisitor;
import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.*;
import dijkstra.utility.DijkstraType;
import static dijkstra.utility.DijkstraType.*;

public class DijkstraSymbolVisitor extends DijkstraBaseVisitor<DijkstraType> {
	public ParseTreeProperty<Symbol> symbols = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<DijkstraType> types = new ParseTreeProperty<DijkstraType>();
	public ParseTreeProperty<SymbolTable> symbolTables = new ParseTreeProperty<SymbolTable>();
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
		Symbol symbol = stm.add(id, t);
		types.put(ctx, t);
		symbols.put(ctx,  symbol);
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
		IdListContext idlist = ctx.idList();
		while(idlist != null) {
			String id = idlist.ID().getText();
			Symbol symbol = stm.addArray(id, t);
			idlist = idlist.idList();
		}
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
			//Get type from expression
			DijkstraType t = exprList.expression().accept(this);
			//Now get id
			String id;
			if(var.ID() != null) {
				id = var.ID().getText();
				//create the symbol if it is not an accessor
				Symbol symbol = stm.getSymbol(id);
				if(symbol == null) {
					stm.add(id, t);
				}
			}
			varList = varList.varList();
			exprList = exprList.expressionList();
		}
		
		return null;
	}
	
	@Override
	public DijkstraType visitInputStatement(@NotNull DijkstraParser.InputStatementContext ctx) {
		IdListContext idlist = ctx.idList();
		while(idlist != null) {
			String id = idlist.ID().getText();
			Symbol symbol = stm.add(id);
			idlist = idlist.idList();
		}
		
		return null;
	}
	
	/* Scope changing declarations */
	@Override
	public DijkstraType visitProcedureDeclaration(@NotNull DijkstraParser.ProcedureDeclarationContext ctx) {
		Symbol symbol = stm.add(ctx.ID().getText(), PROCEDURE);
		symbols.put(ctx, symbol);
		stm.enterScope();
		visitChildren(ctx);
		symbolTables.put(ctx, stm.getCurrentSymbolTable());
		stm.exitScope();
		return null;
	}
	
	/* Scope changing declarations */
	@Override
	public DijkstraType visitFunctionDeclaration(@NotNull DijkstraParser.FunctionDeclarationContext ctx) {
		DijkstraType t = ctx.type().accept(this);
		Symbol symbol = stm.addFunction(ctx.ID().getText(), t);
		symbols.put(ctx, symbol);
		stm.enterScope();
		visitChildren(ctx);
		symbolTables.put(ctx, stm.getCurrentSymbolTable());
		stm.exitScope();
		return null;
	}
	
	
	@Override
	public DijkstraType visitCompoundStatement(@NotNull CompoundStatementContext ctx) {
		//if the parent is a function or procedure then we are already in the right context
		if(ctx.getParent() instanceof ProcedureDeclarationContext || ctx.getParent() instanceof FunctionDeclarationContext){
			visitChildren(ctx);
			symbolTables.put(ctx, stm.getCurrentSymbolTable());
		} else {
			stm.enterScope();
			visitChildren(ctx);
			symbolTables.put(ctx, stm.getCurrentSymbolTable());
			stm.exitScope();
		}
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
	public DijkstraType visitIdexp(@NotNull DijkstraParser.IdexpContext ctx) {
		Symbol s = stm.getSymbol(ctx.ID().getText());
		if(s == null){
			throw new DijkstraSymbolException("Reference to symbol " + ctx.ID().getText() + ", which does not exist.");
		}
		symbols.put(ctx, s);
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
