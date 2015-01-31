package dijkstra.lexparse;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Test;

import dijkstra.utility.DijkstraFactory;

public class DjikstraParserTest {
	private DijkstraParser parser;

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	// Helper methods
		private void makeLexer(String text)
		{
			parser = DijkstraFactory.makeParser(new ANTLRInputStream(text));
		}

}
