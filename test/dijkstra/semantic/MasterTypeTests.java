package dijkstra.semantic;

import static org.junit.Assert.*;

import org.junit.Test;

import dijkstra.utility.TypeCheckRunner;

public class MasterTypeTests {
	@Test
	public void oneOfEachShoudPass()
	{
		doMasterTypeCheck("a <- 1 b <- true c <- 3.7");
	}

	@Test(expected=DijkstraSemanticException.class)
	public void floatInMod()
	{
		doMasterTypeCheck("a <- 3 mod 1.2");
	}

	@Test(expected=DijkstraSemanticException.class)
	public void floatInDiv()
	{
		doMasterTypeCheck("a <- 3 div 1.2");
	}

	@Test(expected=DijkstraSemanticException.class)
	public void cannotDetermineType()
	{
		doMasterTypeCheck("input a print a");
	}

	@Test(expected=Exception.class)
	public void simplePrint()
	{
		doMasterTypeCheck("print a");
	}

	@Test
	public void varsWithDifferentTypesInSeparateScopes()
	{
		String text = "input i\n"
				+ "if\n"
				+ "  i < 0 :: { a <- 5 }\n"
				+ "  i = 0 :: { a <- true }\n"
				+ "  i > 0 :: { a <- 3.7 }\n"
				+ "fi";
		doMasterTypeCheck(text);
	}

	@Test(expected=DijkstraSemanticException.class)
	public void sameScopeInIf()
	{
		String text = "input i\n"
				+ "if\n"
				+ "  i < 0 ::  a <- 5 \n"
				+ "  i = 0 ::  a <- true \n"
				+ "  i > 0 ::  a <- 3.7 \n"
				+ "fi";
		doMasterTypeCheck(text);
	}


	@Test(expected=Exception.class)
	public void printUnknownVar()
	{
		String text = "input i\n"
				+ "if\n"
				+ "  i < 0 :: { a <- 5 }\n"
				+ "  i = 0 :: { a <- true }\n"
				+ "  i > 0 :: { a <- 3.7 }\n"
				+ "fi\n"
				+ "print a";
		doMasterTypeCheck(text);
	}

	@Test(expected=DijkstraSemanticException.class)
	public void useIntAsBooleanExpression()
	{
		doMasterTypeCheck("i <- 1; if i :: print false fi");
	}

	@Test(expected=Exception.class)
	public void expressionTypeMismatch1()
	{
		doMasterTypeCheck("input a; c <- 3.2 * a; b <- ~a");
	}

	@Test(expected=DijkstraSemanticException.class)
	public void badSwap()
	{
		doMasterTypeCheck("a <- 0; b <- true; a, b <- b, a");
	}

	@Test
	public void intInDivision()
	{
		doMasterTypeCheck("a, b <- 0, 1.2; c <- a / b");
	}

	@Test(expected=Exception.class)
	public void impliedUseBeforeDef()
	{
		doMasterTypeCheck("a <- b; b <- 1.0");
	}

	@Test(expected=Exception.class)
	public void declaredUseBeforDef()
	{
		doMasterTypeCheck("a <- b; float b; b <- 1.0");
	}

	@Test(expected=DijkstraSemanticException.class)
	public void inferredFloatFromTheSpec()
	{
		doMasterTypeCheck("x <- 3 / 2; y <- x mod 5;");
	}

	@Test(expected=DijkstraSemanticException.class)
	public void unknownEqualsType()
	{
		doMasterTypeCheck("input a, b; c <- a = b");
	}

	@Test
	public void fibonacci()
	{
		String text =
				"\n"
				+ "  int f1\n"
				+ "  int f2\n"
				+ "  input n\n"
				+ "  f1 <- 1 f2 <- 1 "
				+ "  if\n"
				+ "    n < 3 :: print 1\n"
				+ "    n > 2 :: n <- n - 2\n"
				+ "  fi\n"
				+ "  do\n "
				+ "    n > 0 :: {"
				+ "      t <- f1\n"
				+ "      f1 <- f2\n"
				+ "      f2 <- t + f1\n"
				+ "      n <- n - 1"
				+ "    }\n"
				+ "  od\n"
				+ "  print f2";
		doMasterTypeCheck(text);
	}
	
	public void doMasterTypeCheck(String text) {
		TypeCheckRunner.check("program test " + text);
	}
}
