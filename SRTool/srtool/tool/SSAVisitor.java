package srt.tool;

import java.util.HashMap;
import java.util.Map;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

public class SSAVisitor extends DefaultVisitor {

	private Map<String, Integer> index = new HashMap<String, Integer>();

	public SSAVisitor() {
		super(true);
	}

	@Override
	public Object visit(Decl decl) {
		String name = decl.getName();
		index.put(name, 0);
		String SSAName = name+"$"+index.get(name);
		return new Decl(SSAName, decl.getType(), decl.getNodeInfo());
	}

	@Override
	public Object visit(DeclRef declRef) {
		String name = declRef.getName();
		/*if(index.get(name)==null)
			index.put(name, 0);*/
		String SSAName = name+"$"+index.get(name);
		return new DeclRef(SSAName, declRef.getNodeInfo());
	}

	@Override
	public Object visit(AssignStmt assignment) {
		Expr expr  = (Expr) super.visit(assignment.getRhs());
		String lhsName = assignment.getLhs().getName();
		int i = index.get(lhsName) + 1;
        index.put(lhsName, i);
		DeclRef declRef  = (DeclRef) this.visit(assignment.getLhs());
		return new AssignStmt(declRef, expr);
	}

}
