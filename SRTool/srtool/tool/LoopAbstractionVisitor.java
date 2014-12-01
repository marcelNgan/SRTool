package srt.tool;

import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class LoopAbstractionVisitor extends DefaultVisitor {

	public LoopAbstractionVisitor() {
		super(true);
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		return super.visit(whileStmt);
	}

}