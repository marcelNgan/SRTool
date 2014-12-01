package srt.tool;

import java.util.List;
import java.util.Set;



import srt.ast.AssertStmt;
import srt.ast.AssignStmt;

public class SMTLIBQueryBuilder {

	private ExprToSmtlibVisitor exprConverter;
	private CollectConstraintsVisitor constraints;
	private String queryString = "";

	public SMTLIBQueryBuilder(CollectConstraintsVisitor ccv) {
		this.constraints = ccv;
		this.exprConverter = new ExprToSmtlibVisitor();
	}

	public void buildQuery() {
		Set<String> variable = constraints.variableNames;
		List<AssignStmt> transitionNodes = constraints.transitionNodes;
		StringBuilder query = new StringBuilder();
		query.append("(set-logic QF_BV)\n"
				+ "(define-fun tobv32 ((p Bool)) (_ BitVec 32) (ite p (_ bv1 32) (_ bv0 32)))\n"
				+ "(define-fun tobool ((p (_ BitVec 32))) (Bool) (not (= p (_ bv0 32))))\n");
		// TODO: Define more functions above (for convenience), as needed.

		// TODO: Declare variables, add constraints, add properties to check
		// here.

		// Declare Variables
		for (String var : variable) {
			String v = "(declare-fun " + var + " () (_ BitVec 32))\n";
			query.append(v);
		}
		// Add constraints
		for (AssignStmt a : transitionNodes) {
			String lhs = a.getLhs().getName();
			String rhs = exprConverter.visit(a.getRhs());
			String c = "(assert (= " + lhs + " " + rhs + ")"
					+ ")";
			query.append(c+ "\n");
		}

		// Add assertion checks.
		int currentPropertyIndex = 0;
		for (AssertStmt assertStmt : constraints.propertyNodes) {
			String varName = assertStmt.getName() != null ? assertStmt.getName() : ("prop" + currentPropertyIndex++);
			query.append("(define-fun " + varName + " () (Bool) (not (tobool " + exprConverter.visit(assertStmt.getCondition()) + ")))\n");
			currentPropertyIndex++;
		}
		
		// Verify properties.
		query.append("(assert");
		for (int i = 0; i < currentPropertyIndex; i++) {
			query.append(" (or prop" + i);
		}	
		for (int i = 0; i <= currentPropertyIndex; i++) {
			query.append(")");
		}		
		query.append("\n");
		
		// Check.
		query.append("(check-sat)\n");
		query.append("(get-value (");
		for (int i = 0; i < currentPropertyIndex -1; i++) {
			query.append("prop" + i + " ");
		}
		if (currentPropertyIndex > 0) query.append("prop" + (currentPropertyIndex -1));
		query.append("))\n");
		
		queryString = query.toString();
	}

	public String getQuery() {
		return queryString;
	}

}
