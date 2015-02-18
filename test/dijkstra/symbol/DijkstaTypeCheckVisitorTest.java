package dijkstra.symbol;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.Test;

import dijkstra.lexparse.DijkstraParser;
import dijkstra.utility.DijkstraFactory;

public class DijkstaTypeCheckVisitorTest {
	private DijkstraParser parser;
	private ParserRuleContext tree;
	private SymbolTableManager stm = SymbolTableManager.getInstance();
	
	@Test(expected=DijkstraTypeException.class)
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
	
	@Test(expected=DijkstraTypeException.class)
	public void functionCallBadParams() {
		doTypeCheck("int a; fun foo (boolean a) : int { return 4 } a <- foo(a)");
	}
	
	@Test
	public void procedureCallGoodParams() {
		doTypeCheck("int a, b; proc foo (int a, float b)  { b <- a } foo(a, b)");
		doTypeCheck("float a, b; proc foo (int a, float b)  { a <- b } foo(a, b)");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraTypeException.class)
	public void procedureCallBadParams() {
		doTypeCheck("int a; proc foo (boolean a) { return 4 } foo(a)");
	}
	
	@Test(expected=DijkstraTypeException.class)
	public void badMinus() {
		doTypeCheck("boolean b; a <- - b");
	}
	
	@Test
	public void goodMinus() {
		doTypeCheck("input b; a <- - b");
		doTypeCheck("int b; a <- - b");
		assertTrue(true);
	}
	
	
	@Test(expected=DijkstraTypeException.class)
	public void badNot() {
		doTypeCheck("int b; a <- ~ b");
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
	
	@Test(expected=DijkstraTypeException.class)
	public void badMult() {
		doTypeCheck("int a, b; boolean c; a <- c * b");
	}
	
	@Test(expected=DijkstraTypeException.class)
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
	
	@Test(expected=DijkstraTypeException.class)
	public void badDivide() {
		doTypeCheck("int a, b; boolean c; a <- c / b");
	}
	
	@Test(expected=DijkstraTypeException.class)
	public void badDivideAssign() {
		doTypeCheck("boolean a; float b,c; a <- c / b");
	}
	
	
	@Test(expected=DijkstraTypeException.class)
	public void badDiv() {
		doTypeCheck("int a, b; float c; a <- c div b");
	}
	
	@Test(expected=DijkstraTypeException.class)
	public void badDivReturn() {
		doTypeCheck("int a, b; boolean c; c <- a div b");
	}
	
	@Test
	public void goodDiv() {
		doTypeCheck("int a, b; c <- a div b");
		doTypeCheck("int a, b; float c; c <- a div b");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraTypeException.class)
	public void badMod() {
		doTypeCheck("int a, b; float c; a <- c mod b");
	}
	
	@Test(expected=DijkstraTypeException.class)
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
	@Test(expected=DijkstraTypeException.class)
	public void inferIntSecond()
	{
		doTypeCheck("input a, b, c; x <- a / b; y <- x mod c");
	}
	
	@Test
	public void goodAdd() {
		doTypeCheck("int a,b,c; a <- c + b");
		doTypeCheck("float a,b,c; a <- c + b");
		doTypeCheck("int a,b; float c; a <- c + b");
		doTypeCheck("int a; float b, c; a <- c + b");
		assertTrue(true);
	}
	
	@Test(expected=DijkstraTypeException.class)
	public void badAdd() {
		doTypeCheck("int a, b; boolean c; a <- c + b");
	}
	
	@Test(expected=DijkstraTypeException.class)
	public void badAddAssign() {
		doTypeCheck("boolean a; float b,c; a <- c + b");
	}
	
	
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
		//System.out.println(doParse(inputText));
		stm.reset();
		doParse(inputText);
		DijkstraSymbolVisitor visitor = new DijkstraSymbolVisitor();
		tree.accept(visitor);
		DijkstraResolutionVisitor resolver = new DijkstraResolutionVisitor(visitor);
		while(!resolver.isComplete()) {
			tree.accept(resolver);
		}
		DijkstraTypeCheckVisitor checker = new DijkstraTypeCheckVisitor(resolver);
		tree.accept(checker);
		return checker;
	}

}
