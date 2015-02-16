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

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.*;

import dijkstra.lexparse.*;
import dijkstra.lexparse.DijkstraParser.*;
import dijkstra.utility.*;
import static dijkstra.utility.DijkstraType.*;

/**
 * Description
 * @version Feb 7, 2015
 */
public class DijkstraSymbolVisitorTest
{
	private DijkstraParser parser;
	private ParserRuleContext tree;
	private SymbolTableManager stm = SymbolTableManager.getInstance();
	
	@Before
	public void setup()
	{
		stm.reset();
	}
	
	@Test(expected=DijkstraSymbolException.class)
	public void assignWrongType()
	{
		doSymbolTable("boolean a; a <- 1");
	}
	
	@Test
	public void inferByArrayAccessor()
	{
		doSymbolTable("int [2] b; a <- b[1]");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
	}
	
	@Test
	public void inferredVariableFromDecl()
	{
		doSymbolTable("int b a <- b");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
	}
	
	@Test(expected=DijkstraSymbolException.class)
	public void nonExistentArray()
	{
		doSymbolTable("a <- b[1]");
	}
	
	@Test(expected=DijkstraSymbolException.class)
	public void referenceNonexistentID()
	{
		doSymbolTable("c <- a = b");
	}
	
	@Test
	public void NotInferredVariable()
	{
		doSymbolTable("int a; a <- 1");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
	}
	
	@Test
	public void addOneVariable()
	{
		DijkstraSymbolVisitor visitor = doSymbolTable("int i");
		Symbol s = stm.getSymbol("i");
		assertNotNull(s);
		assertEquals(INT, s.getType());
	}
	
	@Test //FIX
	public void declareArray()
	{
		DijkstraSymbolVisitor visitor = doSymbolTable("int [2] i");
		Symbol s = stm.getArray("i");
		assertNotNull(s);
		assertEquals(INT, s.getType());
	}
	
	@Test
	public void addThreeVariables()
	{
		DijkstraSymbolVisitor visitor = doSymbolTable("int i, j, k");
		Symbol s = stm.getSymbol("i");
		assertNotNull(s);
		assertEquals(INT, s.getType());
		s = stm.getSymbol("j");
		assertNotNull(s);
		assertEquals(INT, s.getType());
		s = stm.getSymbol("k");
		assertNotNull(s);
		assertEquals(INT, s.getType());
	}
	
	@Test
	public void inferredVariable()
	{
		doSymbolTable("a <- 1");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
	}
	
	@Test
	public void inferredVariableInput()
	{
		doSymbolTable("input a;");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(UNDEFINED, s.getType());
	}
	
