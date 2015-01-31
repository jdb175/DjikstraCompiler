/*******************************************************************************
 * Copyright (c) 2015 Gary F. Pollice
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Used in CS4533/CS544 at Worcester Polytechnic Institute
 *******************************************************************************/
 /*
  * This is the grammar for Toy Dijkstra
  */
 grammar Dijkstra;
 
 // Parser rules
 program :		EOF ;
 
 /** Lexical rules */
 
 // Separators and operators
 ASSIGN :		'<-' ;
 EQ :			'=' ;
 GT :			'>' ;
 GTE :			'>=' ;
 GUARD :		'::' ;
 COLON :		':' ;
 LPAR :			'(' ;
 LBRACK :		'[' ;
 LBRACE :		'{';
 LT :			'<' ;
 LTE :			'<=' ;
 MINUS :		'-' ;
 NEQ :			'~=' ;
 PLUS :			'+' ;
 RPAR :			')' ;
 RBRACK :		']' ;
 RBRACE :		'}' ;
 SEMICOLON :	';' ;
 SLASH :		'/' ;
 STAR :			'*' ;
 TILDE :		'~' ;
 COMMA :		',' ;
 OR :			'|' ;
 AND :			'&' ;
 PERIOD :		'.' ;
 
 // Reserved words
 BOOLEAN :		'boolean' ;
 FALSE :		'false' ;
 FI :			'fi' ;
 IF :			'if' ;
 INPUT :		'input' ;
 INT :			'int' ;
 FLOAT :		'float';
 PRINT :		'print' ;
 PROGRAM :		'program' ;
 PROC :			'proc' ;
 FUN :			'fun' ;
 TRUE :			'true' ;
 DO :			'do' ;
 OD :			'od' ; 
 DIV :			'div' ;
 MOD :			'mod' ;
 RETURN :		'return' ;
 
 // The rest
 ID : 			LETTER (LETTER|DIGIT|'_'|'?')* ;
 
 INTEGER : 		DIGIT+ ;
 
 WS :			[ \t\r\n]+ -> skip ;
 COMMENT :		'#' .*? ('\n'|EOF);
 
 fragment
 LETTER :		[A-Za-z] ;
 
 fragment
 DIGIT :		[0-9] ;