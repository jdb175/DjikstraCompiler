/*******************************************************************************
 * Copyright (c) 2015 Gary F. Pollice
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Used in CS4533/CS544 at Worcester Polytechnic Institute
 *******************************************************************************/

package dijkstra.symbol;

import java.util.*;
import dijkstra.utility.DijkstraType;

/**
 * Singleton manager class that manages all symbol tables in the compilation.
 * 
 * @version Feb 7, 2015
 */
public class SymbolTableManager
{
	private static SymbolTableManager instance =  null;
	private SymbolTable currentSymbolTable;
	private final ArrayList<SymbolTable> tables;
	public final Map<Integer, Symbol> symbols;
	
	/**
	 * Constructor that sets up the initial (global) symbol table.
	 */
	private SymbolTableManager()
	{
		tables = new ArrayList<SymbolTable>();
		currentSymbolTable = new SymbolTable(null);
		tables.add(currentSymbolTable);
		symbols = new HashMap<Integer, Symbol>();
	}
	
	/**
	 * Enter a new scope. This adds a new symbol table to the lexical scope.
	 */
	public void enterScope()
	{
		currentSymbolTable = new SymbolTable(currentSymbolTable);
		tables.add(currentSymbolTable);
	}
	
	/**
	 * Exit a scope.
	 */
	public void exitScope()
	{
		currentSymbolTable = currentSymbolTable.getParent();
	}

	/**
	 * @return the instance
	 */
	public static SymbolTableManager getInstance()
	{
		if (instance == null) {
			instance = new SymbolTableManager();
		}
		return instance;
	}
	
	// The next methods are pass through methods to the current symbol table, but the
	// symbol table manager takes care of creating the appropriate symbols.
	
	/**
	 * Add a symbol to the current symbol table.
	 * @param symbol the symbol to add
	 * @return the added symbol
	 * @throws DijkstraSymbolException if the symbol already exists in this table
	 * @see SymbolTable#add(Symbol)
	 */
	public Symbol add(Symbol symbol)
	{
		return currentSymbolTable.add(symbol);
	}
	
	/**
	 * Add a symbol to the current symbol table with a type of UNDEFINED.
	 * @param id the symbol name 
	 * @return the added symbol
	 * @throws DijkstraSymbolException if the symbol already exists in this table
	 * @see SymbolTable#add(Symbol)
	 * @see Symbol#Symbol(String)
	 */
	public Symbol add(String id)
	{
		Symbol symbol = currentSymbolTable.add(new Symbol(id));
		return symbol;
	}
	
	/**
	 * Add a symbol to the current symbol function table with the type specified.
	 * @param id the symbol name 
	 * @param symbolType the symbol's type
	 * @return the added symbol
	 * @throws DijkstraSymbolException if the symbol already exists in this table
	 * @see SymbolTable#add(Symbol)
	 * @see Symbol#Symbol(String, dijkstra.utility.DijkstraType)
	 */
	public MethodSymbol addFunction(String id, DijkstraType symbolType)
	{
		MethodSymbol symbol = currentSymbolTable.addFunction(new MethodSymbol(id, symbolType));
		return symbol;
	}
	
	/**
	 * Add a symbol to the current symbol function table with the type specified.
	 * @param id the symbol name 
	 * @param symbolType the symbol's type
	 * @return the added symbol
	 * @throws DijkstraSymbolException if the symbol already exists in this table
	 * @see SymbolTable#add(Symbol)
	 * @see Symbol#Symbol(String, dijkstra.utility.DijkstraType)
	 */
	public MethodSymbol addProcedure(String id, DijkstraType symbolType)
	{
		MethodSymbol symbol = currentSymbolTable.addProcedure(new MethodSymbol(id, symbolType));
		return symbol;
	}

	/**
	 * Add a symbol to the current symbol array table with the type specified.
	 * @param id the symbol name 
	 * @param symbolType the symbol's type
	 * @return the added symbol
	 * @throws DijkstraSymbolException if the symbol already exists in this table
	 * @see SymbolTable#add(Symbol)
	 * @see Symbol#Symbol(String, dijkstra.utility.DijkstraType)
	 */
	public Symbol addArray(String id, DijkstraType symbolType)
	{
		Symbol symbol = currentSymbolTable.addArray(new Symbol(id, symbolType));
		return symbol;
	}
	
	/**
	 * Add a symbol to the current symbol table with the type specified.
	 * @param id the symbol name 
	 * @param symbolType the symbol's type
	 * @return the added symbol
	 * @throws DijkstraSymbolException if the symbol already exists in this table
	 * @see SymbolTable#add(Symbol)
	 * @see Symbol#Symbol(String, dijkstra.utility.DijkstraType)
	 */
	public Symbol add(String id, DijkstraType symbolType)
	{
		Symbol symbol = currentSymbolTable.add(new Symbol(id, symbolType));
		return symbol;
	}
	
	/**
	 * Get the symbol with the specified key in the current scope.
	 * @param id the desired symbol's ID
	 * @return the symbol referenced or null if it does not exist.
	 * @see SymbolTable#getSymbol(String)
	 */
	public Symbol getSymbol(String id)
	{
		return currentSymbolTable.getSymbol(id);
	}
	
	/**
	 * Get the function symbol with the specified key in the current scope.
	 * @param id the desired symbol's ID
	 * @return the symbol referenced or null if it does not exist.
	 * @see SymbolTable#getSymbol(String)
	 */
	public MethodSymbol getFunction(String id)
	{
		return currentSymbolTable.getFunction(id);
	}
	
	/**
	 * Get the function symbol with the specified key in the current scope.
	 * @param id the desired symbol's ID
	 * @return the symbol referenced or null if it does not exist.
	 * @see SymbolTable#getSymbol(String)
	 */
	public MethodSymbol getProcedure(String id)
	{
		return currentSymbolTable.getProcedure(id);
	}
	
	/**
	 * Get the array symbol with the specified key in the current scope.
	 * @param id the desired symbol's ID
	 * @return the symbol referenced or null if it does not exist.
	 * @see SymbolTable#getSymbol(String)
	 */
	public Symbol getArray(String id)
	{
		return currentSymbolTable.getArray(id);
	}

	/**
	 * @return the current symbol table
	 */
	public SymbolTable getCurrentSymbolTable()
	{
		return currentSymbolTable;
	}
	
	// Next methods added for testing and debugging
	/**
	 * @param i
	 * @return the symbol table at index i in the symbol table array
	 */
	public SymbolTable getSymbolTable(int i)
	{
		return tables.get(i);
	}
	
	public void reset()
	{
		tables.clear();
		symbols.clear();
		currentSymbolTable = new SymbolTable(null);
		tables.add(currentSymbolTable);
	}
}
