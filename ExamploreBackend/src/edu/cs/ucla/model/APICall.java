package edu.cs.ucla.model;

import java.util.ArrayList;

public class APICall implements Item {
	public String name;
	public String originalGuard;
	public String normalizedGuard;
	public String receiver; // null if does not have receiver, e.g., constructor
	public String ret; // null if does not have return value
	public ArrayList<String> arguments; // empty if does not have arguments
	
	public int startIndex = -1;
	public int endIndex = -1;
	
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
		String apiName = name.substring(0, name.indexOf('(')) + "(";
		if(!arguments.isEmpty()) {
			for(String arg : arguments) {
				apiName += arg + ", ";
			}
			apiName = apiName.substring(0, apiName.length() - 2);
		}
		
		apiName += ")";
		
		return apiName + "@" + normalizedGuard;
	}
	
	@Override
	public int hashCode() {
		int hash = 31;
		hash += 37 * this.name.hashCode();
		hash += 43 * this.normalizedGuard.hashCode();
		hash += 51 * this.arguments.hashCode();
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof APICall) {
			APICall call = (APICall)obj;
			return this.name.equals(call.name) && this.normalizedGuard.equals(call.normalizedGuard)
					&& this.arguments.equals(call.arguments);
		} else {
			return false;
		}
	} 
}
