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

package dijkstra.lexparse;

import static org.junit.Assert.*;
import org.antlr.v4.runtime.*;
import org.junit.Test;
import dijkstra.utility.DijkstraFactory;

/**
 * Tests used for assignment 1
 * @version Feb 15, 2015
 */
public class BaseDijkstraMasterParserTest
{
	private DijkstraParser parser;
	private ParserRuleContext tree;
	
	@Test
	public void minimalprogram()
	{
		doParse("int i");
	}
	
	@Test
	public void twoDeclarationProgram()
	{
		doParse("int i boolean b");
	}
	
	@Test
	public void singleAssignmentProgram()
	{
		doParse("i <- 1");
	}
	
	@Test
	public void declarationFollowedBySemicolon()
	{
		doParse("int i;");
	}
	
	@Test
	public void assignStatement()
	{
		doParse("a <- 42");
	}
	
	@Test
	public void assignmentStatementFollowedBySemicolon()
	{
		doParse("a <- 42;");
	}
	
	@Test
	public void inputStatement()
	{
		doParse("input x");
	}
	
	@Test
	public void inputStatementFollowedBySemicolon()
	{
		doParse("input x;");
	}
	
	@Test
	public void outputStatement()
	{
		doParse("print 1");
	}
	
	@Test
	public void outputStatementFollowedBySemicolon()
	{
		doParse("print 2;");
	}
	
	@Test
	public void compoundStatement()
	{
		doParse("{ print 3; }");
	}
	
	@Test
	public void compoundStatementWithDeclaration()
	{
		doParse("{ int i i <- 3 }");
	}
	
	@Test
	public void singleGuard()
	{
		doParse("if b :: print 1 fi");
	}
	
	
	@Test
	public void arithmeticPrecedence1()
	{
		doParse("a <- a + b * c");
	}
	
	@Test
	public void arithmeticPrecedence2()
	{
		doParse("a <- a * b + c");
	}
	
	@Test
	public void parenthesizedExpression()
	{
		doParse("a <- a * (b + c)");
	}
	
	@Test
	public void guardWithCompoundStatement()
	{
		final String text = "input a "
				+ "if"
				+ "  a < 1 :: { b <- 6; print b} "
				+ "  ~(a < 1) :: { b <- 5; print a * b}"
				+ "fi";
		doParse(text);
	}
	
	// Base Dijkstra tests
	@Test
	public void floatingPointValues()
	{
		doParse("f <- 1.3; f1 <- -2.4");
	}
	
	@Test
	public void multipleDeclarations()
	{
		doParse("int a, b, c; float f1, f2; boolean b1, b2");
	}
	
	@Test
	public void multipleAssignments()
	{
		doParse("i, b, f <- 1, false, -2.42");
	}
	
	@Test
	public void iterativeStatement()
	{
		String text = "input a "
				+ "do "
				+ "   a <= 5 :: print 1"
				+ "   a > 5 :: { b <- 3+a; print b }"
				+ "od";
		doParse(text);
	}
	
	@Test
	public void logicalExpressions()
	{
		doParse("a <- 1 < 5 | 5 < 1 & 3 > 2");
	}
	
	@Test
	public void modDiv()
	{
		doParse("a <- 3 mod 5; c <- 5 div 3");
	}
	
	@Test(expected=DijkstraParserException.class)
	public void badLogicalAnd()
	{
		doParse("x <- 5 && 3");
	}
	
	@Test
	public void validDuplicateOperators()
	{
		doParse("i <- 3 ----2;");
	}
	
	@Test(expected=DijkstraParserException.class)
	public void badUseOfReservedWord()
	{
		doParse("x <- a + div");
	}
	
	@Test(expected=DijkstraParserException.class)
	public void badFloat()
	{
		doParse("x <- .42");
	}

	// Helper methods
	private void makeParser(String inputText)
	{
		parser = DijkstraFactory.makeParser(new ANTLRInputStream(inputText));
	}

	/**
	 * This method performs the parse. If you want to see what the tree looks like, use
	 * 		System.out.println(tree.toStringTree());
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
}