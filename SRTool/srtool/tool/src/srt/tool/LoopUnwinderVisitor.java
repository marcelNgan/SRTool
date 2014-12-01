package srt.tool;

import java.util.ArrayList;
import java.util.List;
import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.EmptyStmt;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.Stmt;
import srt.ast.UnaryExpr;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class LoopUnwinderVisitor extends DefaultVisitor {

	private boolean unsound;
	private int defaultUnwindBound;

	public LoopUnwinderVisitor(boolean unsound, int defaultUnwindBound) {
		super(true);
		this.unsound = unsound;
		this.defaultUnwindBound = defaultUnwindBound;
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		int bound = defaultUnwindBound;
		boolean defaultIsUsed = true;
		if (whileStmt.getBound() != null) {
			bound = whileStmt.getBound().getValue();
			defaultIsUsed = false;
		}
		Stmt Body = whileStmt.getBody();
		
		List<Invariant> invariants = whileStmt.getInvariantList().getInvariants();
		
		List<Stmt> invariantList = new ArrayList<Stmt>();
		
		//invariants
		for (Invariant invariant : invariants) {
			AssertStmt assertStmt = new AssertStmt(invariant.getExpr());
			invariantList.add(assertStmt);
		}
		
		
		Stmt endIfBranch;
		
		
		if (defaultIsUsed && !unsound) {
			endIfBranch = new BlockStmt(new Stmt[] {
					new AssertStmt(new UnaryExpr(UnaryExpr.LNOT,whileStmt.getCondition())),
					new AssumeStmt(new UnaryExpr(UnaryExpr.LNOT,whileStmt.getCondition())) });
		} else {
			endIfBranch = new BlockStmt(new Stmt[] { new AssumeStmt(
					new IntLiteral(0)) });
		}
		
		IfStmt ifStmt = new IfStmt(whileStmt.getCondition(), endIfBranch,
				new EmptyStmt());
		
		for (int i = 0; i < bound; i++) {
			List<Stmt> statements = new ArrayList<Stmt>();
			statements.addAll(((BlockStmt) Body).getStmtList()
					.getStatements());
			statements.addAll(invariantList);
			statements.add(ifStmt);
			
			Stmt ifBody = new BlockStmt(statements);
			
			ifStmt = new IfStmt(whileStmt.getCondition(), ifBody, new EmptyStmt());
		}
		List<Stmt> unwindLoop = new ArrayList<Stmt>();
		unwindLoop.addAll(invariantList);
		unwindLoop.add(ifStmt);
		return new BlockStmt(unwindLoop);
	}
}