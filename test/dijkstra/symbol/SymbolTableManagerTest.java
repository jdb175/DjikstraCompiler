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
 * Test cases for the SymbolTableManager singleton.
 * @version Feb 8, 2015
 */
public class SymbolTableManagerTest
{
	private SymbolTableManager stm = SymbolTableManager.getInstance();
	
	@Before
	public void setup()
	{
		stm.reset();
	}
	
	@Test
	public void addSymbol()
	{
		final Symbol symbol = stm.add("a");
		assertEquals("a", symbol.getId());
		assertEquals(UNDEFINED, symbol.getType());
	}
	
	@Test
	public void addSymbolAndType()
	{
		final Symbol symbol = stm.add("a", BOOLEAN);
		assertEquals("a", symbol.getId());
		assertEquals(BOOLEAN, symbol.getType());
	}

	@Test(expected=DijkstraSymbolException.class)
	public void addSymbolTwice()
	{
		stm.add("a");
		stm.add("a", INT);
	}
	
	@Test
	public void addSameIdInParentAndChild()
	{
		final Symbol a1 = stm.add("a");
		stm.enterScope();
		final Symbol a2 = stm.add("a");
		assertFalse(a1 == a2);
		assertTrue(stm.getSymbol("a") == a2);
		stm.exitScope();
		assertFalse(stm.getSymbol("a") == a2);
		assertTrue(stm.getSymbol("a") == a1);
	}
	
	@Test
	public void canGetParentId()
	{
		final Symbol a1 = stm.add("a");
		stm.enterScope();
		assertTrue(stm.getSymbol("a") == a1);
		stm.exitScope();
		assertTrue(stm.getSymbol("a") == a1);
	}
}
