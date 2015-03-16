package com.eulersbridge.isegoria;

import java.util.Vector;

public class CountryInfo {
	private String country;
	private Vector<InstitutionInfo> institutions;
	
	public CountryInfo(String country) {
		this.country = country;
		this.institutions = new Vector<InstitutionInfo>();
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
	public void addInstituion(String id, String institution) {
		institutions.add(new InstitutionInfo(id, institution));
	}

	public Vector<InstitutionInfo> getInstitutions() {
		return institutions;
	}

	public void setInstitutions(Vector<InstitutionInfo> institutions) {
		this.institutions = institutions;
	}
}
