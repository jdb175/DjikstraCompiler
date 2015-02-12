package dijkstra.lexparse;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Test;

import dijkstra.utility.DijkstraFactory;

public class DijkstraParserTest {
	private DijkstraParser parser;

	@Test
	public void testSimpleFloatDeclaration() {
		makeParser("program test float test_float;");
		parser.dijkstraText();
		assertTrue(true);
	}
	
	@Test
	public void testSimpleIntDeclaration() {
		makeParser("program test int test_int;");
		parser.dijkstraText();
		assertTrue(true);
	}
	
	@Test
	public void testSimpleBooleanDeclaration() {
		makeParser("program test boolean test_bool;");
		parser.dijkstraText();
		assertTrue(true);
	}
	
	@Test
	public void testMultipleVariableDeclaration() {
		makeParser("program test boolean test_bool, test_bool2;");
		parser.dijkstraText();
		assertTrue(true);
	}
	
	@Test
	public void testThreeVariableDeclaration() {
		makeParser("program test boolean test_bool, test_bool2, test_bool3;");
		parser.dijkstraText();
		assertTrue(true);
	}
	
	@Test
	public void testTwoMultivariableDeclarations() {
		makeParser("program test boolean test_bool, test_bool2, test_bool3; int test_int, test_int2, test_int3;");
		parser.dijkstraText();
		assertTrue(true);
	}
	
	@Test
	public void testTwoMultivariableDeclarationsNoSemicolon() {
		makeParser("program test boolean test_bool, test_bool2, test_bool3 int test_int, test_int2, test_int3");
		parser.dijkstraText();
		assertTrue(true);
	}
	
	@Test
	public void testBasicArrayDeclaration() {
		makeParser("program test boolean [1] tset_arr");
		parser.dijkstraText();
		assertTrue(true);
	}
	
