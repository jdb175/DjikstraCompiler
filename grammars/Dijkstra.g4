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
 declaration :				variableDeclaration 
 								| arrayDeclaration
 								| procedureDeclaration
 								| functionDeclaration;
 variableDeclaration :		type idList separator;
 arrayDeclaration :			type LBRACK expression RBRACK idList separator;
 functionDeclaration :		FUN ID LPAR parameterList? RPAR COLON type compoundStatement;
 procedureDeclaration :		PROC ID LPAR parameterList? RPAR compoundStatement;
 	parameterList :			parameter | parameterList COMMA parameter;
 	parameter :				ID | type ID;
 
 //Some utils
 type : 					FLOAT | INT | BOOLEAN;
 typeList :					type | typeList COMMA type;
 separator :				SEMICOLON?;
 idList :					ID | idList COMMA ID;
 expressionList :			expression | expressionList COMMA expression;
 
 //Statements
 statement :				assignStatement separator
 								| alternativeStatement
 								| iterativeStatement
 								| inputStatement separator
 								| outputStatement separator
 								| compoundStatement
 								| returnStatement separator
 								| procedureCall separator;
 								
 assignStatement :			varList ASSIGN expressionList;
 	var :					ID | arrayAccessor;
 	varList :				var | varList COMMA var;
 
 alternativeStatement :		IF guard+ FI;
 iterativeStatement :		DO guard+ OD;
 	guard :					expression GUARD statement;
 
 inputStatement :			INPUT idList;
 outputStatement :			PRINT expression;
 compoundStatement :		LBRACE compoundBody RBRACE;
 	compoundBody :			cpdDeclOrStatement
 								| compoundBody cpdDeclOrStatement;
 	cpdDeclOrStatement:		variableDeclaration
 								| arrayDeclaration
 								| statement;
 					
 returnStatement :			RETURN expression?;
 procedureCall :			ID LPAR argList? RPAR;
 argList :		 			expression | argList COMMA expression;
 
 //Expressions
 expression :				LPAR expression RPAR #compound
 								| (TILDE | MINUS) expression #unary
 								| expression (STAR | SLASH | MOD | DIV) expression #mult
 								| expression (PLUS | MINUS) expression #add
 								| expression (GT | GTE | LT | LTE) expression #relational
 								| expression AND expression #and
 								| <assoc=right>expression (EQ | NEQ) expression #equal
 								| expression OR expression #or
 								| INTEGER #integer
 								| INTEGER PERIOD INTEGER #float
 								| TRUE #bool
 								| FALSE #bool
 								| ID #idexp
 								| functionCall #fCall
 								| arrayAccessor #arrayAccess;
 functionCall :				ID LPAR argList? RPAR;
 arrayAccessor :			ID LBRACK expression RBRACK;

 
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