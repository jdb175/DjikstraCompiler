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

package djikstra.semantic;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.*;

import dijkstra.lexparse.*;
import dijkstra.lexparse.DijkstraParser.*;
import dijkstra.symbol.DijkstraSymbolException;
import dijkstra.symbol.DijkstraSymbolVisitor;
import dijkstra.symbol.Symbol;
import dijkstra.symbol.SymbolTable;
import dijkstra.symbol.SymbolTableManager;
import dijkstra.utility.*;
import djikstra.semantic.DjikstraTypeFinalizerVisitor;
import djikstra.semantic.DjikstraTypeResolutionVisitor;
import static dijkstra.utility.DijkstraType.*;

/**
 * Description
 * @version Feb 7, 2015
 */
public class DijkstraTypeFinalizationTest
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
	public void basicInput() {
		doTypeFinalization("input a");
	}
	
	@Test(expected=DijkstraSymbolException.class)
	public void basicInputMulti() {
		doTypeFinalization("input a, b");
	}
	
	@Test
	public void basicDef() {
		doTypeFinalization("int a, b");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
		s = stm.getSymbol("b");
		assertNotNull(s);
		assertEquals("b", s.getId());
		assertEquals(INT, s.getType());
	}
	
	@Test(expected=DijkstraSymbolException.class)
	public void inferUnsureGuard()
	{
		doTypeFinalization("input a, b if a ~= b :: print a; a = b :: print b;  fi");
	}
	
	@Test
	public void convertIntFromNum() {
		doTypeFinalization("input a; b <- a < 2.0");
		Symbol s = stm.getSymbol("a");
		assertNotNull(s);
		assertEquals("a", s.getId());
		assertEquals(INT, s.getType());
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
	
	private DjikstraTypeFinalizerVisitor doTypeFinalization (String inputText)
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
		DjikstraTypeFinalizerVisitor finalizer = new DjikstraTypeFinalizerVisitor(resolver);
		tree.accept(finalizer);
		return finalizer;
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
