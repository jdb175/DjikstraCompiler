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

import org.antlr.v4.runtime.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static dijkstra.lexparse.DijkstraLexer.*;
import dijkstra.utility.DijkstraFactory;

/**
 * Description
 * @version Jan 26, 2015
 */
public class DijkstraLexerTest
{
	private DijkstraLexer lexer;
	private Token t;
	
	@Test
	public void recognizeReservedWords()
	{
		makeLexer("boolean false fi if input int float print program proc fun true do od div mod return");
		checkNextToken(BOOLEAN, "boolean");
		checkNextToken(FALSE, "false");
		checkNextToken(FI, "fi");
		checkNextToken(IF, "if");
		checkNextToken(INPUT, "input");
		checkNextToken(INT, "int");
		checkNextToken(FLOAT, "float");
		checkNextToken(PRINT, "print");
		checkNextToken(PROGRAM, "program");
		checkNextToken(PROC, "proc");
		checkNextToken(FUN, "fun");
		checkNextToken(TRUE, "true");
		checkNextToken(DO, "do");
		checkNextToken(OD, "od");
		checkNextToken(DIV, "div");
		checkNextToken(MOD, "mod");
		checkNextToken(RETURN, "return");
	}
	
	@Test
	public void recognizeSeparators()
	{
		makeLexer(";():::[]{},.");
		checkNextToken(SEMICOLON, ";");
		checkNextToken(LPAR, "(");
		checkNextToken(RPAR, ")");
		checkNextToken(GUARD, "::");
		checkNextToken(COLON, ":");
		checkNextToken(LBRACK, "[");
		checkNextToken(RBRACK, "]");
		checkNextToken(LBRACE, "{");
		checkNextToken(RBRACE, "}");
		checkNextToken(COMMA, ",");
		checkNextToken(PERIOD, ".");
		nextToken();
		assertEquals(EOF, t.getType());
	}
	
	@Test
	public void recognizeAssignOp()
	{
		makeLexer("<-");
		checkNextToken(ASSIGN, "<-");
	}
	
	@Test
	public void recognizeEqualityOperators()
	{
		makeLexer("= ~=");
		checkNextToken(EQ, "=");
		checkNextToken(NEQ, "~=");
	}
	
	@Test
	public void recognizeRelationalOperators()
	{
		makeLexer("< > >= <=");
		checkNextToken(LT, "<");
		checkNextToken(GT, ">");
		checkNextToken(GTE, ">=");
		checkNextToken(LTE, "<=");
	}
	
	@Test
	public void recognizeArithmetOperators()
	{
		makeLexer("+-*/");
		checkNextToken(PLUS, "+");
		checkNextToken(MINUS, "-");
		checkNextToken(STAR, "*");
		checkNextToken(SLASH, "/");
	}
	
	@Test
	public void recognizeLogicalNot()
	{
		makeLexer("~");
		checkNextToken(TILDE, "~");
	}
	
	@Test
	public void recognizeBooleanOperators()
	{
		makeLexer("| &");
		checkNextToken(OR, "|");
		checkNextToken(AND, "&");
	}
	
	@Test
	public void recognizeSimpleIDs()
	{
		makeLexer("hello Test1 var_1 isOpen?");
		checkNextToken(ID, "hello");
		checkNextToken(ID, "Test1");
		checkNextToken(ID, "var_1");
		checkNextToken(ID, "isOpen?");
	}
	
	@Test
	public void recognizeIntegers()
	{
		makeLexer("42 12345678901234567890");
		checkNextToken(INTEGER, "42");
		checkNextToken(INTEGER, "12345678901234567890");
	}
	
	@Test
	public void recognizeMixed()
	{
		makeLexer("42hello world_215");
		checkNextToken(INTEGER, "42");
		checkNextToken(ID, "hello");
		checkNextToken(ID, "world_215");
	}
	
	// Helper methods
		private void makeLexer(String text)
		{
			lexer = DijkstraFactory.makeLexer(new ANTLRInputStream(text));
		}

		private void nextToken()
		{
			t = lexer.nextToken();
		}
		
		private void checkNextToken(int tokenType, String tokenText)
		{
			nextToken();
			assertEquals(tokenType, t.getType());
			assertEquals(tokenText, t.getText());
		}
}
