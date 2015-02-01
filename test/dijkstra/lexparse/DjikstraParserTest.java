package dijkstra.lexparse;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Test;

import dijkstra.utility.DijkstraFactory;

public class DjikstraParserTest {
	private DijkstraParser parser;

	@Test
	public void testSimpleFloatDeclaration() {
		makeParser("program test float test_float;");
		parser.djikstraText();
		assertTrue(true);
	}
	
	@Test
	public void testSimpleIntDeclaration() {
		makeParser("program test int test_int;");
		parser.djikstraText();
		assertTrue(true);
	}
	
	@Test
	public void testSimpleBooleanDeclaration() {
		makeParser("program test boolean test_bool;");
		parser.djikstraText();
		assertTrue(true);
	}
	
	@Test
	public void testMultipleVariableDeclaration() {
		makeParser("program test boolean test_bool, test_bool2;");
		parser.djikstraText();
		assertTrue(true);
	}
	
	@Test
	public void testThreeVariableDeclaration() {
		makeParser("program test boolean test_bool, test_bool2, test_bool3;");
		parser.djikstraText();
		assertTrue(true);
	}
	
	@Test
	public void testTwoMultivariableDeclarations() {
		makeParser("program test boolean test_bool, test_bool2, test_bool3; int test_int, test_int2, test_int3;");
		parser.djikstraText();
		assertTrue(true);
	}
	
	@Test
	public void testTwoMultivariableDeclarationsNoSemicolon() {
		makeParser("program test boolean test_bool, test_bool2, test_bool3 int test_int, test_int2, test_int3");
		parser.djikstraText();
		assertTrue(true);
	}
	
	@Test
	public void testBasicArrayDeclaration() {
		makeParser("program test boolean [1] tset_arr");
		parser.djikstraText();
		assertTrue(true);
	}
	
	@Test
	public void testIntIsPrimaryExpression() {
		makeParser("1234");
		parser.primaryexpression();
		assertTrue(true);
	}
	
	@Test
	public void testBooleansArePrimaryExpression() {
		makeParser("true");
		parser.primaryexpression();
		makeParser("false");
		parser.primaryexpression();
		assertTrue(true);
	}
	
	@Test
	public void testIDIsPrimaryExpression() {
		makeParser("abcd");
		parser.primaryexpression();
		assertTrue(true);
	}
	
	@Test
	public void testPrimaryExpressionInParentheses() {
		makeParser("(abcd)");
		parser.primaryexpression();
		assertTrue(true);
	}
	
	@Test
	public void testFloat() {
		makeParser("4.0");
		parser.floatconstant();
		assertTrue(true);
	}
	
	@Test
	public void testUnaryNot() {
		makeParser("~ true");
		parser.unaryexpression();
		assertTrue(true);
	}
	
	@Test
	public void testUnaryNegative() {
		makeParser("- 4");
		parser.unaryexpression();
		assertTrue(true);
	}
	
	@Test
	public void testUnaryNested() {
		makeParser("- - 4");
		parser.unaryexpression();
		assertTrue(true);
	}
	
