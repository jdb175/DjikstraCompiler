package dijkstra.codegen;

import static dijkstra.utility.DijkstraType.INT;
import static org.objectweb.asm.Opcodes.*;

import java.util.Stack;

import dijkstra.semantic.DjikstraTypeFinalizerVisitor;
import dijkstra.symbol.MethodSymbol;
import dijkstra.symbol.Symbol;
import dijkstra.utility.DijkstraType;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import dijkstra.lexparse.DijkstraBaseVisitor;
import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.*;

public class CodeGenVisitor extends DijkstraBaseVisitor<byte[]> {
	public ParseTreeProperty<Symbol> symbols = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> functions = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> arrays = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<DijkstraType> types = new ParseTreeProperty<DijkstraType>();
	private ClassWriter cw = null;
	private MethodVisitor mv = null;
	private MethodVisitor oldmv = null;
	
	private final String DEFAULT_PACKAGE = "djkcode";
	private String classPackage;
	private String classNameQualified;
	//private boolean needValue;		// used to indicate whether we need an ID value or address
	final private Stack<Label> guardLabelStack;
	final private Stack<DijkstraType> typeNeeded;
	private String programName;
	
	
	public CodeGenVisitor(DjikstraTypeFinalizerVisitor oldTree)
	{
		this.types = oldTree.types;
		this.symbols = oldTree.symbols;
		this.arrays = oldTree.arrays;
		this.functions = oldTree.functions;
		classPackage = DEFAULT_PACKAGE;
		guardLabelStack = new Stack<Label>();
		typeNeeded = new Stack<DijkstraType>();
	}
	
	@Override
	public byte[] visitDijkstraText(DijkstraTextContext ctx) 
	{
		return ctx.program().accept(this);
	}
	
	/**
	 * Generate the program prolog, then visit the children, then generate
	 * the program end.
	 * @see dijkstra.ast.ASTVisitor#visit(dijkstra.ast.ASTNodeFactory.ProgramNode)
	 */
	@Override
	public byte[] visitProgram(ProgramContext program) 
	{
		this.programName = program.ID().getText();
		// prolog
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES); 
		classNameQualified = classPackage + "/" + program.ID().getText();
		cw.visit(V1_8, ACC_PUBLIC + ACC_STATIC, classNameQualified, null, 
				"java/lang/Object", null);
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		// Start the main() method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		
		visitChildren(program);
		
