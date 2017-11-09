package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

public class Institution {

	@Json(name = "institutionId")
	public long id;

	public long newsFeedId;

	//@Json(name = value="institutionName", alternate = {"name"})
	//TODO: Custom field
	@Json(name = "institutionName")
	public String name;

	public String state;
	public String campus;
	public String country;
}