	@Test
	public void testIntIsPrimaryExpression() {
		makeParser("1234");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testBooleansArePrimaryExpression() {
		makeParser("true");
		parser.expression();
		makeParser("false");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testIDIsPrimaryExpression() {
		makeParser("abcd");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testPrimaryExpressionInParentheses() {
		makeParser("(abcd)");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testFloat() {
		makeParser("4.0");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testUnaryNot() {
		makeParser("~ true");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testUnaryNegative() {
		makeParser("- 4");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testUnaryNested() {
		makeParser("- - 4");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testUnaryNestedParens() {
		makeParser("- ( - 4)");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicMultiply() {
		makeParser("1 * 4");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicDivid() {
		makeParser("1 / 4");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicMod() {
		makeParser("1 mod 4");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicDiv() {
		makeParser("1 div 4");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testMultNegative() {
		makeParser("1 div - 4");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testChainedMult() {
		makeParser("1 * - 4.0 / 2");
		assertEquals(parser.expression().getText(), "1*-4.0/2");
	}
	
	@Test
	public void testMultiplicativeArrayDeclaration() {
		makeParser("boolean [1*3.3] test_arr");
		parser.arrayDeclaration();
		assertTrue(true);
	}
	
	@Test
	public void testAddition() {
		makeParser("1 + 1");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testSubtraction() {
		makeParser("1 - 1");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testChainedSubtraction() {
		makeParser("1 - 1 - 3");
		assertEquals(parser.expression().getText(), "1-1-3");
	}
	
	@Test
	public void testChainedSubtractionWithMultipication() {
		makeParser("1 - 1 - 3 * 5");
		assertEquals(parser.expression().getText(), "1-1-3*5");
	}
	
	
	@Test
	public void testAdditiveArrayDeclaration() {
		makeParser("boolean [1+3] test_arr");
		parser.arrayDeclaration();
		assertTrue(true);
	}
	
	@Test
	public void testGreaterThan() {
		makeParser("4 > 3");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testLessThan() {
		makeParser("4 < 3");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testLessThanOrEqual() {
		makeParser("4 <= 3");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testGreaterThanOrEqual() {
		makeParser("4 >= 3");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testGreaterThanOrEqualExpressions() {
		makeParser("4 + 4 >= 3 * 4");
		assertEquals(parser.expression().getText(), "4+4>=3*4");
	}
	
	@Test
	public void testGreaterThanOrEqualExpressionsParens() {
		makeParser("(4 + 4 >= 3 * 4)");
		assertEquals(parser.expression().getText(), "(4+4>=3*4)");
	}
	
	@Test
	public void testEquals() {
		makeParser("1 = 1");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testNotEquals() {
		makeParser("1 ~= 2");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testEqualsRelationals() {
		makeParser("(1 > 2) = (2 < 1)");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testEqualsParens() {
		makeParser("((1 > 2) = (2 < 1))");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicAnd() {
		makeParser("true & false");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testExpressionsAnd() {
		makeParser("(1 > 2) & (3 < 5)");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testExpressionsAndChained() {
		makeParser("(1 > 2) & (3 < 5) & false");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void andExprParens() {
		makeParser("(true & false)");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testOr() {
		makeParser("true | false");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testOrExprs() {
		makeParser("(true & false) | (1 < 3)");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testOrExprsParens() {
		makeParser("((true & false) | (1 < 3))");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testOrChained() {
		makeParser("true | false | true");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCall() {
		makeParser("foo ()");
		parser.functionCall();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCallOneArg() {
		makeParser("bar(5)");
		parser.functionCall();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCallOneArgExpression() {
		makeParser("bar(5 + 3)");
		parser.functionCall();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCall2Arg() {
		makeParser("bar(4, 5 + 3)");
		parser.functionCall();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCall3Arg() {
		makeParser("bar(4, 5 + 3, (11))");
		parser.functionCall();
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessor() {
		makeParser("bar[0]");
		parser.arrayAccessor();
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorExpression() {
		makeParser("bar[(0+2)]");
		parser.arrayAccessor();
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorFunction() {
		makeParser("bar[foo()]");
		parser.arrayAccessor();
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorNested() {
		makeParser("bar[foo[1]]");
		parser.arrayAccessor();
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorAsExpression() {
		makeParser("bar[0]");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCallAsExpression() {
		makeParser("foo()");
		parser.expression();
		assertTrue(true);
	}
	
	@Test
	public void testProcedureCall() {
		makeParser("foo()");
		parser.procedureCall();
		assertTrue(true);
	}
	
	@Test
	public void testProcedureCallWithArg() {
		makeParser("foo(6)");
		parser.procedureCall();
		assertTrue(true);
	}
	
	@Test
	public void testProcedureCallWithArgExpr() {
		makeParser("foo(bar[6])");
		parser.procedureCall();
		assertTrue(true);
	}
	
	@Test
	public void testProcedureCallWithArgList() {
		makeParser("foo(bar[6], 4)");
		parser.procedureCall();
		assertTrue(true);
	}
	
	@Test
	public void testReturnStatement() {
		makeParser("return");
		parser.returnStatement();
		assertTrue(true);
	}
	
	@Test
	public void testReturnExpression() {
		makeParser("return (1+2)");
		assertEquals(parser.returnStatement().getText(), "return(1+2)");
	}
	
	@Test
	public void testReturnExpressionList() {
		makeParser("return (1+2), b");
		assertEquals(parser.returnStatement().getText(), "return(1+2),b");
	}
	
	@Test
	public void testBasicGuard() {
		makeParser("x > 3 :: return false");
		parser.guard();
		assertTrue(true);
	}
	
	@Test
	public void testBasicGuardSemicolon() {
		makeParser("x > 3 :: return false;");
		parser.guard();
		assertTrue(true);
	}
	
	@Test
	public void testBasicGuardProcCall() {
		makeParser("true :: foo()");
		parser.guard();
		assertTrue(true);
	}
	
	@Test
	public void testInputStatement() {
		makeParser("input abc");
		parser.inputStatement();
		assertTrue(true);
	}
	
	@Test
	public void testInputStatementList() {
		makeParser("input abc, cba");
		assertEquals(parser.inputStatement().getText(), "inputabc,cba");
	}
	
	@Test
	public void testOutputStatement() {
		makeParser("print a");
		parser.outputStatement();
		assertTrue(true);
	}
	
	@Test
	public void testOutputStatementExpr() {
		makeParser("print (3+a)");
		parser.outputStatement();
		assertTrue(true);
	}
	
	@Test
	public void guardPrint() {
		makeParser("(true) :: print (3+a);");
		parser.guard();
		assertTrue(true);
	}
	
	@Test
	public void guardInput() {
		makeParser("(true) :: input avariable;");
		parser.guard();
		assertTrue(true);
	}
	
	@Test
	public void assignStatement() {
		makeParser("a <- 5");
		parser.assignStatement();
		assertTrue(true);
	}
	
	@Test
	public void assignExpr() {
		makeParser("a <- (1+2)");
		parser.assignStatement();
		assertTrue(true);
	}
	
	@Test
	public void assignArrayAccess() {
		makeParser("a[2] <- 5");
		parser.assignStatement();
		assertTrue(true);
	}
	
	@Test
	public void multiAssign() {
		makeParser("a[2], b <- 5, foo()");
		parser.assignStatement();
		assertTrue(true);
	}
	
	@Test
	public void guardAssign() {
		makeParser("(true) :: a <- 3;");
		parser.guard();
		assertTrue(true);
	}
	
	@Test
	public void compoundStatement() {
		makeParser("{print 3;}");
		parser.compoundStatement();
		assertTrue(true);
	}
	
	@Test
	public void compoundStatementMultiple() {
		makeParser("{print 3;input a;return foo(4)}");
		parser.compoundStatement();
		assertTrue(true);
	}
	
	@Test
	public void compoundDeclareVar() {
		makeParser("{int bar}");
		parser.compoundStatement();
		assertTrue(true);
	}
	
	@Test
	public void compoundDeclareArray() {
		makeParser("{int[4] bar}");
		parser.compoundStatement();
		assertTrue(true);
	}
	
	@Test
	public void guardCompound() {
		makeParser("(true) :: {return 4}");
		parser.guard();
		assertTrue(true);
	}
	
	@Test
	public void iterativeStatementBasic() {
		makeParser("do x < 4 :: return 4 od");
		parser.statement();
		assertTrue(true);
	}
	
	@Test
	public void iterativeStatementList () {
		makeParser("do x < 4 :: x <- 4; x >= 4 :: print 5 od");
		parser.iterativeStatement();
		assertTrue(true);
	}
	
	@Test
	public void alternativeStatementBasic() {
		makeParser("if x < 4 :: print 4 fi");
		parser.statement();
		assertTrue(true);
	}
	
	@Test
	public void alternativeStatementList() {
		makeParser("if x < 4 :: print 4; x >= 4 :: print 5 fi");
		parser.alternativeStatement();
		assertTrue(true);
	}
	
	@Test
	public void functiondeclaration() {
		makeParser("fun foo () : int { return 5 }");
		parser.declaration();
		assertTrue(true);
	}
	
	@Test
	public void functiondeclaration2line() {
		makeParser("fun foo () : int { print 4; return 5 }");
		parser.declaration();
		assertTrue(true);
	}
	
	@Test
	public void functiondeclaration2returns() {
		makeParser("fun foo () : int, boolean { return 5, true }");
		parser.declaration();
		assertTrue(true);
	}
	
	@Test
	public void functiondeclarationparameters() {
		makeParser("fun foo (a, b) : int, boolean { return 5, true }");
		parser.declaration();
		assertTrue(true);
	}
	
	@Test
	public void functiondeclarationparameterswithtype() {
		makeParser("fun foo (a, int b) : int, boolean { return 5, true }");
		parser.declaration();
		assertTrue(true);
	}
	
	@Test
	public void proceduredeclaration() {
		makeParser("proc foo () { print 5 }");
		parser.declaration();
		assertTrue(true);
	}
	
	@Test
	public void proceduredeclarationmultiline() {
		makeParser("proc foo () { print 5; print 6 }");
		parser.declaration();
		assertTrue(true);
	}
	
	@Test
	public void procdeclarationparameters() {
		makeParser("proc foo (a, boolean b) { print 5; print true }");
		parser.declaration();
		assertTrue(true);
	}
	
	@Test(expected=RuntimeException.class)
	public void procdeclarationBad() {
		makeParser("proc foo ( { print 5; print true }");
		parser.declaration();
		assertTrue(true);
	}
	
	public void programwithstatements() {
		makeParser("program foo boolean a, b, c; if a = b :: print c fi");
		parser.dijkstraText();
	}
	
	// Helper methods
		private void makeParser(String text)
		{
			parser = DijkstraFactory.makeParser(new ANTLRInputStream(text));
		}

}
