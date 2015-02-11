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
		//	symbols.put(ctx, symbol);
		//	types.put(ctx, t);
			idlist = idlist.idList();
		}
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
			Symbol symbol = stm.add(id, t);
		//	symbols.put(ctx, symbol);
		//	types.put(ctx, t);
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
			} else {
				id = var.arrayAccessor().ID().getText();
			}
			Symbol symbol = stm.add(id, t);
		//	symbols.put(ctx, symbol);
		//	types.put(ctx, t);
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
		//	symbols.put(ctx, symbol);
		//	types.put(ctx, t);
			idlist = idlist.idList();
		}
		
		return null;
	}
	
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
