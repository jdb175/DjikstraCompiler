package djikstra.semantic;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.Before;
import org.junit.Test;

import dijkstra.lexparse.DijkstraParser;
import dijkstra.symbol.DijkstraSymbolVisitor;
import dijkstra.symbol.SymbolTableManager;
import dijkstra.utility.DijkstraFactory;
import dijkstra.utility.TypeCheckRunner;
import djikstra.semantic.DijkstraTypeCheckVisitor;
import djikstra.semantic.DijkstraSemanticException;
import djikstra.semantic.DjikstraTypeResolutionVisitor;

public class DijkstaTypeCheckVisitorTest {
	private DijkstraParser parser;
	private ParserRuleContext tree;
	private SymbolTableManager stm = SymbolTableManager.getInstance();
	
	@Before
	public void setup() {
		stm.reset();
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void returnWrongType() {
		doTypeCheck("fun foo () : int { return true }");
	}
	
	@Test
	public void returnRightType() {
		doTypeCheck("fun foo () : int { return 4 }");
		assertTrue(true);
	}
	
	@Test
	public void returnRightTypeAssign() {
		doTypeCheck("int a; fun foo () : int { return 4 } a <- foo()");
		assertTrue(true);
	}
	
	@Test
	public void returnRightTypeAssignConvert() {
		doTypeCheck("int a; fun foo () : float { return 4.0 } a <- foo()");
		doTypeCheck("float a; fun foo () : int { return 4 } a <- foo()");
		assertTrue(true);
	}

	@Test
	public void functionCallGoodParams() {
		doTypeCheck("int a, b; fun foo (int a, float b) : int { return 4 } a <- foo(a, b)");
		doTypeCheck("float a, b; fun foo (int a, float b) : int { return 4 } a <- foo(a, b)");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void functionCallBadParams() {
		doTypeCheck("int a; fun foo (boolean a) : int { return 4 } b <- foo(a)");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void functionCallBadParamsPrimitive() {
		doTypeCheck("fun foo (boolean a) : int { return 4 } b <- foo(4)");
	}
	
	@Test
	public void procedureCallGoodParams() {
		doTypeCheck("int a, b; proc foo (int a, float b)  { b <- a } foo(a, b)");
		doTypeCheck("float a, b; proc foo (int a, float b)  { a <- b } foo(a, b)");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void procedureCallBadParams() {
		doTypeCheck("int a; proc foo (boolean a) { return 4 } foo(a)");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void procedureCallBadParamsPrimitive() {
		doTypeCheck("proc foo (boolean a) { return 4 } foo(4)");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badMinus() {
		doTypeCheck("boolean b; a <- - b");
	}
	
	
	@Test(expected=DijkstraSemanticException.class)
	public void badUnaryChain() {
		doTypeCheck("boolean b; a <- - - ~ 2");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badMinusPrimitive() {
		doTypeCheck("boolean b; a <- - true");
	}
	
	@Test
	public void goodMinus() {
		doTypeCheck("input b; a <- - b");
		doTypeCheck("a <- - 2");
		doTypeCheck("int b; a <- - - - b");
		assertTrue(true);
	}
	
	
	@Test(expected=DijkstraSemanticException.class)
	public void badNot() {
		doTypeCheck("int b; a <- ~ b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badNotPrimitive() {
		doTypeCheck("int b; a <- ~ 3");
	}
	
	@Test
	public void goodNot() {
		doTypeCheck("input b; a <- ~ b");
		doTypeCheck("boolean b; a <- ~ b");
		assertTrue(true);
	}
	
	@Test
	public void goodMult() {
		doTypeCheck("int a,b,c; a <- c * b");
		doTypeCheck("float a,b,c; a <- c * b");
		doTypeCheck("int a,b; float c; a <- c * b");
		doTypeCheck("int a; float b, c; a <- c * b");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badMult() {
		doTypeCheck("int a, b; boolean c; a <- c * b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badMultPrimitive() {
		doTypeCheck("int a, b; a <- false * b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badMultAssign() {
		doTypeCheck("boolean a; float b,c; a <- c * b");
	}
	
	@Test
	public void goodDivide() {
		doTypeCheck("int a,b,c; a <- c / b");
		doTypeCheck("float a,b,c; a <- c / b");
		doTypeCheck("int a,b; float c; a <- c / b");
		doTypeCheck("int a; float b, c; a <- c / b");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badDivide() {
		doTypeCheck("int a, b; boolean c; a <- c / b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badDivideAssign() {
		doTypeCheck("boolean a; float b,c; a <- c / b");
	}
	
	
	@Test(expected=DijkstraSemanticException.class)
	public void badDiv() {
		doTypeCheck("int a, b; float c; a <- c div b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badDivReturn() {
		doTypeCheck("int a, b; boolean c; c <- a div b");
	}
	
	@Test
	public void goodDiv() {
		doTypeCheck("int a, b; c <- a div b");
		doTypeCheck("int a, b; c <- a div (b+a)");
		doTypeCheck("int a, b; float c; c <- a div b");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badMod() {
		doTypeCheck("int a, b; float c; a <- c mod b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badModPrimitive() {
		doTypeCheck("int a, b; a <- 3.5 mod b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badModReturn() {
		doTypeCheck("int a, b; boolean c; c <- a mod b");
	}
	
	@Test
	public void goodMod() {
		doTypeCheck("int a, b; c <- a mod b");
		doTypeCheck("int a, b; float c; c <- a mod b");
		assertTrue(true);
	}
	
	/*from class*/	
	@Test(expected=DijkstraSemanticException.class)
	public void inferIntSecond()
	{
		doTypeCheck("input a, b, c; x <- a / b; y <- x mod c");
	}
	
	@Test
	public void goodAdd() {
		doTypeCheck("int a,b,c; a <- c + b");
		doTypeCheck("float a,b,c; a <- c + (b + c)");
		doTypeCheck("int a,b; float c; a <- c + b");
		doTypeCheck("int a; float b, c; a <- c + b");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badAdd() {
		doTypeCheck("int a, b; boolean c; a <- c + b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badAddPrimitive() {
		doTypeCheck("int a, b; a <- b + true");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badAddAssign() {
		doTypeCheck("boolean a; float b,c; a <- c + b");
	}
	
	@Test
	public void goodRelational() {
		doTypeCheck("int b, c; a <- c < b");
		doTypeCheck("float b, c; a <- c > b");
		doTypeCheck("float b; int c; a <- c >= (b + 2)");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badRelational() {
		doTypeCheck("int b; boolean c; a <- c > b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badRelationalPrimitive() {
		doTypeCheck("int b; boolean c; a <- c > true");
	}
	
	
	@Test(expected=DijkstraSemanticException.class)
	public void badRelationalAssign() {
		doTypeCheck("float a,b,c; a <- c < b");
	}
	
	@Test
	public void goodAnd() {
		doTypeCheck("boolean a, b; a <- a & (b & b)");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badAnd() {
		doTypeCheck("boolean a; int b; a <- a & b");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badAndPrimitive() {
		doTypeCheck("boolean a; int b; a <- a & 4");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badAndAssign() {
		doTypeCheck("int a; boolean b; a <- a & b");
	}
	
	@Test
	public void goodOr() {
		doTypeCheck("boolean a, b; a <- a | (b | true)");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badOr() {
		doTypeCheck("boolean a; a <- a | 5");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badOrAssign() {
		doTypeCheck("int a; boolean b; a <- a | (b | false)");
	}
	
	@Test
	public void goodEquals() {
		doTypeCheck("int a; c <- a = 2");
		doTypeCheck("float a, b; c <- a ~= (b + (2 + b))");
		doTypeCheck("boolean a, b; c <- a = (b & a)");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void badEquals() {
		doTypeCheck("int a; float b; c <- a = b");
	}
	
	@Test
	public void testArrayDeclaration() {
		doTypeCheck("int[1] a;");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testArrayDeclarationBad() {
		doTypeCheck("int[true] a;");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testArrayDeclarationBadExpr() {
		doTypeCheck("int[(4 < 5)] a;");
	}
	
	
	
	@Test
	public void testArrayAccessor() {
		doTypeCheck("int[1] a; int b; c <- a[0] = b");
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorWithSelf() {
		doTypeCheck("int[2] a; a[1] <- a[0]");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testArrayAccessorWithOther() {
		doTypeCheck("int[1] a; boolean[1] b; b[0] <- a[0]");
		assertTrue(true);
	}

	@Test
	public void testArrayAccessor2() {
		doTypeCheck("int[1] a; int b; c <- a[b + 0] = b");
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorGet() {
		doTypeCheck("int[1] a; int b; a[0] <- b");
		assertTrue(true);
	}
	
	@Test
	public void testArrayAccessorInExpression() {
		doTypeCheck("int[1] a; int b; c <- (a[0]) + b");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testArrayAccessorWrongAccessType() {
		doTypeCheck("int[1] a; c <- a[true]");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testArrayAccessorWrongAccessTypeExpr() {
		doTypeCheck("int[1] a; c <- a[(2 < 3)]");
	}
	
	@Test
	public void testAssignStatement() {
		doTypeCheck("a,b,c <- 5, 4.0, true");
		doTypeCheck("boolean[1] d; a,b,c <- 5, 4.0, d[0]");
		doTypeCheck("fun d() : boolean { return true; } a,b,c <- 5, 4.0, d()");
		doTypeCheck("int a; float b; boolean c; a,b,c <- 5, 4.0, true");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testAssignStatementWrongType() {
		doTypeCheck("int a; a <- true");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testAssignStatementWrongTypeFunction() {
		doTypeCheck("fun d() : boolean { return true; } int a; a <- d()");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testNestedFunctions() {
		doTypeCheck("fun a (boolean b) : boolean { return b } fun d() : boolean { return a(4.5); }");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testConditional() {
		doTypeCheck("if 4 :: print 5 fi");
	}
	
	@Test
	public void testConditionalGood() {
		doTypeCheck("if false :: print 5 fi");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testConditionalInternal() {
		doTypeCheck("if true :: a <- 4 | true fi");
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testIterative() {
		doTypeCheck("do 4 :: print 5 od");
	}
	
	@Test
	public void testIterativeGood() {
		doTypeCheck("do false :: print 5 od");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraSemanticException.class)
	public void testIterativeInternal() {
		TypeCheckRunner.check("program test do true :: a <- 4 | true od");
	}
	
	/*  HELPERS */
	
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
	
	private DijkstraTypeCheckVisitor doTypeCheck(String inputText)
	{
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
		DijkstraTypeCheckVisitor checker = new DijkstraTypeCheckVisitor(resolver);
		tree.accept(checker);
		return checker;
	}

}
