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
 * A Dijkstra compiler symbol table
 * @version Feb 6, 2015
 */
public class SymbolTable
{
	private final SymbolTable parent;
	private final Map<String, Symbol> symbols;
	
	/**
	 * Sole constructor. Creates the symbol table with the specified parent. The parent
	 * can be null for the global lexical level symbol table.
	 * @param parent the symbol table at the enclosing lexical level.
	 */
	public SymbolTable(SymbolTable parent)
	{
		this.parent = parent;
		symbols = new HashMap<String, Symbol>();
	}
	
	/**
	 * Add the specified Symbol to the current symbol table.
	 * @param symbol the symbol to add to the table
	 * @return the symbol that was added
	 * @throws DijkstraSymbolException if the symbol already exists in this table
	 */
	public Symbol add(Symbol symbol) 
	{
		final Symbol s = symbols.put(symbol.getId(), symbol);
		if (s != null) {	// Symbol was already in the table
			throw new DijkstraSymbolException(
					"Attempting to add a duplicate symbol to a symbol table" + s.getId());
		}
		return symbol;
	}
	
//	public Symbol addIfNew(Symbol symbol)
//	{
//		Symbol s = getSymbol(symbol.getId());
//		if (s == null) {
//			s = symbols.put(symbol.getId(), symbol);
//		}
//		return s;
//	}
	
	/**
	 * Get the symbol with the specified key in the current scope.
	 * @param id the desired symbol's ID
	 * @return the symbol referenced or null if it does not exist.
	 */
	public Symbol getSymbol(String id)
	{
		Symbol symbol = symbols.get(id);
		SymbolTable st = this;
		if (symbol == null && st.parent != null) {
			symbol = st.parent.getSymbol(id);
		}
		return symbol;
	}
	
	public int getNumberOfSymbols()
	{
		return symbols.size();
	}
	
	/**
	 * @return the parent of this symbol table
	 */
	public SymbolTable getParent()
	{
		return parent;
	}
	
	/**
	 * @return string of the symbols in this table
	 */
	public String printLocalScope() 
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Table\n");
		builder.append("============\n");
		Iterator it = symbols.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			builder.append(pair.getKey() + " : " + pair.getValue() + "\n");
		}
		return builder.toString();
	}
	
	/**
	 * @return string of the symbols in this table and parents
	 */
	public String printTotalScope() 
	{
		StringBuilder builder = new StringBuilder();
		if(parent != null){
			builder.append("Parent: \n");
			builder.append(parent);
		}
		builder.append(this);
		return builder.toString();
	}
	
	@Override
	public String toString() 
	{
		return printLocalScope();
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((symbols == null) ? 0 : symbols.hashCode());
		return result;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SymbolTable)) {
			return false;
		}
		SymbolTable other = (SymbolTable) obj;
		if (parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!parent.equals(other.parent)) {
			return false;
		}
		if (symbols == null) {
			if (other.symbols != null) {
				return false;
			}
		} else if (!symbols.equals(other.symbols)) {
			return false;
		}
		return true;
	}

	/**
	 * Update the symbol with specified key in the table, if
	 * the current type is consistent.
	 * @param id the desired symbol's ID
	 * @param symbolType the desired type for the symbol
	 * @return the updated symbol
	 */
	public Symbol updateType(String id, DijkstraType symbolType) {
		Symbol s = getSymbol(id);
		s.updateType(symbolType);
		return s;
	}
}
