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
			//create the symbol if it does not exist
			Symbol symbol = stm.getSymbol(id);
			if(symbol == null) {
				stm.add(id, t);
			} else {
				stm.updateType(id, t);
			}
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
	
	/* Complex Expression Types */
	@Override
	public DijkstraType visitEqual(@NotNull DijkstraParser.EqualContext ctx) {
		//Figure out types
		DijkstraType t1 = ctx.expression(0).accept(this);
		DijkstraType t2 = ctx.expression(1).accept(this);
		DijkstraType t = UNDEFINED;
		if(t1 == UNDEFINED) {
			t1 = t2;
		} else if(t2 == UNDEFINED) {
			t2 = t1;
		}
		
		if(t1 == BOOLEAN && (t2 == FLOAT || t2 == NUM || t2 == INT)){
			throw new DijkstraSymbolException("Attempted to compare two incompatible types with '='");
		} else if(t2 == BOOLEAN && (t1 == FLOAT || t1 == NUM || t1 == INT)){
			throw new DijkstraSymbolException("Attempted to compare two incompatible types with '='");
		}

		
		if(t1 == FLOAT || t2 == FLOAT || t1 == INT || t2 == INT || t1 == NUM || t2 == NUM) {
			t = NUM;
		} else if (t1 == BOOLEAN || t2 == BOOLEAN) {
			t = BOOLEAN;
		}
		
		//Update symbols
		Symbol first = symbols.get(ctx.expression(0));
		Symbol second = symbols.get(ctx.expression(1));
		if(first != null) 
			stm.updateType(first.getId(), t);
		if(second != null) 
			stm.updateType(second.getId(), t);
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitOr(@NotNull DijkstraParser.OrContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		Symbol first = symbols.get(ctx.expression(0));
		if(first != null) {
			stm.updateType(first.getId(), BOOLEAN);
		}
		Symbol second = symbols.get(ctx.expression(1));
		if(second != null) {
			stm.updateType(second.getId(), BOOLEAN);
		}
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitAnd(@NotNull DijkstraParser.AndContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		Symbol first = symbols.get(ctx.expression(0));
		if(first != null) {
			stm.updateType(first.getId(), BOOLEAN);
		}
		Symbol second = symbols.get(ctx.expression(1));
		if(second != null) {
			stm.updateType(second.getId(), BOOLEAN);
		}
		return BOOLEAN;
	}
	
	@Override
	public DijkstraType visitMult(@NotNull DijkstraParser.MultContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		Symbol first = symbols.get(ctx.expression(0));
		if(first != null) {
			stm.updateType(first.getId(), NUM);
		}
		Symbol second = symbols.get(ctx.expression(1));
		if(second != null) {
			stm.updateType(second.getId(), NUM);
		}
		if(first.getType() == FLOAT || second.getType() == FLOAT) {
			return FLOAT;
		} else {
			return NUM;
		}
	}
	
	@Override
	public DijkstraType visitAdd(@NotNull DijkstraParser.AddContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		Symbol first = symbols.get(ctx.expression(0));
		if(first != null) {
			stm.updateType(first.getId(), NUM);
		}
		Symbol second = symbols.get(ctx.expression(1));
		if(second != null) {
			stm.updateType(second.getId(), NUM);
		}
		if(first.getType() == FLOAT || second.getType() == FLOAT) {
			return FLOAT;
		} else {
			return NUM;
		}
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
	
	@Override
	public DijkstraType visitCompound(@NotNull DijkstraParser.CompoundContext ctx) {
		return ctx.expression().accept(this);
	}
	
	@Override
	public DijkstraType visitArrayAccessor(@NotNull DijkstraParser.ArrayAccessorContext ctx) {
		//Return type of the array if it exists
		//Otherwise it's an error
		Symbol s = stm.getSymbol(ctx.ID().getText());
		if(s == null){
			throw new DijkstraSymbolException("Reference to array " + ctx.ID().getText() + "[], which does not exist.");
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
