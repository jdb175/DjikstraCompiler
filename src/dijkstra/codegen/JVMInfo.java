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

package dijkstra.codegen;

/**
 * Description
 * @version Feb 22, 2015
 */
public class JVMInfo
{
	private static int nextAddress = 1;
	private static int oldAddress = 1;
	
	public static int getNextAddress()
	{
		return nextAddress++;
	}
	
	public static void enterScope()
	{
		oldAddress = nextAddress;
		nextAddress = 0;
	}
	
	public static void exitScope()
	{
		nextAddress = oldAddress;
	}
}
