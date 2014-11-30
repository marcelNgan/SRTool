package srt.tool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

public class PredicationVisitor extends DefaultVisitor {

	private String freshVariable;
	private Expr parentPredicate;
	private Expr globalPredicate;

	public PredicationVisitor() {
		super(true);
		freshVariable = "$A";
		//initialized to "G" which is true
		globalPredicate = new IntLiteral(1);
		parentPredicate = globalPredicate;
	}
	
	public String generateFreshVariable(){
		StringBuilder variableChars = new StringBuilder(freshVariable);
		char lastChar = variableChars.charAt(variableChars.length()-1);
		if (lastChar == 'Z'){
			variableChars.append('A');
		} else {
			variableChars.setCharAt(variableChars.length()-1, (char) (lastChar+1));
		}
		freshVariable = variableChars.toString()
		return freshVariable;
	}
	
	public Expr gAndP(){
		return new BinaryExpr(BinaryExpr.LAND, globalPredicate, parentPredicate);
	}
	
	@Override
	public Object visit(IfStmt ifStmt) {
		//Q = P && E;
		//R = P && !E;
		//Pred(S, Q);
		//Pred(T, R);
		List<Stmt> stmts = new LinkedList<Stmt>();
		Expr ifExpr = ifStmt.getCondition();
		DeclRef q = new DeclRef(generateFreshVariable());
        DeclRef r = new DeclRef(generateFreshVariable());
        Decl declareQ = new Decl(q.getName(), "int");
		stmts.add(declareQ);
        Decl declareR = new Decl(r.getName(), "int");
		stmts.add(declareR);
		BinaryExpr qExpression = new BinaryExpr(BinaryExpr.LAND, parentPredicate, ifExpr);
		BinaryExpr rExpression = new BinaryExpr(BinaryExpr.LAND, parentPredicate, new UnaryExpr(UnaryExpr.LNOT, ifExpr));
		AssignStmt assignQ = new AssignStmt(q, qExpression);
		stmts.add(assignQ);
        AssignStmt assignR = new AssignStmt(r, rExpression);
		stmts.add(assignR);
		Expr tempParentPredicate = parentPredicate;
		parentPredicate = q;
		Stmt thenStmt = (Stmt) visit(ifStmt.getThenStmt());
		stmts.add(thenStmt);
		parentPredicate = r;
		Stmt elseStmt = (Stmt) visit(ifStmt.getElseStmt());
		stmts.add(elseStmt);
		parentPredicate = tempParentPredicate;
				
		return super.visit((new BlockStmt(stmts,ifStmt.getNodeInfo())));
	}

	@Override
	public Object visit(AssertStmt assertStmt) {
		// assert(G && P => E)
		Expr lhs = new UnaryExpr(UnaryExpr.LNOT, gAndP());
		Expr rhs = assertStmt.getCondition();
		return super.visit(new AssertStmt(new BinaryExpr(BinaryExpr.LOR, lhs, rhs), assertStmt.getNodeInfo()));
	}

	@Override
	public Object visit(AssignStmt assignment) {
		// x = (G && P) ? E : x;
		TernaryExpr ternaryExpression = new TernaryExpr(gAndP(), assignment.getRhs(), assignment.getLhs());
		return super.visit(new AssignStmt(assignment.getLhs(), ternaryExpression, assignment.getNodeInfo()));
	}

	@Override
	public Object visit(AssumeStmt assumeStmt) {
		// G = G && (P => E)
		Expr lhs = new UnaryExpr(UnaryExpr.LNOT, gAndP());
		Expr rhs = assumeStmt.getCondition();
		BinaryExpr biExpression = new BinaryExpr(BinaryExpr.LOR, lhs, rhs);
		DeclRef freshVar = new DeclRef(generateFreshVariable());
		Stmt declFreshVar = new Decl(freshVar.getName(), "int");
		AssignStmt assignStatement = new AssignStmt(freshVar, biExpression);
		globalPredicate = new BinaryExpr(BinaryExpr.LAND, globalPredicate, freshVar);
		//not sure do i need an assignstatement statement for newG in Block statement
		return super.visit(new BlockStmt(new Stmt[] {declFreshVar, assignStatement}, assumeStmt.getNodeInfo()));
	}

	@Override
	public Object visit(HavocStmt havocStmt) {
		//v = ((G && P) ? h : v)
		DeclRef v = havocStmt.getVariable();
		DeclRef h = new DeclRef(generateFreshVariable());
		Stmt declH = new Decl(h.getName(), "int");
		TernaryExpr ternaryExpression = new TernaryExpr(gAndP(), h, v);
		Stmt e = new AssignStmt(v, ternaryExpression);
		return super.visit(new BlockStmt(new Stmt[] { declH, e}, havocStmt.getNodeInfo()));
	}

}