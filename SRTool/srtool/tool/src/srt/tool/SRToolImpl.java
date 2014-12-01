package srt.tool;

import java.io.IOException;

import srt.ast.*
import srt.ast.Program;
import srt.ast.visitor.impl.AssignmentVisitor;
import srt.ast.visitor.impl.PrinterVisitor;
import srt.exec.ProcessExec;
import srt.tool.exception.ProcessTimeoutException;

public class SRToolImpl implements SRTool {
	private Program program;
	private CLArgs clArgs;

	public SRToolImpl(Program p, CLArgs clArgs) {
		this.program = p;
		this.clArgs = clArgs;
	}

	public SRToolResult go() throws IOException, InterruptedException {
		if(clArgs.mode.equals(CLArgs.INVGEN)){
			InvariantExtractorVisitor invariantExtractor = new InvariantExtractorVisitor();
			invariantExtractor.visit(program);
			
			set<String> variableNames = invariantExtractor.getVariableNames();
			set<Integer> intLiterals = invariantExtractor.getIntLiterals();			
			List<Invariant> invariantList = InvariantGenerator.generate(variableNames, intLiterals);
			program = (Program) InvGenAddVisitor(invariantList).visit(program);
		}

		if (clArgs.mode.equals(CLArgs.HOUDINI) || clArgs.mode.equals(CLArgs.INVGEN)) {
    		// Extract all loops
			HoudiniLoopUnwinderVisitor LoopUnwinder = new HoudiniLoopUnwinderVisitor(program);
			Program loopProgram = (Program) LoopUnwinder.visit(program);
			
			if (!LoopUnwinder.noLoops()) {
				boolean hasFailedCandidate;
				List<Set<Integer>> preTrueCandidates = new ArrayList<Set<Integer>>();
				List<Set<Integer>> postTrueCandidates = new ArrayList<Set<Integer>>();
				int loopCount = LoopUnwinder.loopCount();
				for (int i = 0; i < loopCount; i++) {
					preTrueCandidates.add(new HashSet<Integer>());
					postTrueCandidates.add(new HashSet<Integer>());
				}
				
				do {
					LoopUnwinder.reset();
					hasFailedCandidate = false;
					preTrueCandidates = new ArrayList<Set<Integer>>();
					postTrueCandidates = new ArrayList<Set<Integer>>();
					for (int i = 0; i < loopCount; i++) {
						preTrueCandidates.add(new HashSet<Integer>());
						postTrueCandidates.add(new HashSet<Integer>());
					}
					
					Program newLoopProgram;
					newLoopProgram = (Program) new LoopAbstractionVisitor().visit(loopProgram);
					newLoopProgram = (Program) new PredicationVisitor().visit(newLoopProgram);
					newLoopProgram = (Program) new SSAVisitor().visit(newLoopProgram);

					String smtQuery = buildSMTQuery(newLoopProgram);
					ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
					String queryResult = "";
					try {
						queryResult = process.execute(smtQuery, clArgs.timeout);
					} catch (ProcessTimeoutException e) {
						if (clArgs.verbose) {
							System.out.println("Timeout!");
						}
						return SRToolResult.UNKNOWN;
					}
					
					if (queryResult.startsWith("sat")) {
						Pattern p = Pattern.compile("([\\w-]+ \\w+)");
						Matcher m = p.matcher(queryResult);
						
						while (m.find()) {
							String match = m.group();
							String[] matches = match.split(" ");
							String invariantName = matches[0];
							
							
							if (invariantName.startsWith("cand")) {
								if (matches[1].equals("true")) {
									hasFailedCandidate = true;
								} else if (matches[1].equals("false")) {
									String[] splitString = matches[0].split("-");
									int loopId = Integer.parseInt(splitString[1]);
									int invId = Integer.parseInt(splitString[2]);
									if(splitString[3].equals("pre")) {
										preTrueCandidates.get(loopId).add(invId);			
									} else if (splitString[3].equals("post")) {
										postTrueCandidates.get(loopId).add(invId);
									}

								}
							}
						}


						List<Set<Integer>> trueCandidates = new ArrayList<Set<Integer>>();
						for(int i = 0; i < loopCount; i++) {
							postTrueCandidates.get(i).retainAll(preTrueCandidates.get(i));
							trueCandidates.add(postTrueCandidates.get(i));
						}
						LoopUnwinder.setCandidates(trueCandidates);
						loopProgram = (Program) LoopUnwinder.visit(program);
					}
	
				} while (hasFailedCandidate);

				List<List<Invariant>> invariantsLists = LoopUnwinder.getAllInvariants();				
				program = (Program) new HoudiniVisitor(invariantsLists).visit(program);
			}
		}

		if (clArgs.mode.equals(CLArgs.BMC)) {
			program = (Program) new LoopUnwinderVisitor(clArgs.unsoundBmc,
					clArgs.unwindDepth).visit(program);
		} else {
			program = (Program) new LoopAbstractionVisitor().visit(program);
		}
		program = (Program) new PredicationVisitor().visit(program);
		program = (Program) new SSAVisitor().visit(program);
		
		// Output the program as text after being transformed (for debugging).
		if (clArgs.verbose) {
			String programText = new PrinterVisitor().visit(program);
			System.out.println(programText);
		}

		// Collect the constraint expressions and variable names.
		CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
		ccv.visit(program);

		// TODO: Convert constraints to SMTLIB String.
		SMTLIBQueryBuilder builder = new SMTLIBQueryBuilder(ccv);
		builder.buildQuery();

		String smtQuery = builder.getQuery();

		// Output the query for debugging
		if (clArgs.verbose) {
			System.out.println(smtQuery);
		}

		// Submit query to SMT solver.
		// You can use other solvers.
		// E.g. The command for cvc4 is: "cvc4", "--lang", "smt2"
		ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
		String queryResult = "";
		try {
			queryResult = process.execute(smtQuery, clArgs.timeout);
		} catch (ProcessTimeoutException e) {
			if (clArgs.verbose) {
				System.out.println("Timeout!");
			}
			return SRToolResult.UNKNOWN;
		}

		// output query result for debugging
		if (clArgs.verbose) {
			System.out.println(queryResult);
		}

		if (queryResult.startsWith("unsat")) {
			return SRToolResult.CORRECT;
		}

		if (queryResult.startsWith("sat")) {
			return SRToolResult.INCORRECT;
		}
		// query result started with something other than "sat" or "unsat"
		return SRToolResult.UNKNOWN;
	}
	private class HoudiniVisitor extends DefaultVisitor {
		private List<List<Invariant>> invariantsList;
		private int id;
		public HoudiniVisitor(List<List<Invariant>> invariantsList) {
			super(true);
			this.invariantsList = invariantsList;
		}
		@Override
		public Object visit(WhileStmt whileStmt) {
			list<Invariant> invariants = invariantsList.get(id);
			whileStmt.getInvariantList().setInvariants(invariants);
			id++;
			return super.visit(whileStmt);
		}
	}
	
	private class InvGenAddVisitor extends DefaultVisitor {
		private List<Invariant> invariantsList;
		
		public InvGenAddVisitor(List<Invariant> invariantsList) {
			super(true);
			this.invariantsList = invariantsList;
		}
		@Override
		public Object visit(WhileStmt whileStmt) {
			list<Invariant> invariants = new ArrayList<Invariant>();
			invariants.addAll(whileStmt.getInvariantList().getInvariants());
			invariants.addAll(invariantsList);
			
			return new WhileStmt(whileStmt.getCondition(), whileStmt.getBound(),
				new InvariantList(invariants), (BlockStmt) super.visit(whileStmt.getBody()));
		}
	}
	
}
