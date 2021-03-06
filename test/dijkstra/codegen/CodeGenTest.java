package dijkstra.codegen;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.Test;

import dijkstra.lexparse.DijkstraParser;
import dijkstra.runtime.DijkstraRuntime;
import dijkstra.semantic.DijkstraTypeCheckVisitor;
import dijkstra.semantic.DjikstraTypeFinalizerVisitor;
import dijkstra.semantic.DjikstraTypeResolutionVisitor;
import dijkstra.symbol.DijkstraSymbolVisitor;
import dijkstra.symbol.SymbolTableManager;
import dijkstra.utility.DijkstraException;
import dijkstra.utility.DijkstraFactory;

public class CodeGenTest extends ClassLoader {
	private DijkstraParser parser;
	private ParserRuleContext tree;
	private SymbolTableManager stm = SymbolTableManager.getInstance();
	private byte[] code;

	@Test
	public void testPrintBool() throws Exception {
		runCode("print true;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("print false;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testPrintInt() throws Exception {
		runCode("print 1;");
		assertEquals("i=1", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testPrintFloat() throws Exception {
		runCode("print 1.0;");
		assertEquals("f=1.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testAssignF2I() throws Exception {
		runCode("int a; a <- 1.9; print a;");
		assertEquals("i=1", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void simultaneousAssign() throws Exception {
		runCode("a, b <- 1, 2; a, b <- b, a; print a;");
		assertEquals("i=2", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void simultaneousAssignArrays() throws Exception {
		runCode("int[1] a; int[1] b; a[0] <- 1; b[0] <- 2; a[0], b[0] <- b[0], a[0]; print a[0];");
		assertEquals("i=2", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void simultaneousAssignMix() throws Exception {
		runCode("int[1] a; a[0] <- 1; b <- 2; a[0], b <- b, a[0]; print a[0];");
		assertEquals("i=2", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testAssignI2F() throws Exception {
		runCode("float a; a <- 1; print a;");
		assertEquals("f=1.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testCastVar() throws Exception {
		runCode("float a; int b; b <- 1; a <- b; print a;");
		assertEquals("f=1.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testPrintAssignedVar() throws Exception {
		runCode("a <- 1.5; b <- 2; print a;");
		assertEquals("f=1.5", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testPrintAssignedVars() throws Exception {
		runCode("int a, b; a, b <- 1, 2; print b;");
		assertEquals("i=2", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testBasicInput() throws Exception {
		DijkstraRuntime.setInputs(new String[] {"true", "false"});
		runCode("boolean b, c; input b, c; print b;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		DijkstraRuntime.setInputs(new String[] {"1"});
		runCode("int b; input b; print b;");
		assertEquals("i=1", DijkstraRuntime.getLastMessage());
		DijkstraRuntime.setInputs(new String[] {"1"});
		runCode("float b; input b; print b;");
		assertEquals("f=1.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testUnaryMinus() throws Exception {
		runCode("int c; c <- - 1; print c;");
		assertEquals("i=-1", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testUnaryMinusFloat() throws Exception {
		runCode("float c; c <- - 1.0; print c;");
		assertEquals("f=-1.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testUnaryNot() throws Exception {
		runCode("boolean c; c <- ~true; print c;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testMultInt() throws Exception {
		runCode("a, b <- 3, 2; c <- a * b; print c;");
		assertEquals("i=6", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testDivideInt() throws Exception {
		runCode("a, b <- 3, 2; c <- a / b; print c;");
		assertEquals("f=1.5", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testDivideFloat() throws Exception {
		runCode("a, b <- 3.0, 2.0; c <- a / b; print c;");
		assertEquals("f=1.5", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testMod() throws Exception {
		runCode("a <- 3*7+5; print a mod 7;");
		assertEquals("i=5", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testDiv() throws Exception {
		runCode("a <- 3*7+5; print a div 7;");
		assertEquals("i=3", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testMultFloat() throws Exception {
		runCode("a, b <- 3.0, 2.0; c <- a * b; print c;");
		assertEquals("f=6.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testMultMix() throws Exception {
		runCode("a, b <- 3, 2.0; c <- a * b * 3 * 2; print c;");
		assertEquals("f=36.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testMultMixCompound() throws Exception {
		runCode("a, b <- 3, 2.0; c <- a * b * (3 * -2); print c;");
		assertEquals("f=-36.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testAddInt() throws Exception {
		runCode("a, b <- 3, 2; c <- a + b; print c;");
		assertEquals("i=5", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testSubtractInt() throws Exception {
		runCode("a, b <- 3, 2; c <- a - b; print c;");
		assertEquals("i=1", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testAddFloat() throws Exception {
		runCode("a, b <- 3.0, 2.0; c <- a + b; print c;");
		assertEquals("f=5.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testSubtractFloat() throws Exception {
		runCode("a, b <- 3.0, 2; c <- a - b; print c;");
		assertEquals("f=1.0", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 3.0, 2.0; c <- a - b; print c;");
		assertEquals("f=1.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testAddMix() throws Exception {
		runCode("a, b <- 3, 2.0; c <- a + (b + 3); print c;");
		assertEquals("f=8.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testAnd() throws Exception {
		runCode("a <- false & true; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testAndTrue() throws Exception {
		runCode("a <- true & true; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testAndFalses() throws Exception {
		runCode("a <- false & false; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testOr() throws Exception {
		runCode("a <- true | false; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testOrFalses() throws Exception {
		runCode("a <- false | false; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testOrTrues() throws Exception {
		runCode("a <- true | true; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testEqualityBoolean() throws Exception {
		runCode("a <- true = true; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a <- false = true; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a <- false = false; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a <- true ~= true; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a <- false ~= true; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a <- false ~= false; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testEqualityInt() throws Exception {
		runCode("a <- 1 = 1; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a <- 1 = 2; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a <- 1 ~= 1; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a <- 1 ~= 2; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testEqualityFloat() throws Exception {
		runCode("a <- 1.0 = 1.0; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a <- 1.0 = 2.0; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a <- 1.0 ~= 1.0; print a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a <- 1.0 ~= 2.0; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
	}

	@Test
	public void testRelationalInt() throws Exception {
		runCode("a, b <- 1, 2; print a < b;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1, 2; print a > b;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1, 2; print b > a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1, 2; print b < a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testRelationalIntEquals() throws Exception {
		runCode("a, b <- 1, 2; print a <= b;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1, 2; print a >= b;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1, 2; print b >= a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1, 2; print b <= a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1, 1; print b >= a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1, 1; print b <= a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testRelationalFloat() throws Exception {
		runCode("a, b <- 1.0, 2.0; print a < b;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1, 2.0; print a > b;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1.0, 2; print b > a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1.0, 2.0; print b < a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testRelationalFloatEquals() throws Exception {
		runCode("a, b <- 1, 2.0; print a <= b;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1.0, 2; print a >= b;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1.0, 2.0; print b >= a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1.0, 2.0; print b <= a;");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1.0, 1.0; print b >= a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
		runCode("a, b <- 1.0, 1.0; print b <= a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testNestedExprs() throws Exception {
		runCode("a <- 2.0; print -a < (2*4 + -100);");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testArrayAccessInt() throws Exception {
		runCode("int[1] a; a[0] <- 1; print a[0]");
		assertEquals("i=1", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testArrayAccessFloat() throws Exception {
		runCode("float[4] a; a[2] <- 1; print a[2]");
		assertEquals("f=1.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testArrayAccessBoolean() throws Exception {
		runCode("boolean[1] a; a[0] <- false; print a[0]");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void basicAlternative() throws Exception
	{
		runCode("b <- true a <- 1\n"
				+ "if\n"
				+ "  b :: a <- -5\n"
				+ "fi\n"
				+ "print a");
		assertEquals("i=-5", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void basicAlternativeOverload() throws Exception
	{
		runCode("b <- true a <- 1\n"
				+ "if\n"
				+ "  b :: a <- -5\n"
				+ "  b :: a <- 15\n"
				+ "fi\n"
				+ "print a");
		assertEquals("i=-5", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void secondAlternative() throws Exception
	{
		runCode("b <- true a <- 1\n"
				+ "if\n"
				+ "  ~b :: a <- 2\n"
				+ "  b :: a <- -5\n"
				+ "fi\n"
				+ "print a");
		assertEquals("i=-5", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void nestedAlternative() throws Exception
	{
		runCode("b <- true a <- 1\n"
				+ "if\n"
				+ "  ~b :: a <- 2\n"
				+ "  b :: if b :: a <- -5 fi\n"
				+ "fi\n"
				+ "print a");
		assertEquals("i=-5", DijkstraRuntime.getLastMessage());
	}
	
	@Test(expected=InvocationTargetException.class)
	public void basicAlternativeFail() throws Exception
	{
		runCode("b <- false a <- 1\n"
				+ "if\n"
				+ "  b :: a <- -5\n"
				+ "  b & true :: a <- -5\n"
				+ "fi\n"
				+ "print a");
	}
	
	@Test
	public void basicIterative() throws Exception
	{
		runCode("a <- 0\n"
				+ "do\n"
				+ "  a < 5 :: a <- a + 1\n"
				+ "  a < 10 :: a <- a + 4\n"
				+ "od\n"
				+ "print a");
		assertEquals("i=13", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void basicIterativeNone() throws Exception
	{
		runCode("a <- 0\n"
				+ "do\n"
				+ "  a > 5 :: a <- a + 1\n"
				+ "od\n"
				+ "print a");
		assertEquals("i=0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void iterativeNested() throws Exception
	{
		runCode("a <- 0 b <- 0 c <- 0\n"
				+ "do\n"
				+ "  a < 5 :: { a <- a + 1;  do b < a :: { b <- b + 1; c <- c + 2; } od }\n"
				+ "od\n"
				+ "print c");
		assertEquals("i=10", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void basicProcedureCall() throws Exception
	{
		runCode("proc foo() { print 30; } print 15; foo();");
		assertEquals("i=30", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void basicProcedureCallArgs() throws Exception
	{
		runCode("proc foo(int a, int b) { print a - b; } foo(10, 20);");
		assertEquals("i=-10", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void basicProcedureCallArgsOtherOrder() throws Exception
	{
		runCode("proc foo(int a, int b) { print b - a; print a - b; } foo(10, 20);");
		assertEquals("i=-10", DijkstraRuntime.getLastMessage());
	}
	
	
	@Test
	public void basicFunctionCall() throws Exception
	{
		runCode("fun foo() : int { return 30; } print 15; print foo();");
		assertEquals("i=30", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void basicFunctionCallArgsOtherOrder() throws Exception
	{
		runCode("fun foo(int a, int b) : int { print b - a; return a - b; } print foo(10, 20);");
		assertEquals("i=-10", DijkstraRuntime.getLastMessage());
	}
	
	
	@Test
	public void basicFunctionCallArray() throws Exception
	{
		runCode("fun foo() : int { int[1] b; b[0] <- 30; return b[0]; } print 15; print foo();");
		assertEquals("i=30", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void recursiveFunction() throws Exception
	{
		runCode("fun ret5(int a) : int { if a >= 5 :: return a; a < 5 :: return ret5(5); fi } print 15; print ret5(0);");
		assertEquals("i=5", DijkstraRuntime.getLastMessage());
	}
	
	@Test(expected=InvocationTargetException.class)
	public void functionNoReturn() throws Exception
	{
		runCode("fun noRet(int a) : int { print a; } a <- noRet(2);");
		assertEquals("i=5", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void basicFunctionCallArgs() throws Exception
	{
		runCode("fun foo(int a, int b) : int { return a - b; } print 15; print foo(10, 20);");
		assertEquals("i=-10", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void basicFunctionCallArgsCast() throws Exception
	{
		runCode("fun foo(int a, int b) : int { return a - b; } print 15; print foo(10.0, 20.0);");
		assertEquals("i=-10", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void procFunOverload() throws Exception
	{
		runCode("proc foo() { print true } fun foo() : boolean { return false } print foo();");
		assertEquals("b=false", DijkstraRuntime.getLastMessage());
	}	
	
	@Test
	public void procedureCallAccessLexicalScope() throws Exception
	{
		runCode("a <- 10 proc foo(int b) { print a - b; } foo(20);");
		assertEquals("i=-10", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void procedureCallAccessLexicalScopeArray() throws Exception
	{
		runCode("#comment\n int[1] a; a[0] <- 10 proc foo(int b) { print a[0] - b; } foo(20);");
		assertEquals("i=-10", DijkstraRuntime.getLastMessage());
	}

	/** Utility **/
	private void makeParser(String inputText)
	{
		parser = DijkstraFactory.makeParser(new ANTLRInputStream(inputText));
	}
	
	private String doParse(String inputText)
	{
		makeParser("program Test " + inputText);
		tree = parser.dijkstraText();
		assertTrue(true);
		return tree.toStringTree(parser);
	}
	
	private CodeGenVisitor doCodeGen (String inputText)
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
		DijkstraTypeCheckVisitor checker = new DijkstraTypeCheckVisitor(finalizer);
		tree.accept(checker);
		CodeGenVisitor generator = new CodeGenVisitor(finalizer);
		code = tree.accept(generator);
		return generator;
	}
	
	private void runCode(String inputText) throws Exception
	{
		doCodeGen(inputText);
		CodeGenTest loader = new CodeGenTest();
		Class<?> testClass = loader.defineClass("djkcode.Test", code, 0, code.length);

		// Run the dynamically generated class's main method.
		testClass.getMethods()[0].invoke(null, new Object[] { null });
	}

}
