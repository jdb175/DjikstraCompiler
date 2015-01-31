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

package dijkstra.utility;

import org.antlr.v4.runtime.ANTLRInputStream;
import dijkstra.lexparse.DijkstraLexer;

/**
 * The DijkstraFactory is responsible for constructing all, or parts of a Dijkstra
 * compiler. It is a standard Factory class.
 * 
 * @version Jan 26, 2015
 */
public class DijkstraFactory
{
	/**
	 * Create a Dijkstra lexer using the specified input stream containing the text
	 * @param inputText the ANTLRInputStream that contains the program text
	 * @return the Dijkstra lexer
	 */
	static public DijkstraLexer makeLexer(ANTLRInputStream inputText) {
		return new DijkstraLexer(inputText);
	}
}
