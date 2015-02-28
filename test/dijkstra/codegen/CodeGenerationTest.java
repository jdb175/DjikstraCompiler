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

public class CodeGenerationTest extends ClassLoader {
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
	public void testPrintAssignedI2F() throws Exception {
		runCode("int a; a <- 1.5; print a;");
		assertEquals("i=1", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testPrintAssigneF2I() throws Exception {
		runCode("float a; a <- 1; print a;");
		assertEquals("f=1.0", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testPrintAssignedVarI2F() throws Exception {
		runCode("int a; float b; b <- 1.5; a <- b; print a;");
		assertEquals("i=1", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testInputInt() throws Exception {
		DijkstraRuntime.setInputs(new String[] {"41"});
		runCode("int a; input a; print a;");
		assertEquals("i=41", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testInputBool() throws Exception {
		DijkstraRuntime.setInputs(new String[] {"true"});
		runCode("boolean a; input a; print a;");
		assertEquals("b=true", DijkstraRuntime.getLastMessage());
	}
	
	@Test
	public void testInputFloat() throws Exception {
		DijkstraRuntime.setInputs(new String[] {"41"});
		runCode("float a; input a; print a;");
		assertEquals("f=41.0", DijkstraRuntime.getLastMessage());
	}
	
	
	/*@Test(expected=DijkstraException.class)
	public void testWrongNumberofAssigns() throws Exception {
		runCode("int a, b; a <- 1, 2; print b;");
	}*/

	/** Utiity **/
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
		CodeGenerationTest loader = new CodeGenerationTest();
		Class<?> testClass = loader.defineClass("djkcode.Test", code, 0, code.length);

		// Run the dynamically generated class's main method.
		testClass.getMethods()[0].invoke(null, new Object[] { null });
	}

}
