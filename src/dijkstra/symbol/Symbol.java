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
import dijkstra.codegen.JVMInfo;
import dijkstra.utility.DijkstraType;
import djikstra.semantic.DijkstraSemanticException;

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
	private int address;
	private String fieldName;
	private boolean isFieldInitialized;
	private boolean isLocal;
	public static final int NO_ADDRESS = Integer.MIN_VALUE	;
	private static int curFieldNum = 0;

	
	/**
	 * Constructor that creates the Symbol with the name, and an UNDEFINED type.
	 * @param id the symbol name
	 */
	public Symbol(String id, boolean isLocal)
	{
		this(id, UNDEFINED, isLocal);
	}
	
	/**
	 * Constructor that creates the Symbol with the specified name and type.
	 * @param id the symbol name
	 * @param type the type assigned to the symbol
	 */
	public Symbol(String id, DijkstraType type, boolean isLocal)
	{
		if(type == null) {
			type = UNDEFINED;
		}
		this.address = NO_ADDRESS;
		this.id = id;
		this.isFieldInitialized = false;
		this.type = type;
		this.value = null;
		this.fieldName = null;
		this.isLocal = isLocal;
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
	
	/**
	 * @return the address
	 */
	public int getAddress()
	{
		if(address == NO_ADDRESS) {
			address = JVMInfo.getNextAddress();
		}
		return address;
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
	 * @returns whether the smbol was changed
	 */
	public boolean updateType(DijkstraType newType) {
		DijkstraType old = type;
		if(newType == BOOLEAN) {
			if(type == NUM || type == INT || type == FLOAT) {
				throw new DijkstraSemanticException("Cannot assign updated type "+newType+" to a symbol of type "+type+" ("+id+")");
			} else {
				type = newType;
			}
		} else if (newType == NUM) {
			if(type == BOOLEAN) {
				throw new DijkstraSemanticException("Cannot assign updated type "+newType+" to a symbol of type "+type+" ("+id+")");
			} else if (type == UNDEFINED) {
				type = NUM;
			}
		} else if (newType == INT) {
			if(type == BOOLEAN) {
				throw new DijkstraSemanticException("Cannot assign updated type "+newType+" to a symbol of type "+type+" ("+id+")");
			} else if(type == UNDEFINED || type == NUM) {
				type = newType;
			}
		} else if (newType == FLOAT) {
			if(type == BOOLEAN) {
				throw new DijkstraSemanticException("Cannot assign updated type "+newType+" to a symbol of type "+type+" ("+id+")");
			} else if (type == UNDEFINED || type == NUM){
				type = FLOAT;
			}
		} 
		return type != old;
	}
	
	/**
	 * Returns the equivalent type indicator for given type. Booleans are treated
	 * as integers
	 * @param type type to convert
	 * @return JVM indicator of type
	 */
	public String convertType(DijkstraType type) {
		switch (type) {
			case FLOAT:
				return "F";
			case BOOLEAN:
			case INT:
				return "I";
			default:
				return "V";
		}
	}
	
	/**
	 * Gets a unique name for this field
	 * @return
	 */
	private static String getNewFieldName() {
		return String.valueOf(++curFieldNum);
	}
	
	/**
	 * @return whether this symbol has been initialized as a field
	 */
	public boolean fieldInitialized() {
		return isFieldInitialized;
	}
	
	/**
	 * @return get the field name of this symbol
	 */
	public String getFieldName() {
		if(fieldName == null) {
			fieldName = getNewFieldName();
		}
		return fieldName;
	}

	/**
	 * @return the JVM type identifier for this symbol
	 */
	public String getTypeID() {
		return convertType(this.type);
	}

	/**
	 * Sets the field initialized value of this symbol
	 * @param b
	 */
	public void setFieldInitialized(boolean b) {
		this.isFieldInitialized = b;
	}

	/**
	 * @return whether this symbol is a local variable
	 */
	public boolean isLocal() {
		return isLocal;
	}
}
