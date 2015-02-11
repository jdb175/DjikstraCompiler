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

import static org.junit.Assert.*;
import org.junit.*;
import static dijkstra.utility.DijkstraType.*;

/**
 * Description
 * @version Feb 6, 2015
 */
public class SymbolTableTest
{
	private SymbolTable st, st1;
	private Symbol a = new Symbol("a"),
			b = new Symbol("b"),
			c = new Symbol("c");
	@Before
	public void setup()
	{
		st = new SymbolTable(null);
		st1 = new SymbolTable(st);
	}
	
	@Test
	public void addFirstItem()
	{
		assertEquals(UNDEFINED, a.getType());
		st.add(a);
		assertEquals(a, st.getSymbol("a"));
	}
	
	@Test
	public void getInParent()
	{
		st.add(a);
		assertEquals(a, st1.getSymbol("a"));
	}
	
	@Test
	public void getNonexistentElement()
	{
		assertNull(st.getSymbol("notHere"));
	}
	
	@Test
	public void shadowSymbol()
	{
		st.add(a);
		final Symbol symbol = new Symbol("a");
		st1.add(symbol);
		final Symbol symbol1 = st1.getSymbol("a");
		assertEquals(a, symbol1);
		assertTrue(symbol == symbol1);
		assertFalse(a == symbol1);
	}
	
//	@Test
//	public void addIfNew()
//	{
//		st.add(a);
//		final Symbol symbol = st1.addIfNew(new Symbol("a"));
//		assertTrue(a == symbol);
//	}
	
	@Test(expected=DijkstraSymbolException.class)
	public void addDuplicateSymbol()
	{
		st.add(a);
		st.add(new Symbol("a"));
	}
}
