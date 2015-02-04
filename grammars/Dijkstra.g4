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
 // Matching specification as closely as possible for format
 // for easier comparison
 dijkstraText : 			program EOF;
 program :					PROGRAM ID (declaration | statement)+;
 
 //Declarations
 declaration :				variabledeclaration 
 								| arraydeclaration
 								| proceduredeclaration
 								| functiondeclaration;
 variabledeclaration :		type idlist separator;
 arraydeclaration :			type LBRACK expression RBRACK idlist separator;
 functiondeclaration :		FUN ID LPAR parameterlist? RPAR COLON typelist compoundstatement;
 proceduredeclaration :		PROC ID LPAR parameterlist? RPAR compoundstatement;
 	parameterlist :			parameter | parameterlist COMMA parameter;
 	parameter :				ID | type ID;
 
 //Some utils
 type : 					FLOAT | INT | BOOLEAN;
 typelist :					type | typelist COMMA type;
 separator :				SEMICOLON?;
 idlist :					ID | idlist COMMA ID;
 expressionlist :			expression | expressionlist COMMA expression;
 
 //Statements
 statement :				assignstatement separator
 								| alternativestatement
 								| iterativestatement
 								| inputstatement separator
 								| outputstatement separator
 								| compoundstatement
 								| returnstatement separator
 								| procedurecall separator;
 								
 assignstatement :			varlist ASSIGN expressionlist;
 	var :					ID | arrayaccessor;
 	varlist :				var | varlist COMMA var;
 
 alternativestatement :		IF guard+ FI;
 iterativestatement :		DO guard+ OD;
 	guard :					expression GUARD statement;
 
 inputstatement :			INPUT idlist;
 outputstatement :			PRINT expression;
 compoundstatement :		LBRACE compoundbody RBRACE;
 	compoundbody :			cpddeclorstatement
 								| compoundbody cpddeclorstatement;
 	cpddeclorstatement:		variabledeclaration
 								| arraydeclaration
 								| statement;
 					
 returnstatement :			RETURN expressionlist?;
 procedurecall :			ID LPAR arglist? RPAR;
 arglist :		 			expression | arglist COMMA expression;
 
 //Expressions
 expression :				LPAR expression RPAR
 								| (TILDE | MINUS) expression
 								| expression (STAR | SLASH | MOD | DIV) expression
 								| expression (PLUS | MINUS) expression
 								| expression (GT | GTE | LT | LTE) expression
 								| expression AND expression
 								| <assoc=right>expression (EQ | NEQ) expression
 								| expression OR expression
 								| INTEGER 
 								| floatconstant 
 								| TRUE 
 								| FALSE 
 								| ID
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