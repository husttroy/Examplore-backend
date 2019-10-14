package edu.cs.ucla.model;

public class ControlConstruct implements Item{
	public String type;
	public String guard;
	public String orgnGuard;
	
	// keyword index
	public int startIndex1 = -1;
	public int endIndex1 = -1;
	
	// block index
	public int startIndex2 = -1;
	public int endIndex2 = -1;
	
	public ControlConstruct(String type, String guard) {
		this.type = type;
		this.guard = guard;
		this.orgnGuard = null;
	}
	
	public ControlConstruct(String type, String normalizedGuard, String originalGuard) {
		this.type = type;
		this.guard = normalizedGuard;
		this.orgnGuard = originalGuard;
	}
	
	@Override
	public int hashCode() {
		int hash = 31;
		hash += type.hashCode() * 17 + 11;
		if(guard != null) {
			hash += guard.hashCode() * 17 + 11;
		}
		
		if(orgnGuard != null) {
			hash += orgnGuard.hashCode() * 17 + 11;
		}
		
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ControlConstruct) {
			ControlConstruct other = (ControlConstruct) obj;
			if(this.guard == null) {
				return this.type.equals(other.type) && other.guard == null;
			}
			
			if(this.orgnGuard == null) {
				return this.guard.equals(other.guard) && this.type.equals(other.type) && other.orgnGuard == null;
			} else {
				return this.guard.equals(other.guard) && this.type.equals(other.type) && this.orgnGuard.equals(other.orgnGuard);
			}
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return this.type + "@" + this.guard;
	}
}	
