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

package dijkstra.semantic;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.*;

import dijkstra.lexparse.*;
import dijkstra.lexparse.DijkstraParser.*;
import dijkstra.semantic.DijkstraSemanticException;
import dijkstra.semantic.DjikstraTypeResolutionVisitor;
import dijkstra.symbol.DijkstraSymbolException;
import dijkstra.symbol.DijkstraSymbolVisitor;
import dijkstra.symbol.Symbol;
import dijkstra.symbol.SymbolTable;
import dijkstra.symbol.SymbolTableManager;
import dijkstra.utility.*;
import static dijkstra.utility.DijkstraType.*;

/**
 * Description
 * @version Feb 7, 2015
 */
public class DijkstraTypeResolutionVisitorTest
{
	private DijkstraParser parser;
	private ParserRuleContext tree;
	private SymbolTableManager stm = SymbolTableManager.getInstance();
	
	@Before
	public void setup()
	{
		stm.reset();
	}
	
	@Test
	public void inferredVariableParens()
	{
		doSymbolTable("a <- (1)");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
	}
	
	@Test
	public void inferredVariableUseInArrayAccessor()
	{
		doSymbolTable("input a; int[0] b; c <- b[a];");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
	}
	
	@Test
	public void orExpr()
	{
		doSymbolTable("input a, b; d <- a | b");
		Symbol s = stm.getSymbol("d");
		assertNotNull(s);
		assertEquals("d", s.getId());
		assertEquals(BOOLEAN, s.getType());
		s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void andExpr()
	{
		doSymbolTable("input a, b; d <- a & b");
		Symbol s = stm.getSymbol("d");
		assertNotNull(s);
		assertEquals("d", s.getId());
		assertEquals(BOOLEAN, s.getType());
		s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void equalExpr()
	{
		doSymbolTable("float a; input b; c<- a = b");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(FLOAT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(FLOAT, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void resolve2Pass()
	{
		doSymbolTable("input a, b, c, d; c <- a = b; b <- d; d <- c");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(BOOLEAN, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void equalExprFunctionCall()
	{
		doSymbolTable("input a; fun foo() : int { return 6 } c<- a = foo()");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void equalExprFunctionCallBool()
	{
		doSymbolTable("input a; fun foo() : boolean { return 6 } c<- a = foo()");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void equalExprFunctionCallBoolParam()
	{
		//should a be int?
		doSymbolTable("input a; fun foo(a) : int { return a + 6 } c<- a = foo()");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(BOOLEAN, s.getType());
		
		SymbolTable procTable = stm.getSymbolTable(1);
		s = procTable.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(NUM, s.getType());
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void OrExprInts()
	{
		doSymbolTable("int a, b; d <- a | b");
	}
	
	@Test
	public void inferByMult()
	{
		doSymbolTable("input b, c; a <- b * c");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(NUM, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(NUM, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(NUM, s.getType());
	}
	
	@Test
	public void inferByDiv()
	{
		doSymbolTable("input b, c; a <- b div c");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(INT, s.getType());
	}
	@Test
	public void inferIntFirst()
	{
		doSymbolTable("input x, a, c; int b; y <- x mod c; y <- a / b; z <- y div b");
		Symbol s = stm.getSymbol("x");
		assertNotNull(s);
		assertEquals("x", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("y");
		assertNotNull(s);
		assertEquals("y", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(FLOAT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(INT, s.getType());
	}

	@Test
	public void inferByMod()
	{
		doSymbolTable("input b, c; a <- b mod c");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(INT, s.getType());
	}
	
	
	@Test
	public void inferByDivision()
	{
		doSymbolTable("input b, c; a <- b / c");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(FLOAT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(FLOAT, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(FLOAT, s.getType());
	}
	
	@Test
	public void inferByMultFloats()
	{
		doSymbolTable("int b; float c; a <- b * c");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(FLOAT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(FLOAT, s.getType());
	}
	
	@Test
	public void inferByAdd()
	{
		doSymbolTable("input b, c; a <- b + c");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(NUM, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(NUM, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(NUM, s.getType());
	}
	
	@Test
	public void inferByAddFloats()
	{
		doSymbolTable("float b, c; a <- b + c");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(FLOAT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(FLOAT, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(FLOAT, s.getType());
	}
	
	@Test
	public void inferRelational()
	{
		doSymbolTable("input b, c; a <- b < c");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(NUM, s.getType());
		s = stm.getSymbol("c");
		assertNotNull(s);
		assertEquals("c", s.getId());
		assertEquals(NUM, s.getType());
	}
	
	@Test
	public void inferGuard()
	{
		doSymbolTable("input a if a :: print a fi");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void twoVars() {
		doSymbolTable("input a, b; a <- 3; b <- 3.0;");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(FLOAT, s.getType());
	} 
	
	@Test
	public void functionParametersInfer() {
		doSymbolTable("input a, b; fun getTypes(a, boolean b, float c) : int { return 1 } a <- getTypes(a,b,1.0)");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
	@Test
	public void procParametersInfer() {
		doSymbolTable("input a, b; proc getTypes(int a, boolean b, float c) { if b :: print a + c fi } getTypes(a,b,1.0)");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(BOOLEAN, s.getType());
	}
	
/*	@Test(expected=DijkstraSymbolException.class)
	public void cantCallProcAsFun() {
		doSymbolTable("proc foo() { print 1 } a <- foo()");	
	}
	
	@Test(expected=DijkstraSymbolException.class)
	public void cantCallFunAsProc() {
		doSymbolTable("fun foo() : int { return 1 } foo()");	
	}*/
	
	
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
		DjikstraTypeResolutionVisitor resolver = new DjikstraTypeResolutionVisitor(visitor);
		while(!resolver.isComplete()) {
			tree.accept(resolver);
		}
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
