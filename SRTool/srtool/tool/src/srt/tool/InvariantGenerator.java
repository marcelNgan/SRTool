package srt.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import srt.ast.BinaryExpr;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.IntLiteral;
import srt.ast.Invariant;

public class InvariantGenerator {

	public static List<Invariant> generate(Set<String> variableNames, Set<Integer> intLiterals) {
		intLiterals.add(0);
		List<Invariant> invariants = new ArrayList<invariants>();
		
		for (String variableA : variableNames) {
			DeclRef a = new DeclRef(variableA);
			
			for (String variableB : variableNames) {
				if (variableA.equals(variableB)) {
					continue;
				}
				
				DeclRef b = new DeclRef(variableB);
				
				invariants.add(new Invariant(true, (new BinaryExpr(BinaryExpr.LT, a, b))));
				invariants.add(new Invariant(true, (new BinaryExpr(BinaryExpr.LEQ, a, b))));
				invariants.add(new Invariant(true, (new BinaryExpr(BinaryExpr.EQUAL, a, b))));
			}
			
			for (Integer intLiteral : intLiterals) {
				IntLiteral n = new IntLiteral(intLiteral);
				
				invariants.add(new Invariant(true, (new BinaryExpr(BinaryExpr.LT, a, n))));
				invariants.add(new Invariant(true, (new BinaryExpr(BinaryExpr.LEQ, a, n))));
				invariants.add(new Invariant(true, (new BinaryExpr(BinaryExpr.EQUAL, a, n))));
				invariants.add(new Invariant(true, (new BinaryExpr(BinaryExpr.GEQ, a, n))));
				invariants.add(new Invariant(true, (new BinaryExpr(BinaryExpr.GT, a, n))));
			}
		}
		
		invariants.add(new Invariant(true, (new BinaryExpr(BinaryExpr.LT, new DeclRef("i"), new IntLiteral(12)))));
		
		return invariants;
	}
}