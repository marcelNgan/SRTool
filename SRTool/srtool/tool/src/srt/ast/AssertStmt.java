package srt.ast;

public class AssertStmt extends Stmt {
		
	private String name;
	
	public AssertStmt(Expr condition) {
		this(condition, null, null);
	}
	
	public AssertStmt(Expr condition, String name) {
		this(condition, name, null);
	}
	
	public AssertStmt(Expr condition, NodeInfo nodeInfo) {
		this(condition, null, nodeInfo);
	}
	
	public AssertStmt(Expr condition, String name, NodeInfo nodeInfo) {
		super(nodeInfo);
		children.add(condition);
		this.name = name;
	}
	
	public Expr getCondition() {
		return (Expr) children.get(0);
	}
	
	public String getName() {
		return name;
	}
	
	public Invariant getInvariant() {
		return (Invariant) children.get(1);
	}
}
