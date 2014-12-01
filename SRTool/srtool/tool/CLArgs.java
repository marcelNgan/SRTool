package srt.tool;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(separators = " =:")
public class CLArgs implements Cloneable {
	
	public static final String BMC = "bmc";
	public static final String VERIFIER = "verifier";
	public static final String HOUDINI = "houdini";
	public static final String INVGEN = "invgen";
	public static final String COMP = "comp";
	
	public static class ModeValidator implements IParameterValidator {
		@Override
		public void validate(String name, String value)
				throws ParameterException {
			if(!value.equals(BMC)
					&& !value.equals(VERIFIER)
					&& !value.equals(HOUDINI)
					&& !value.equals(INVGEN)
					&& !value.equals(COMP))
			{
				throw new ParameterException("Invalid mode");
			}
		}
	}
	
	@Parameter(description = "file")
	public List<String> files = new ArrayList<String>();
	
	@Parameter(names = {"-mode", "--mode", "-m"}, description = "Set the mode of tool." , help = true)
	public String mode = BMC;
	
	@Parameter(names = {"--help", "-help", "-h"}, description = "Show usage.", help = true)
	public boolean help;

	@Parameter(names = {"--verbose", "-verbose", "-v"}, description = "Show extra output.")
	public boolean verbose = false;
	
	@Parameter(names = { "--depth", "-depth", "-d" }, description = "Unwinding depth.")
	public Integer unwindDepth = 5;
	
	@Parameter(names = { "--timeout", "-timeout", "-t" }, description = "Timeout for SMT query in seconds.")
	public Integer timeout = 60;
	
	@Parameter(names = { "--unsound", "-unsound",
			"-us" }, description = "If this option is included and "
					+ "there are no assertion failures within the unwinding depth, "
					+ "then the bounded model checker will report 'correct', "
					+ "even if the unwinding depth is not large enough "
					+ "to guarantee that the program is correct.")
	public boolean unsoundBmc = false;

	@Override
	public CLArgs clone() throws CloneNotSupportedException {
		return (CLArgs) super.clone();
	}
	
	
	
}
