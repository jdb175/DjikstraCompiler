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

/**
 * General runtime exception for the Dijkstra compiler.
 * @version Feb 14, 2015
 */
public class DijkstraException extends RuntimeException
{
	
	/**
	 * @see java.lang.RuntimeException#RuntimeException()
	 */
	public DijkstraException(String msg)
	{
		super(msg);
	}
	
	/**
	 * @see java.lang.RuntimeException#RuntimeException(String, Throwable)
	 */
	public DijkstraException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
