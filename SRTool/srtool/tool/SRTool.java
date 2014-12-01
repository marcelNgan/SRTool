package srt.tool;

public interface SRTool {
	
	public static enum SRToolResult {
		CORRECT, INCORRECT, UNKNOWN
	}
	
	public SRToolResult go() throws Exception;
	
}
