package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

public class Institution {

	@SerializedName("institutionId")
	public long id;

	public long newsFeedId;

	@SerializedName(value="institutionName", alternate = {"name"})
	public String name;

	public String state;
	public String campus;
	public String country;
}
