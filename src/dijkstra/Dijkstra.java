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

package dijkstra;

import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.Scanner;

import org.antlr.v4.runtime.*;

import dijkstra.codegen.CodeGenVisitor;
import dijkstra.lexparse.DijkstraParser;
import dijkstra.semantic.DijkstraTypeCheckVisitor;
import dijkstra.semantic.DjikstraTypeFinalizerVisitor;
import dijkstra.semantic.DjikstraTypeResolutionVisitor;
import dijkstra.symbol.DijkstraSymbolVisitor;
import dijkstra.utility.DijkstraFactory;

/**
 * This is the driver for the Dijkstra compiler.
 * @version October 22, 2012
 */
public class Dijkstra
{
	private static String programName = new String();
	private static String customPackage = "djkcode";	// default
	private static String outputDirectory = ".";		// default
	private static String fileContents = null;
	
	
	/**
	 * Main program to run compiler.
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		String fileName = parseArgs(args);
		//read the input file containing the actual Dijkstra code
		try
		{
			fileContents = new Scanner(new File(fileName)).useDelimiter("\\A").next();
		}
		catch(Exception e)
		{
			System.out.println("Either no input file was specified or it could not be read.");
			showHelp();
			System.exit(1);
		}
		
		doCompile();
	}

	
	/**
	 * Parser for arguments to the main function
	 * @param args Array of strings
	 * @return name of the source file to compile from
	 */
	static private String parseArgs(String[] args)
	{
		int i = 0;
		String s = null;
		while (i < args.length) {
			s = args[i++];
			if (s.equals("-h")) {
				showHelp();
				System.exit(0);
			}
			else if (s.equals("-o")) {
				outputDirectory = args[i++];
			}
			else if (s.equals("-p")) {
				customPackage = args[i++];
			}
		}
		return s;
	}
	
	/**
	 * Prints help information
	 */
	static private void showHelp()
	{
		System.out.println("Arguments: [-h] [-cp CLASSPATH] [-p PACKAGE_NAME] sourceFile");
		System.out.println("Takes in Base Dijkstra code written in sourceFile and prints compiled code to the screen.\n" +
				"\t-h Show this text\n" +
				"\t-o<directory> compiles to the specified directory\n" +
				"\t-p<package> Sets the package to <package>\n");
	}
	
	static private void doCompile()
	{
		//Parse
		DijkstraParser parser = DijkstraFactory.makeParser(new ANTLRInputStream(fileContents));
		ParserRuleContext tree = parser.dijkstraText();
		DijkstraSymbolVisitor visitor = new DijkstraSymbolVisitor();
		tree.accept(visitor);
		
		//Typecheck
		DjikstraTypeResolutionVisitor resolver = new DjikstraTypeResolutionVisitor(visitor);
		while(!resolver.isComplete()) {
			tree.accept(resolver);
		}
		DjikstraTypeFinalizerVisitor finalizer = new DjikstraTypeFinalizerVisitor(resolver);
		tree.accept(finalizer);
		DijkstraTypeCheckVisitor checker = new DijkstraTypeCheckVisitor(finalizer);
		tree.accept(checker);
		
		//Code generation
		CodeGenVisitor generator = new CodeGenVisitor(finalizer);
		//set package
		if (customPackage != null) {
			generator.setClassPackage(customPackage);
		}
		//compile
		byte[] code = tree.accept(generator);
		programName = generator.getProgramName();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(outputDirectory + "/" 
					+ customPackage + "/" + programName + ".class");
			fos.write(code);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
