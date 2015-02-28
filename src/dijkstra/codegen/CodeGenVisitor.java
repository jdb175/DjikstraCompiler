package dijkstra.codegen;

import static dijkstra.utility.DijkstraType.INT;
import static org.objectweb.asm.Opcodes.*;

import java.util.Stack;

import dijkstra.symbol.Symbol;
import dijkstra.utility.DijkstraType;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import dijkstra.lexparse.DijkstraBaseVisitor;
import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.*;
import djikstra.semantic.DjikstraTypeFinalizerVisitor;

public class CodeGenVisitor extends DijkstraBaseVisitor<byte[]> {
	public ParseTreeProperty<Symbol> symbols = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> functions = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<Symbol> arrays = new ParseTreeProperty<Symbol>();
	public ParseTreeProperty<DijkstraType> types = new ParseTreeProperty<DijkstraType>();
	private ClassWriter cw = null;
	private MethodVisitor mv = null;
	
	private final String DEFAULT_PACKAGE = "djkcode";
	private String classPackage;
	//private boolean needValue;		// used to indicate whether we need an ID value or address
	final private Stack<Label> guardLabelStack;
	final private Stack<DijkstraType> typeNeeded;
	
	
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
		// prolog
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES); 
		cw.visit(V1_8, ACC_PUBLIC + ACC_STATIC, classPackage + "/" + program.ID().getText(), null, 
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
		while(varList != null) {				
			VarContext var = varList.var();
			Symbol curSymbol = symbols.get(var);
			if(curSymbol != null) {
				typeNeeded.push(curSymbol.getType());
				exprList.expression().accept(this);	// TOS = expression value
				if(curSymbol.getType() == DijkstraType.FLOAT) {
					mv.visitVarInsn(FSTORE, curSymbol.getAddress());
				} else {
					mv.visitVarInsn(ISTORE, curSymbol.getAddress());
				}
				typeNeeded.pop();
			}
			varList = varList.varList();
			exprList = exprList.expressionList();
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
				mv.visitVarInsn(ISTORE, s.getAddress());
			} else if (s.getType() == DijkstraType.FLOAT) {
				mv.visitMethodInsn(INVOKESTATIC, "dijkstra/runtime/DijkstraRuntime", "inputFloat", 
						"(Ljava/lang/String;)F", false);
				mv.visitVarInsn(FSTORE, s.getAddress());
			} else {
				mv.visitMethodInsn(INVOKESTATIC, "dijkstra/runtime/DijkstraRuntime", "inputBoolean", 
						"(Ljava/lang/String;)Z", false);
				mv.visitVarInsn(ISTORE, s.getAddress());
			}
		}
		return null;
	}
	
	@Override
	public byte[] visitIdexp(IdexpContext ctx) {
		Symbol symbol = symbols.get(ctx);
		if(symbol.getType() == DijkstraType.FLOAT) {
			mv.visitVarInsn(FLOAD, symbol.getAddress());
		} else {
			mv.visitVarInsn(ILOAD, symbol.getAddress());
		}
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
	
	public void cast(DijkstraType from) {
		DijkstraType to = typeNeeded.isEmpty() ? null : typeNeeded.peek();
		if(from == DijkstraType.INT && to == DijkstraType.FLOAT) {
			mv.visitInsn(I2F);
		} else if (from == DijkstraType.FLOAT && to == DijkstraType.INT){
			mv.visitInsn(F2I);
		}
	}
}
