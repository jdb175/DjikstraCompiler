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

import static dijkstra.utility.DijkstraType.*;
import dijkstra.utility.DijkstraType;

/**
 * This class defines a Symbol object that gets stored in a symbol table and is
 * bound to identifiers and other entities. This is a simple entity data structure.
 * @version Feb 4, 2015
 */
public class Symbol
{
	private final String id;
	private DijkstraType type;
	private String value;
	
	/**
	 * Constructor that creates the Symbol with the name, and an UNDEFINED type.
	 * @param id the symbol name
	 */
	public Symbol(String id)
	{
		this(id, UNDEFINED);
	}
	
	/**
	 * Constructor that creates the Symbol with the specified name and type.
	 * @param id the symbol name
	 * @param type the type assigned to the symbol
	 */
	public Symbol(String id, DijkstraType type)
	{
		if(type == null) {
			type = UNDEFINED;
		}
		this.id = id;
		this.type = type;
		this.value = null;
	}

	/**
	 * @return the type
	 */
	public DijkstraType getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(DijkstraType type)
	{
		this.type = type;
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * @return the name
	 */
	public String getId()
	{
		return id;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof Symbol)) {
			return false;
		}
		Symbol other = (Symbol) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("name=");
		builder.append(id);
		builder.append(", DijkstraSymbol [type=");
		builder.append(type);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Updates the type to be newType, if that is consistent. Otherwise
	 * throws an exception
	 * @param newType
	 */
	public void updateType(DijkstraType newType) {
		if(newType == BOOLEAN) {
			if(type == NUM || type == INT || type == FLOAT) {
				throw new DijkstraSymbolException("Cannot assign updated type "+newType+" to a symbol of type "+type+" ("+id+")");
			} else {
				type = newType;
			}
		} else if (newType == NUM) {
			if(type == BOOLEAN) {
				throw new DijkstraSymbolException("Cannot assign updated type "+newType+" to a symbol of type "+type+" ("+id+")");
			} else if (type == UNDEFINED) {
				type = NUM;
			}
		} else if (newType == INT) {
			if(type == BOOLEAN || type == FLOAT) {
				throw new DijkstraSymbolException("Cannot assign updated type "+newType+" to a symbol of type "+type+" ("+id+")");
			} else {
				type = newType;
			}
		} else if (newType == FLOAT) {
			if(type == BOOLEAN) {
				throw new DijkstraSymbolException("Cannot assign updated type "+newType+" to a symbol of type "+type+" ("+id+")");
			} else if (type == UNDEFINED || type == NUM){
				type = FLOAT;
			}
		} 
	}
}
