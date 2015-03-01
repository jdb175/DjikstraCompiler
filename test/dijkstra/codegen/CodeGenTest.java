package dijkstra.codegen;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.Test;

import dijkstra.lexparse.DijkstraParser;
import dijkstra.runtime.DijkstraRuntime;
import dijkstra.symbol.DijkstraSymbolVisitor;
import dijkstra.symbol.SymbolTableManager;
import dijkstra.utility.DijkstraException;
import dijkstra.utility.DijkstraFactory;
import djikstra.semantic.DjikstraTypeFinalizerVisitor;
import djikstra.semantic.DjikstraTypeResolutionVisitor;

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
		runCode("int a; a <- 1.4; print a;");
		assertEquals("i=1", DijkstraRuntime.getLastMessage());
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
	public void testAddFloat() throws Exception {
		runCode("a, b <- 3.0, 2.0; c <- a + b; print c;");
		assertEquals("f=5.0", DijkstraRuntime.getLastMessage());
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