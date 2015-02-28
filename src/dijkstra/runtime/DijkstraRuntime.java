/*******************************************************************************
 * This file is used in CS4533/CS544, Compiler Construction & Techniques of Language Translation.
 * 
 * Copyright (c) 2012 Gary F. Pollice
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: gpollice
 *******************************************************************************/

package dijkstra.runtime;

import java.io.*;
import dijkstra.utility.DijkstraException;

/**
 * This class contains static methods that are called from the generated Dijkstra code.
 * 
 * @author gpollice
 * @version Oct 6, 2012
 */
public class DijkstraRuntime
{
	private static String lastMessage = null;		// used for testing
	private static String[] inputs = null;
	private static int inputIndex = 0;
	
	public static void printInt(int i)
	{
		lastMessage = "i=" + i;
		System.out.println("(int)> " + i);
	}
	
	public static void printFloat(float f)
	{
		lastMessage = "f=" + f;
		System.out.println("(float)> " + f);
	}

	public static void printBoolean(boolean b)
	{
		lastMessage = "b=" + b;
		System.out.println("(boolean)> " + b);
	}

	public static int inputInt(String varName)
	{
		int value = 0;
		String input;
		if (inputs == null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("int " + varName + " = ");
			System.out.flush();
			try {
				input = in.readLine();
			} catch (IOException e) {
				throw new RuntimeException(
						"I/O exception on reading from standard input.", e);
			}
		} else {
			input = inputs[inputIndex++];
		}
		value = Integer.parseInt(input.trim());
		return value;
	}

	public static boolean inputBoolean(String varName)
	{
		boolean value = false;
		String input;
		if (inputs == null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("boolean " + varName + " = ");
			System.out.flush();
			boolean accepting = true;
			while (accepting) {
				try {
					input = in.readLine().trim().toLowerCase();
					if (input.equals("true")) {
						value = true;
						accepting = false;
					} else if (input.equals("false")) {
						value = false;
						accepting = false;
					} else {
						System.out.println("Enter either 'true' or 'false'");
					}
				} catch (IOException e) {
					throw new DijkstraException(
							"I/O exception on reading from standard input.", e);
				}
			}
		} else {
			input = inputs[inputIndex++];
			value = input.equals("true") ? true : false;
		}
		return value;
	}
	
	public static void abortNoAlternative(int lineNo)
	{
		String msg = "No alternative evaluated to true (line " + lineNo + ")";
		System.err.println(msg);
		throw new DijkstraException(msg);
	}

	/**
	 * @return the lastMessage
	 */
	public static String getLastMessage()
	{
		return lastMessage;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public static void setInputs(String[] inputs)
	{
		DijkstraRuntime.inputs = inputs;
		inputIndex = 0;
	}
}
