package srt.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.*;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.HavocStmt;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.ast.*;


public class LoopAbstractionVisitor extends DefaultVisitor {

	public LoopAbstractionVisitor() {
		super(true);
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		List<Stmt> stmts = new LinkedList<Stmt>();
		//get invariant
		List<Invariant> invariants = whileStmt.getInvariantList().getInvariants();
		List<Expr> invariantExprs = new ArrayList<Expr>();
		List<Stmt> AssertStmts = new LinkedList<Stmt>();
		
		//assertion
		for (Invariant invariant : invariants) {
			Expr invariantexpr = invariant.getExpr();
			invariantExprs.add(invariantexpr);
			AssertStmt assertStmt = new AssertStmt(invariantexpr);
			AssertStmts.add(assertStmt);
		}
		stmts.addAll(AssertStmts);
		
		Expr whileCondition = whileStmt.getCondition();
		Stmt whileBody = whileStmt.getBody();
		
		//get the set of modified variables
		ModSetVisitor modSetVisitor = new ModSetVisitor();
		modSetVisitor.visit(whileStmt.getBody());
		Set<DeclRef> modSet = modSetVisitor.getModSet();
		
		//havoc modified variables 
		for (DeclRef var : modSet) {
			stmts.add(new HavocStmt(var));
		}
		
		
		List<Stmt> AssumeStmts = new LinkedList<Stmt>();
		for (Invariant invariant : invariants) {
			AssumeStmts.add(new AssumeStmt(invariant.getExpr()));
		}
		stmts.addAll(AssumeStmts);
		
		List<Stmt> ifStmts = new LinkedList<Stmt>();
		ifStmts.add((Stmt) visit(whileBody));
		ifStmts.addAll(AssertStmts);
		ifStmts.add(new AssumeStmt(new IntLiteral(0)));
		
		BlockStmt newCondition = new BlockStmt(ifStmts);
		Stmt ifStmt = new IfStmt(whileCondition, newCondition, null);
		
		stmts.add(ifStmt);
		
		return new BlockStmt(stmts, whileStmt.getNodeInfo());
	}
	
	private class ModSetVisitor extends DefaultVisitor {
		// get modified variables 
		private Set<DeclRef> modSet;
		public ModSetVisitor() {
			super(true);
			this.modSet = new HashSet<DeclRef>();
		}
		@Override
		public Object visit(AssignStmt assignment) {
			DeclRef lhs = assignment.getLhs();
			modSet.add(lhs);
			return visitChildren(assignment);
		}
		protected Set<DeclRef> getModSet() {
			return this.modSet;
		}
	}

}