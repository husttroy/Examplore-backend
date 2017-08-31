package edu.cs.ucla.model;

import java.util.ArrayList;

public class APICall implements Item {
	public String name;
	public String originalGuard;
	public String normalizedGuard;
	public String receiver; // null if does not have receiver, e.g., constructor
	public String ret; // null if does not have return value
	public ArrayList<String> arguments; // empty if does not have arguments
	
	public APICall(String name, String originalGuard, String normalizedGuard, String receiver, ArrayList<String> args, String retVal) {
		this.name = name;
		this.originalGuard = originalGuard;
		this.normalizedGuard = normalizedGuard;
		this.receiver = receiver;
		this.arguments = args;
		this.ret = retVal;
	}
	
	@Override
	public String toString() {
		return name + "@" + normalizedGuard;
	}
	
	@Override
	public int hashCode() {
		int hash = 31;
		hash += 37 * this.name.hashCode();
		hash += 43 * this.normalizedGuard.hashCode();
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof APICall) {
			APICall call = (APICall)obj;
			return this.name.equals(call.name) && this.normalizedGuard.equals(call.normalizedGuard);
		} else {
			return false;
		}
	} 
}
