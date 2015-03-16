package com.eulersbridge.isegoria;

public class InstitutionInfo {
	private String id;
	private String institution;
	
	public InstitutionInfo(String id, String institution) {
		this.id = id;
		this.institution = institution;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getInstitution() {
		return institution;
	}
	public void setInstitution(String institution) {
		this.institution = institution;
	}
}
