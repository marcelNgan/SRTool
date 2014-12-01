package srt.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.Decl;
import srt.ast.DeclList;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.ast.visitor.impl.PrinterVisitor;

public class HoudiniLoopUnwinderVisitor extends DefaultVisitor {
    
    private int id = 0;
    private List<List<Invariant>> candidateLoopInvariants = new ArrayList<List<Invariant>>();
    private List<List<Invariant>> loopInvariants = new ArrayList<List<Invariant>>();
    
	public HoudiniLoopUnwinderVisitor(WhileStmt whileStmt) {
		List<Invariant> candidates = new ArrayList<Invariant>();
    	List<Invariant> invariants = new ArrayList<Invariant>();
    	for(Invariant invariant : whileStmt.getInvariantList().getInvariants()) {
    		if (invariant.isCandidate()) {
           		candidates.add(invariant);
           	} else {
           		invariants.add(invariant);
           	}
    	}
    	loopInvariants.add(invariants);
        candidateLoopInvariants.add(candidates);
		super(true);
	}

    @Override
    public Object visit(WhileStmt whileStmt) {
    	
		int tempid = id++;
		
    	List<Stmt> stmts = new ArrayList<Stmt>();
    	int inv = 0;
    	int cand = 0;
    	
    	for (Invariant invariant : loopInvariants.get(tempid)) {
    		stmts.add(new AssertStmt(invariant.getExpr(), "inv-" + tempid + "-" + inv++ + "-pre"));
    	}
    	for (Invariant invariant : candidateLoopInvariants.get(tempid)) {
    		stmts.add(new AssertStmt(invariant.getExpr(), "cand-" + tempid + "-" + cand++ + "-pre"));	
    	}
        
        BlockStmt body = (BlockStmt) super.visit(whileStmt.getBody());
        
        inv = 0;
        cand = 0;
        List<Stmt> loopBodyStatements = new ArrayList<Stmt>();
        loopBodyStatements.add(body);
    	for (Invariant invariant : loopInvariants.get(tempid)) {
    		loopBodyStatements.add(new AssertStmt(invariant.getExpr(), "inv-" + tempid + "-" + inv++ + "-post"));
    	}
    	for (Invariant invariant : candidateLoopInvariants.get(tempid)) {
    		loopBodyStatements.add(new AssertStmt(invariant.getExpr(), "cand-" + tempid + "-" + cand++ + "-post"));	
    	}
    	
    	List<Invariant> invariantsToAdd = new ArrayList<Invariant>();
    	invariantsToAdd.addAll(loopInvariants.get(tempid));
    	invariantsToAdd.addAll(candidateLoopInvariants.get(tempid));
    	
    	BlockStmt loopBody = new BlockStmt(loopBodyStatements);
    	WhileStmt loopStmt = new WhileStmt(whileStmt.getCondition(), whileStmt.getBound(), new InvariantList(invariantsToAdd), loopBody);

        stmts.add(loopStmt);

        return new BlockStmt(stmts);
    }
    
    public void setCandidates(List<Set<Integer>> candidates) {
    	List<List<Invariant>> newCandidates = new ArrayList<List<Invariant>>();
    	
    	int i = 0;
    	for (Set<Integer> validCandidateIndices : candidates) {
    		List<Invariant> currentCandidates = new ArrayList<Invariant>();
    		for (Integer validIndex : validCandidateIndices) {
    			currentCandidates.add(candidateLoopInvariants.get(i).get(validIndex));
    		}
    		newCandidates.add(currentCandidates);
    		i++;
    	}
    	candidateLoopInvariants = newCandidates;
    }
    
    public List<List<Invariant>> getAllInvariants() {
    	List<List<Invariant>> invariants = new ArrayList<List<Invariant>>();
    	
    	for (int i = 0; i < loopInvariants.size(); i++) {
    		List<Invariant> currentInvariants = new ArrayList<Invariant>();
    		invariants.addAll(loopInvariants.get(i));
    		invariants.addAll(candidateLoopInvariants.get(i));
    		invariants.add(currentInvariants);
    	}
    	
    	return invariants;
    }
    
    public Boolean noLoops() {
    	return loopInvariants.isEmpty();
    }
    
    public int loopCount () {
    	return id;
    }
	
	public void reset() {
		id = 0;
	}
}