	@Test
	public void inferredVariables()
	{
		doSymbolTable("a, b, c <- 1, true, 2.0");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(BOOLEAN, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(FLOAT, s.getType());
	}

	@Test
	public void inputVariables()
	{
		doSymbolTable("input a, b");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(UNDEFINED, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(UNDEFINED, s.getType());
	}
	
	@Test
	public void orExpr()
	{
		doSymbolTable("a <- true | false");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void andExpr()
	{
		doSymbolTable("a <- true & false");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void equalExpr()
	{
		doSymbolTable("a <- true = true");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}

	@Test
	public void inferByMult()
	{
		doSymbolTable("a <- 2 * 3");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(NUM, s.getType());
	}

	@Test
	public void inferByAdd()
	{
		doSymbolTable("a <- 1 + 2");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(NUM, s.getType());
	}

	
	@Test
	public void inferRelational()
	{
		doSymbolTable("a <- 1 < 2");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void inferUnaryMinus()
	{
		doSymbolTable("a <- -2");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(NUM, s.getType());
	}
	
	@Test
	public void inferUnaryNot()
	{
		doSymbolTable("a <- ~false");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void changeScopeAtProcedureDecl()
	{
		doSymbolTable("int b; proc foo () { boolean b; int c;}");
		Symbol s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(INT, s.getType());
		
		SymbolTable procTable = stm.getSymbolTable(1);
		s = procTable.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(BOOLEAN, s.getType());
		
		s = stm.getSymbol("c");
		assertNull(s);
	}
	
	@Test
	public void changeScopeAtFunctionDecl()
	{
		doSymbolTable("int b; fun foo () : int{ boolean b;}");
		Symbol s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(INT, s.getType());
		
		s = stm.getFunction("foo");
		assertNotNull(s);
		assertEquals("foo", s.getId());
		assertEquals(INT, s.getType());
		
		
		SymbolTable procTable = stm.getSymbolTable(1);
		s = procTable.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void defineVariablesForProcedureCall()
	{
		doSymbolTable("int a; proc foo (boolean a) { print false; }");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		
		s = stm.getFunction("foo");
		assertNotNull(s);
		assertEquals("foo", s.getId());
		assertEquals(UNDEFINED, s.getType());
		
		SymbolTable procTable = stm.getSymbolTable(1);
		s = procTable.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void defineVariablesForCompoundBlock()
	{
		doSymbolTable("int a; { boolean a; }");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		
		SymbolTable procTable = stm.getSymbolTable(1);
		s = procTable.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());

	}
	
	@Test
	public void recursiveFunction()
	{
		doSymbolTable("fun fib(n): int { if n <= 2 :: return n n > 1 :: return fib(n - 2) + fib(n - 1) fi }");
		Symbol s = stm.getFunction("fib");
		assertNotNull(s);
		assertEquals("fib", s.getId());
		assertEquals(INT, s.getType());
	}
	
	@Test
	public void functionAndVar()
	{
		doSymbolTable("x <- true; fun x(n): int { return 2 }");
		Symbol s = stm.getFunction("x");
		assertNotNull(s);
		assertEquals("x", s.getId());
		assertEquals(INT, s.getType());
		
		s = stm.getSymbol("x");
		assertNotNull(s);
		assertEquals("x", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void defineVariablesForProcedureCall2()
	{
		doSymbolTable("int a; proc foo (a) { b <- a; }");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		
		
		SymbolTable procTable = stm.getSymbolTable(1);
		s = procTable.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(UNDEFINED, s.getType());
		
		s = procTable.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(UNDEFINED, s.getType());
	}
	
	// Helper methods
	private void makeParser(String inputText)
	{
		parser = DijkstraFactory.makeParser(new ANTLRInputStream(inputText));
	}

	/**
	 * This method performs the parse. If you want to see what the tree looks like, use
	 * 		<br><code>System.out.println(tree.toStringTree());<code></br>
	 * after calling this method.
	 * @param inputText the text to parse
	 */
	private String doParse(String inputText)
	{
		makeParser("program test " + inputText);
		tree = parser.dijkstraText();
		assertTrue(true);
		return tree.toStringTree(parser);
	}
	
	private DijkstraSymbolVisitor doSymbolTable(String inputText)
	{
		//System.out.println(doParse(inputText));
		stm.reset();
		doParse(inputText);
		DijkstraSymbolVisitor visitor = new DijkstraSymbolVisitor();
		tree.accept(visitor);
		return visitor;
	}

	//-------------------------------- Test visitor ---------------------------------//
	/**
	 * This visitor is simply used to make sure that the annotations are able to be
	 * read from pass to pass.
	 * @version Feb 8, 2015
	 */
	class DijkstraTestSymbolVisitor extends DijkstraBaseVisitor<Void>
	{
		public ParseTreeProperty<Symbol> symbols;
		public ParseTreeProperty<DijkstraType> types;
		
		public DijkstraTestSymbolVisitor(ParseTreeProperty<Symbol> symbols, 
				ParseTreeProperty<DijkstraType> types, ParseTreeProperty<SymbolTable> symbolTables)
		{
			this.symbols = symbols;
			this.types = types;
		}
		
		@Override
		public Void visitDeclaration(DeclarationContext ctx) {
			Symbol symbol = symbols.get(ctx);
			System.out.println("From test visitor: " + symbol);
			System.out.println("My type: " + types.get(ctx));
			return null;
		}
	}
}
