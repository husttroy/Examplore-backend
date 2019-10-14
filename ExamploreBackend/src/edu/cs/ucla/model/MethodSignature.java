package edu.cs.ucla.model;

import java.util.ArrayList;

public class MethodSignature {
	public String name;
	public String rcvType;
	public ArrayList<String> argType;
	public int count;
	
	public MethodSignature(String name, String rcvType, ArrayList<String> argType) {
		this.name = name;
		this.rcvType = rcvType;
		this.argType = argType;
		this.count = 1;
	}

	@Override
	public int hashCode() {
		int hash = 37;
		hash += 13 * name.hashCode();
		hash += 17 * rcvType.hashCode();
		hash += 29 * argType.hashCode();
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MethodSignature) {
			MethodSignature that = (MethodSignature) obj;
			return that.name.equals(name) && that.rcvType.equals(rcvType) && that.argType.equals(argType);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		String s = rcvType + "." + name + "(";
		String tmp = "";
		if(!argType.isEmpty()) {
			for(String arg : argType) {
				tmp += arg + ",";
			}
			tmp = tmp.substring(0, tmp.length() - 1);
		}
		
		s += tmp + ")";				
		return s; 
	}
}