		// program end
		//  End of main
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		// Actual end of generation
		cw.visitEnd();
		return cw.toByteArray();
	}
	
	@Override
	public byte[] visitOutputStatement(OutputStatementContext ctx)
	{
		ctx.expression().accept(this);	// TOS = expression value
		if (types.get(ctx) == DijkstraType.INT) {
			mv.visitMethodInsn(INVOKESTATIC, 
				"dijkstra/runtime/DijkstraRuntime", "printInt", "(I)V", false);
		} else if (types.get(ctx) == DijkstraType.FLOAT) {
			mv.visitMethodInsn(INVOKESTATIC, 
					"dijkstra/runtime/DijkstraRuntime", "printFloat", "(F)V", false);
		} else {
			mv.visitMethodInsn(INVOKESTATIC, 
					"dijkstra/runtime/DijkstraRuntime", "printBoolean", "(Z)V", false);
		}
		return null;
	}
	
	@Override
	public byte[] visitAssignStatement(AssignStatementContext ctx) 
	{
		//iterate over var list and expressionList
		VarListContext varList = ctx.varList();
		ExpressionListContext exprList = ctx.expressionList();
		Stack<Symbol> assignees = new Stack<Symbol>();
		Stack<Boolean> isArrays = new Stack<Boolean>();
		while(varList != null) {				
			VarContext var = varList.var();
			Symbol curSymbol = symbols.get(var);
			if(curSymbol != null) {
				typeNeeded.push(curSymbol.getType());
				exprList.expression().accept(this);
				typeNeeded.pop();
				assignees.push(curSymbol);
				isArrays.push(false);
			} else {
				ArrayAccessorContext arr = var.arrayAccessor();
				Symbol curArray = arrays.get(arr);
				getArray(curArray);
				typeNeeded.push(INT);
				arr.expression().accept(this);
				typeNeeded.pop();
				typeNeeded.push(curArray.getType());
				exprList.expression().accept(this);
				typeNeeded.pop();
				assignees.push(curArray);
				isArrays.push(true);
			}
			varList = varList.varList();
			exprList = exprList.expressionList();
		}
		while(!assignees.isEmpty()) {
			Symbol cur = assignees.pop();
			Boolean isArray = isArrays.pop();
			if(isArray) {
				if(cur.getType() == DijkstraType.FLOAT) {
					mv.visitInsn(FASTORE);
				} else {
					mv.visitInsn(IASTORE);
				}
			} else {
				putSymbol(cur);
			}
		}
				
		return null;
	}
	
	@Override
	public byte[] visitInputStatement(InputStatementContext ctx) {
		IdListContext idlist = ctx.idList();
		Stack<IdListContext> ids = new Stack<IdListContext>();
		while(idlist != null) {
			ids.push(idlist);
			idlist = idlist.idList();
		}
		while(!ids.isEmpty()) {
			idlist = ids.pop();
			Symbol s = symbols.get(idlist);
			mv.visitLdcInsn(s.getId());	// Name of the variable
			if (s.getType() == INT) {
				mv.visitMethodInsn(INVOKESTATIC, "dijkstra/runtime/DijkstraRuntime", "inputInt", 
						"(Ljava/lang/String;)I", false);
				//mv.visitVarInsn(ISTORE, s.getAddress());
				putSymbol(s);
			} else if (s.getType() == DijkstraType.FLOAT) {
				mv.visitMethodInsn(INVOKESTATIC, "dijkstra/runtime/DijkstraRuntime", "inputFloat", 
						"(Ljava/lang/String;)F", false);
				//mv.visitVarInsn(FSTORE, s.getAddress());
				putSymbol(s);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, "dijkstra/runtime/DijkstraRuntime", "inputBoolean", 
						"(Ljava/lang/String;)Z", false);
				//mv.visitVarInsn(ISTORE, s.getAddress());
				putSymbol(s);
			}
		}
		return null;
	}
	
	@Override
	public byte[] visitAlternativeStatement (AlternativeStatementContext ctx) {
		final Label endLabel = new Label();
		guardLabelStack.push(endLabel);
		visitChildren(ctx);
		guardLabelStack.pop();
		mv.visitIntInsn(BIPUSH, ctx.getStart().getLine());
		mv.visitMethodInsn(INVOKESTATIC, "dijkstra/runtime/DijkstraRuntime", 
				"abortNoAlternative", "(I)V", false);
		mv.visitLabel(endLabel);
		return null;
	}
	
	@Override
	public byte[] visitIterativeStatement (IterativeStatementContext ctx) {
		final Label startLabel = new Label();
		mv.visitLabel(startLabel);
		guardLabelStack.push(startLabel);
		visitChildren(ctx);
		guardLabelStack.pop();
		return null;
	}
	
	@Override
	public byte[] visitGuard (GuardContext ctx) {
		final Label failLabel = new Label();
		ctx.expression().accept(this);
		mv.visitJumpInsn(IFEQ, failLabel);
		ctx.statement().accept(this);
		mv.visitJumpInsn(GOTO, guardLabelStack.peek());
		mv.visitLabel(failLabel);
		return null;
	}
	
	@Override
	public byte[] visitArrayDeclaration (ArrayDeclarationContext ctx) {
		IdListContext idlist = ctx.idList();
		while(idlist != null) {
			Symbol s = symbols.get(idlist);
			typeNeeded.push(INT);
			ctx.expression().accept(this);
			typeNeeded.pop();
			if(s.getType() == DijkstraType.FLOAT) {
				mv.visitIntInsn(NEWARRAY, T_FLOAT);
			} else {
				mv.visitIntInsn(NEWARRAY, T_INT);
			}
			createArray(s);
			idlist = idlist.idList();
		}
		return null;
	}
	
	@Override
	public byte[] visitProcedureDeclaration(ProcedureDeclarationContext ctx) {
		oldmv = mv;
		JVMInfo.enterScope();
		MethodSymbol proc = (MethodSymbol) symbols.get(ctx);
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, proc.getName(), proc.getSignature(), null, null); 
		mv.visitCode();
		//We have to visit the parameters in reverse order to get the proper locals
		if(ctx.parameterList() != null) {
			Stack<ParameterContext> args = new Stack<ParameterContext>();
			ParameterListContext params = ctx.parameterList();
			while(params != null) {
				args.push(params.parameter());
				params = params.parameterList();
			}
			while(!args.isEmpty()) {
				Symbol s = symbols.get(args.pop());
				s.getAddress();
			}
		}
		ctx.compoundStatement().accept(this);
		//In case of fallthrough in control, just to compile
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		mv = oldmv;
		JVMInfo.exitScope();
		return null;
	}
	
	@Override
	public byte[] visitFunctionDeclaration(FunctionDeclarationContext ctx) {
		oldmv = mv;
		JVMInfo.enterScope();
		MethodSymbol fun = (MethodSymbol) functions.get(ctx);
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, fun.getName(), fun.getSignature(), null, null); 
		mv.visitCode();
		//We have to visit the parameters in reverse order to get the proper locals
		if(ctx.parameterList() != null) {
			Stack<ParameterContext> args = new Stack<ParameterContext>();
			ParameterListContext params = ctx.parameterList();
			while(params != null) {
				args.push(params.parameter());
				params = params.parameterList();
			}
			while(!args.isEmpty()) {
				Symbol s = symbols.get(args.pop());
				s.getAddress();
			}
		}
		ctx.compoundStatement().accept(this);
		//In case of fallthrough in control, just to compile
		mv.visitIntInsn(BIPUSH, ctx.getStart().getLine());
		mv.visitMethodInsn(INVOKESTATIC, "dijkstra/runtime/DijkstraRuntime", 
				"abortNoFunctionReturn", "(I)V", false);
		mv.visitLdcInsn(-1);
		mv.visitInsn(IRETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		mv = oldmv;
		JVMInfo.exitScope();
		return null;
	}
	
	@Override
	public byte[] visitReturnStatement(ReturnStatementContext ctx) {
		ctx.expression().accept(this);
		if(types.get(ctx) == DijkstraType.FLOAT) {
			mv.visitInsn(FRETURN);
		} else {
			mv.visitInsn(IRETURN);
		}
		return null;
	}
	
	@Override
	public byte[] visitFunctionCall(FunctionCallContext ctx) {
		MethodSymbol fun = (MethodSymbol) functions.get(ctx);
		//put arguments
		ArgListContext params = ctx.argList();
		Stack<ExpressionContext> args = new Stack<ExpressionContext>();
		while(params != null) {
			args.push(params.expression());
			params = params.argList();
		}
		int i = 0;
		while(!args.isEmpty()) {
			typeNeeded.push(fun.getParameter(i));
			args.pop().accept(this);
			typeNeeded.pop();
			++i;
		}
		//call
		mv.visitMethodInsn(INVOKESTATIC, classNameQualified, fun.getName(), fun.getSignature(), false);
		return null;
	}
	
	@Override
	public byte[] visitProcedureCall(ProcedureCallContext ctx) {
		MethodSymbol proc = (MethodSymbol) symbols.get(ctx);
		//put arguments
		ArgListContext params = ctx.argList();
		Stack<ExpressionContext> args = new Stack<ExpressionContext>();
		while(params != null) {
			args.push(params.expression());
			params = params.argList();
		}
		int i = 0;
		while(!args.isEmpty()) {
			typeNeeded.push(proc.getParameter(i));
			args.pop().accept(this);
			typeNeeded.pop();
			++i;
		}
		//call
		mv.visitMethodInsn(INVOKESTATIC, classNameQualified, proc.getName(), proc.getSignature(), false);
		return null;
	}
	
	/** Expressions **/
	
	@Override
	public byte[] visitUnary(UnaryContext ctx) {
		ctx.expression().accept(this);
		if(ctx.MINUS() != null) {
			if(types.get(ctx) == DijkstraType.INT) {
				mv.visitInsn(INEG);
			} else {
				mv.visitInsn(FNEG);
			}
			cast(types.get(ctx));
		} else {
			final Label l1 = new Label();
			final Label l2 = new Label();
			mv.visitJumpInsn(IFEQ, l1);
			mv.visitInsn(ICONST_0);		// true -> false
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);		// false -> true
			mv.visitLabel(l2);
		}
		return null;
	}
	
	@Override
	public byte[] visitMult(MultContext ctx) {
		if(ctx.STAR() != null) {
			if(types.get(ctx) == DijkstraType.INT) {
				typeNeeded.push(DijkstraType.INT);
				ctx.expression(0).accept(this);
				ctx.expression(1).accept(this);
				typeNeeded.pop();
				mv.visitInsn(IMUL);
			} else {
				typeNeeded.push(DijkstraType.FLOAT);
				ctx.expression(0).accept(this);
				ctx.expression(1).accept(this);
				typeNeeded.pop();
				mv.visitInsn(FMUL);
			}
		} else if (ctx.SLASH() != null) {
			typeNeeded.push(DijkstraType.FLOAT);
			ctx.expression(0).accept(this);
			ctx.expression(1).accept(this);
			typeNeeded.pop();
			mv.visitInsn(FDIV);
		} else if (ctx.MOD() != null) {
			ctx.expression(0).accept(this);
			ctx.expression(1).accept(this);
			mv.visitInsn(IREM);
		} else if (ctx.DIV() != null) {
			ctx.expression(0).accept(this);
			ctx.expression(0).accept(this);
			ctx.expression(1).accept(this);
			mv.visitInsn(IREM);
			mv.visitInsn(ISUB);
			ctx.expression(1).accept(this);
			mv.visitInsn(IDIV);
		}
		cast(types.get(ctx));
		return null;
	}
	
	@Override
	public byte[] visitAdd(AddContext ctx) {
		if(types.get(ctx) == DijkstraType.INT) {
			typeNeeded.push(DijkstraType.INT);
			ctx.expression(0).accept(this);
			ctx.expression(1).accept(this);
			typeNeeded.pop();
			if(ctx.PLUS() != null) {
				mv.visitInsn(IADD);
			} else {
				mv.visitInsn(ISUB);
			}
		} else {
			typeNeeded.push(DijkstraType.FLOAT);
			ctx.expression(0).accept(this);
			ctx.expression(1).accept(this);
			typeNeeded.pop();
			if(ctx.PLUS() != null) {
				mv.visitInsn(FADD);
			} else {
				mv.visitInsn(FSUB);
			}
		}
		cast(types.get(ctx));
		return null;
	}
	
	@Override
	public byte[] visitAnd(AndContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		mv.visitInsn(IAND);
		return null;
	}
	
	@Override
	public byte[] visitOr(OrContext ctx) {
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		mv.visitInsn(IOR);
		return null;
	}
	
	@Override
	public byte[] visitEqual(EqualContext ctx) {
		boolean isFloat = false;
		if(types.get(ctx.expression(0)) == DijkstraType.FLOAT || types.get(ctx.expression(1)) == DijkstraType.FLOAT){
			isFloat = true;
		}
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		Label lab1, lab2;
		if(isFloat) {
			if(ctx.EQ() != null) {
				mv.visitInsn(FCMPL);
				Label l3 = new Label();
				mv.visitJumpInsn(IFNE, l3);
				mv.visitInsn(ICONST_1);
				Label l4 = new Label();
				mv.visitJumpInsn(GOTO, l4);
				mv.visitLabel(l3);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l4);
			} else {
				mv.visitInsn(FCMPL);
				Label l3 = new Label();
				mv.visitJumpInsn(IFEQ, l3);
				mv.visitInsn(ICONST_1);
				Label l4 = new Label();
				mv.visitJumpInsn(GOTO, l4);
				mv.visitLabel(l3);
				mv.visitInsn(ICONST_0);					
				mv.visitLabel(l4);
			}
		} else {
			if(ctx.EQ() != null) {
				lab1 =  new Label();
				mv.visitJumpInsn(IF_ICMPNE, lab1);
				mv.visitInsn(ICONST_1);		// left = right
				lab2 = new Label();
				mv.visitJumpInsn(GOTO, lab2);
				mv.visitLabel(lab1);
				mv.visitInsn(ICONST_0);		// left ~= right
				mv.visitLabel(lab2);
			} else {
				lab1 =  new Label();
				mv.visitJumpInsn(IF_ICMPEQ, lab1);
				mv.visitInsn(ICONST_1);		// left = right
				lab2 = new Label();
				mv.visitJumpInsn(GOTO, lab2);
				mv.visitLabel(lab1);
				mv.visitInsn(ICONST_0);		// left ~= right
				mv.visitLabel(lab2);
			}
		}
		return null;
	}
	
	@Override
	public byte[] visitRelational(RelationalContext ctx) {
		boolean isFloat = false;
		if(types.get(ctx.expression(0)) == DijkstraType.FLOAT || types.get(ctx.expression(1)) == DijkstraType.FLOAT){
			isFloat = true;
			typeNeeded.push(DijkstraType.FLOAT);
		}
		ctx.expression(0).accept(this);
		ctx.expression(1).accept(this);
		if(isFloat) {
			typeNeeded.pop();
		}
		Label lab1, lab2;
		if(isFloat) {
			if(ctx.LT() != null) {
				mv.visitInsn(FCMPG);
				Label l3 = new Label();
				mv.visitJumpInsn(IFGE, l3);
				mv.visitInsn(ICONST_1);
				Label l4 = new Label();
				mv.visitJumpInsn(GOTO, l4);
				mv.visitLabel(l3);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l4);
			} else if (ctx.GT() != null) {
				mv.visitInsn(FCMPL);
				Label l3 = new Label();
				mv.visitJumpInsn(IFLE, l3);
				mv.visitInsn(ICONST_1);
				Label l4 = new Label();
				mv.visitJumpInsn(GOTO, l4);
				mv.visitLabel(l3);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l4);
			} else if (ctx.LTE() != null) {
				mv.visitInsn(FCMPG);
				Label l3 = new Label();
				mv.visitJumpInsn(IFGT, l3);
				mv.visitInsn(ICONST_1);
				Label l4 = new Label();
				mv.visitJumpInsn(GOTO, l4);
				mv.visitLabel(l3);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l4);
			} else if(ctx.GTE() != null) {
				mv.visitInsn(FCMPL);
				Label l3 = new Label();
				mv.visitJumpInsn(IFLT, l3);
				mv.visitInsn(ICONST_1);
				Label l4 = new Label();
				mv.visitJumpInsn(GOTO, l4);
				mv.visitLabel(l3);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l4);
			}
		} else {
			if(ctx.LT() != null) {
				lab1 =  new Label();
				mv.visitJumpInsn(IF_ICMPGE, lab1);
				mv.visitInsn(ICONST_1);		// left < right
				lab2 = new Label();
				mv.visitJumpInsn(GOTO, lab2);
				mv.visitLabel(lab1);
				mv.visitInsn(ICONST_0);		// right >= left
				mv.visitLabel(lab2);
			} else if (ctx.GT() != null) {
				lab1 =  new Label();
				mv.visitJumpInsn(IF_ICMPLE, lab1);
				mv.visitInsn(ICONST_1);		// left > right
				lab2 = new Label();
				mv.visitJumpInsn(GOTO, lab2);
				mv.visitLabel(lab1);
				mv.visitInsn(ICONST_0);		// right <= left
				mv.visitLabel(lab2);
			} else if(ctx.LTE() != null) {
				lab1 =  new Label();
				mv.visitJumpInsn(IF_ICMPGT, lab1);
				mv.visitInsn(ICONST_1);		// left < right
				lab2 = new Label();
				mv.visitJumpInsn(GOTO, lab2);
				mv.visitLabel(lab1);
				mv.visitInsn(ICONST_0);		// right >= left
				mv.visitLabel(lab2);
			} else if (ctx.GTE() != null) {
				lab1 =  new Label();
				mv.visitJumpInsn(IF_ICMPLT, lab1);
				mv.visitInsn(ICONST_1);		// left > right
				lab2 = new Label();
				mv.visitJumpInsn(GOTO, lab2);
				mv.visitLabel(lab1);
				mv.visitInsn(ICONST_0);		// right <= left
				mv.visitLabel(lab2);
			}
		}
		return null;
	}
	
	@Override
	public byte[] visitCompound(CompoundContext ctx) {
		return ctx.expression().accept(this);
	}
	
	@Override
	public byte[] visitIdexp(IdexpContext ctx) {
		Symbol symbol = symbols.get(ctx);
		getSymbol(symbol);
		cast(symbol.getType());
		return null;
	}
	
	@Override
	public byte[] visitBool(BoolContext ctx) {
		if (ctx.TRUE() != null) {
			mv.visitInsn(ICONST_1);
		} else {
			mv.visitInsn(ICONST_0);
		}
		return null;
	}
	
	@Override
	public byte[] visitInteger(IntegerContext ctx) {
		int i = Integer.parseInt(ctx.INTEGER().getText());
		mv.visitLdcInsn(i);
		cast(DijkstraType.INT);
		return null;
	}
	
	@Override
	public byte[] visitFloat(FloatContext ctx) {
		float f = Float.parseFloat(ctx.getText());
		mv.visitLdcInsn(f);
		cast(DijkstraType.FLOAT);
		return null;
	}
	
	@Override
	public byte[] visitArrayAccess(ArrayAccessContext ctx) {
		Symbol s = arrays.get(ctx.arrayAccessor());
		DijkstraType t = types.get(ctx);
		//mv.visitVarInsn(ALOAD, s.getAddress());
		getArray(s);
		typeNeeded.push(INT);
		ctx.arrayAccessor().expression().accept(this);
		typeNeeded.pop();
		if(t == DijkstraType.FLOAT) {
			mv.visitInsn(FALOAD);
		} else {
			mv.visitInsn(IALOAD);
		}
		cast(t);
		return null;
	}
	
	public void cast(DijkstraType from) {
		DijkstraType to = typeNeeded.isEmpty() ? null : typeNeeded.peek();
		if(from == DijkstraType.INT && to == DijkstraType.FLOAT) {
			mv.visitInsn(I2F);
		} else if (from == DijkstraType.FLOAT && to == DijkstraType.INT){
			mv.visitInsn(F2I);
		}
	}
	
	
	public void getArray(Symbol s) {
		if(s.isLocal()) {
			//use locals if in method
			mv.visitVarInsn(ALOAD, s.getAddress());
		} else {
			mv.visitFieldInsn(GETSTATIC, classNameQualified, s.getFieldName(), "["+s.getTypeID());
		}
	}
	
	public void createArray(Symbol s) {
		if(s.isLocal()) {
			mv.visitVarInsn(ASTORE, s.getAddress());
		} else {
			FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, s.getFieldName(), "["+s.getTypeID(), null, null);
			fv.visitEnd();
			s.setFieldInitialized(true);
			mv.visitFieldInsn(PUTSTATIC, classNameQualified, s.getFieldName(), "["+s.getTypeID());
		}
	}
	
	public void getSymbol(Symbol s) {
		if(s.isLocal()) {
			//use locals if in method
			if(s.getType() == DijkstraType.FLOAT) {
				mv.visitVarInsn(FLOAD, s.getAddress());
			} else {
				mv.visitVarInsn(ILOAD, s.getAddress());
			}
		} else {
			if(!s.fieldInitialized()){
				FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, s.getFieldName(), s.getTypeID(), null, null);
				fv.visitEnd();
				s.setFieldInitialized(true);
			}
			mv.visitFieldInsn(GETSTATIC, classNameQualified, s.getFieldName(), s.getTypeID());
		}
	}

	
	public void putSymbol(Symbol s) {
		if(s.isLocal()) {
			//use locals if in method
			if(s.getType() == DijkstraType.FLOAT) {
				mv.visitVarInsn(FSTORE, s.getAddress());
			} else {
				mv.visitVarInsn(ISTORE, s.getAddress());
			}
		} else {
			if(!s.fieldInitialized()){
				FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, s.getFieldName(), s.getTypeID(), null, null);
				fv.visitEnd();
				s.setFieldInitialized(true);
			}
			mv.visitFieldInsn(PUTSTATIC, classNameQualified, s.getFieldName(), s.getTypeID());
		}
	}

	public void setClassPackage(String customPackage) {
		this.classPackage = customPackage;
	}
	
	public String getProgramName() {
		return this.programName;
	}
}
