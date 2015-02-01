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
  * This is the grammar for Dijkstra with Functions
  */
 grammar Dijkstra;
 
 // Parser rules
 djikstraText : 			program EOF;
 program :					PROGRAM ID declaration+;
 declaration :				variabledeclaration 
 								| arraydeclaration;
 variabledeclaration :		type idlist separator;
 arraydeclaration :			type LBRACK expression RBRACK idlist separator;
 type : 					FLOAT | INT | BOOLEAN;
 separator :				SEMICOLON?;
 idlist :					ID | idlist COMMA ID;
 statement :				returnstatement
 								| procedurecall;
 guardedstatementlist :		guard | guardedstatementlist guard;
 guard :					expression GUARD statement;
 expressionlist :			expression | expressionlist COMMA expression;
 returnstatement :			RETURN expressionlist?;
 procedurecall :			ID LPAR arglist? RPAR;
 arglist :		 			argument | arglist COMMA argument;
 argument :					expression;
 //Expressions
 expression :				logicalorexpression;
 logicalorexpression :		logicalandexpression 
 								| logicalorexpression OR logicalandexpression;
 logicalandexpression :		equalityexpression 
 								| logicalandexpression AND equalityexpression;
 equalityexpression :		relationalexpression 
 								| relationalexpression equalityop relationalexpression;
 equalityop :				EQ | NEQ;
 relationalexpression : 	additiveexpression 
 								| additiveexpression relationalop additiveexpression;
 relationalop :				GT | GTE | LT | LTE;
 additiveexpression :		multiplicativeexpression 
 								| additiveexpression additiveop multiplicativeexpression;
 additiveop :				PLUS | MINUS;
 multiplicativeexpression :	unaryexpression 
 								| multiplicativeexpression multiplicativeop unaryexpression;
 multiplicativeop :			STAR | SLASH | MOD | DIV;
 unaryexpression :			primaryexpression 
 								| unaryop unaryexpression;
 unaryop :					TILDE | MINUS;
 primaryexpression :		 INTEGER 
 								| floatconstant 
 								| TRUE 
 								| FALSE 
 								| ID
 								| LPAR expression RPAR
 								| functioncall
 								| arrayaccessor;
 functioncall :				ID LPAR arglist? RPAR;
 arrayaccessor :			ID LBRACK expression RBRACK;
 floatconstant :			INTEGER PERIOD INTEGER;
 
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