	@Test
	public void testUnaryNestedParens() {
		makeParser("- ( - 4)");
		parser.unaryexpression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicMultiply() {
		makeParser("1 * 4");
		parser.multiplicativeexpression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicDivid() {
		makeParser("1 / 4");
		parser.multiplicativeexpression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicMod() {
		makeParser("1 mod 4");
		parser.multiplicativeexpression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicDiv() {
		makeParser("1 div 4");
		parser.multiplicativeexpression();
		assertTrue(true);
	}
	
	@Test
	public void testMultNegative() {
		makeParser("1 div - 4");
		parser.multiplicativeexpression();
		assertTrue(true);
	}
	
	@Test
	public void testChainedMult() {
		makeParser("1 * - 4.0 / 2");
		assertEquals(parser.multiplicativeexpression().getText(), "1*-4.0/2");
	}
	
	@Test
	public void testMultiplicativeArrayDeclaration() {
		makeParser("boolean [1*3.3] test_arr");
		parser.arraydeclaration();
		assertTrue(true);
	}
	
	@Test
	public void testAddition() {
		makeParser("1 + 1");
		parser.additiveexpression();
		assertTrue(true);
	}
	
	@Test
	public void testSubtraction() {
		makeParser("1 - 1");
		parser.additiveexpression();
		assertTrue(true);
	}
	
	@Test
	public void testChainedSubtraction() {
		makeParser("1 - 1 - 3");
		assertEquals(parser.additiveexpression().getText(), "1-1-3");
	}
	
	@Test
	public void testChainedSubtractionWithMultipication() {
		makeParser("1 - 1 - 3 * 5");
		assertEquals(parser.additiveexpression().getText(), "1-1-3*5");
	}
	
	
	@Test
	public void testAdditiveArrayDeclaration() {
		makeParser("boolean [1+3] test_arr");
		parser.arraydeclaration();
		assertTrue(true);
	}
	
	@Test
	public void testGreaterThan() {
		makeParser("4 > 3");
		parser.relationalexpression();
		assertTrue(true);
	}
	
	@Test
	public void testLessThan() {
		makeParser("4 < 3");
		parser.relationalexpression();
		assertTrue(true);
	}
	
	@Test
	public void testLessThanOrEqual() {
		makeParser("4 <= 3");
		parser.relationalexpression();
		assertTrue(true);
	}
	
	@Test
	public void testGreaterThanOrEqual() {
		makeParser("4 >= 3");
		parser.relationalexpression();
		assertTrue(true);
	}
	
	@Test
	public void testGreaterThanOrEqualExpressions() {
		makeParser("4 + 4 >= 3 * 4");
		assertEquals(parser.relationalexpression().getText(), "4+4>=3*4");
	}
	
	@Test
	public void testGreaterThanOrEqualExpressionsParens() {
		makeParser("(4 + 4 >= 3 * 4)");
		assertEquals(parser.relationalexpression().getText(), "(4+4>=3*4)");
	}
	
	@Test
	public void testEquals() {
		makeParser("1 = 1");
		parser.equalityexpression();
		assertTrue(true);
	}
	
	@Test
	public void testNotEquals() {
		makeParser("1 ~= 2");
		parser.equalityexpression();
		assertTrue(true);
	}
	
	@Test
	public void testEqualsRelationals() {
		makeParser("(1 > 2) = (2 < 1)");
		parser.equalityexpression();
		assertTrue(true);
	}
	
	@Test
	public void testEqualsParens() {
		makeParser("((1 > 2) = (2 < 1))");
		parser.equalityexpression();
		assertTrue(true);
	}
	
	@Test
	public void testBasicAnd() {
		makeParser("true & false");
		parser.logicalandexpression();
		assertTrue(true);
	}
	
	@Test
	public void testExpressionsAnd() {
		makeParser("(1 > 2) & (3 < 5)");
		parser.logicalandexpression();
		assertTrue(true);
	}
	
	@Test
	public void testExpressionsAndChained() {
		makeParser("(1 > 2) & (3 < 5) & false");
		parser.logicalandexpression();
		assertTrue(true);
	}
	
	@Test
	public void andExprParens() {
		makeParser("(true & false)");
		parser.logicalandexpression();
		assertTrue(true);
	}
	
	@Test
	public void testOr() {
		makeParser("true | false");
		parser.logicalorexpression();
		assertTrue(true);
	}
	
	@Test
	public void testOrExprs() {
		makeParser("(true & false) | (1 < 3)");
		parser.logicalorexpression();
		assertTrue(true);
	}
	
	@Test
	public void testOrExprsParens() {
		makeParser("((true & false) | (1 < 3))");
		parser.logicalorexpression();
		assertTrue(true);
	}
	
	@Test
	public void testOrChained() {
		makeParser("true | false | true");
		parser.logicalorexpression();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCall() {
		makeParser("foo ()");
		parser.functioncall();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCallOneArg() {
		makeParser("bar(5)");
		parser.functioncall();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCallOneArgExpression() {
		makeParser("bar(5 + 3)");
		parser.functioncall();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCall2Arg() {
		makeParser("bar(4, 5 + 3)");
		parser.functioncall();
		assertTrue(true);
	}
	
	@Test
	public void testFunctionCall3Arg() {
		makeParser("bar(4, 5 + 3, (11))");
		parser.functioncall();
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessor() {
		makeParser("bar[0]");
		parser.arrayaccessor();
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorExpression() {
		makeParser("bar[(0+2)]");
		parser.arrayaccessor();
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorFunction() {
		makeParser("bar[foo()]");
		parser.arrayaccessor();
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorNested() {
		makeParser("bar[foo[1]]");
		parser.arrayaccessor();
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
		parser.procedurecall();
		assertTrue(true);
	}
	
	@Test
	public void testProcedureCallWithArg() {
		makeParser("foo(6)");
		parser.procedurecall();
		assertTrue(true);
	}
	
	@Test
	public void testProcedureCallWithArgExpr() {
		makeParser("foo(bar[6])");
		parser.procedurecall();
		assertTrue(true);
	}
	
	@Test
	public void testProcedureCallWithArgList() {
		makeParser("foo(bar[6], 4)");
		parser.procedurecall();
		assertTrue(true);
	}
	
	@Test
	public void testReturnStatement() {
		makeParser("return");
		parser.returnstatement();
		assertTrue(true);
	}
	
	@Test
	public void testReturnExpression() {
		makeParser("return (1+2)");
		assertEquals(parser.returnstatement().getText(), "return(1+2)");
	}
	
	@Test
	public void testReturnExpressionList() {
		makeParser("return (1+2), b");
		assertEquals(parser.returnstatement().getText(), "return(1+2),b");
	}
	
	@Test
	public void testBasicGuard() {
		makeParser("x > 3 :: return false");
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
	public void testGuardList() {
		makeParser("true :: foo()");
		parser.guardedstatementlist();
		assertTrue(true);
	}
	
	@Test
	public void testGuardList2() {
		makeParser("true :: foo() false :: bar()");
		assertEquals(parser.guardedstatementlist().getText(), "true::foo()false::bar()");
	}
	
	
	// Helper methods
		private void makeParser(String text)
		{
			parser = DijkstraFactory.makeParser(new ANTLRInputStream(text));
		}

}
