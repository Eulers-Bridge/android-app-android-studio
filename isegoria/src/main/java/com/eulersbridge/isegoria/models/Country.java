package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Country {

	@SerializedName("countryId")
	public long id;

	@SerializedName("countryName")
	public String name;
	public List<Institution> institutions;

	public Country(String name) {
	    this.name = name;
    }

}
