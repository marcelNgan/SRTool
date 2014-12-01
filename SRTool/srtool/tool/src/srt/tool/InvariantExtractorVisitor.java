package srt.tool;

import java.util.HashSet;
import java.util.Set;
import srt.ast.Decl
import srt.ast.IntLiteral;
import srt.ast.visitor.impl.DefaultVisitor;

public class InvariantExtractorVisitor extends DefaultVisitor {

	private Set<String> variableNames = new HashSet<>();
	private Set<Integer> intLiterals = new HashSet<>();

	public InvariantExtractorVisitor() {
		super(false);
	}

	@Override
	public Object visit(Decl decl) {
		variableNames.add(decl.getName());
		return decl;
	}
	
	public Object visit(IntLiterals intLiterals) {
		intLiterals.add(intLiterals.getValue());
		return intLiterals;
	}
	
	public Set<String> getVariableNames() {
		return variableNames;
	}
	
	public Set<Integer> getIntLiterals(){
		return intLiterals;
	}	
}