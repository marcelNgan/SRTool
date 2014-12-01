package srt.ast;

import java.util.List;

public class InvariantList extends Node {
	
	public InvariantList(Invariant[] invars) {
		this(invars, null);
	}
	
	public InvariantList(List<Invariant> invars) {
		this(invars.toArray(new Invariant[invars.size()]), null);
	}
	
	public InvariantList(Invariant[] invars, NodeInfo nodeInfo) {
		super(nodeInfo);
		for(Invariant i : invars) {
			children.add(i);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Invariant> getInvariants() {
		return (List<Invariant>) children.clone();
	}
	
	public void setInvariants(List<Invariant> invariants){
		children.clear();
		children.addAll(invariants);
	}
}
