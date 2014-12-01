package srt.tool;

import srt.ast.Program;
import srt.ast.visitor.impl.Checker;
import srt.ast.visitor.impl.MakeBlockVisitor;
import srt.parser.SimpleCParserUtil;
import srt.tool.SRTool.SRToolResult;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Parse command line args.
		CLArgs clArgs = new CLArgs();
		JCommander jcommander = new JCommander(clArgs);
		try {
			jcommander.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			jcommander.usage();
			System.exit(1);
		}
		if (clArgs.help || clArgs.files.size() != 1) {
			jcommander.usage();
			System.exit(1);
		}
		
		String inputFile = clArgs.files.get(0);
		
		// Parse input Simple C file to AST.
		Program p = SimpleCParserUtil.createAST(inputFile);

		// Add blocks to make things simpler.
		// E.g. if(c) stmt; becomes if(c) {stmt;} else {}
		p = (Program) new MakeBlockVisitor().visit(p);

		// Do basic checks.
		// E.g. Variables declared before use,
		// no duplicate local variables.
		Checker checker = new Checker();
		boolean success = checker.check(p);
		if (!success) {
			throw checker.getCheckerError();
		}
		
		SRTool tool = new SRToolImpl(p, clArgs);
		SRToolResult result = tool.go();
		
		// output result: "correct", "incorrect" or "unknown"
		System.out.println(result.toString().toLowerCase());
	}

}
