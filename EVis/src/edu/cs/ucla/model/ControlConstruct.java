package edu.cs.ucla.model;

public class ControlConstruct implements Item{
	public String type;
	public String guard;
	
	public ControlConstruct(String type, String guard) {
		this.type = type;
		this.guard = guard;
	}
	
	@Override
	public int hashCode() {
		int hash = 31;
		hash += type.hashCode() * 17 + 11;
		hash += guard.hashCode() * 17 + 11;
		return hash;
	}
	
	@Override
	public String toString() {
		return this.type + "@" + this.guard;
	}
}	
