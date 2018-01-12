package com.eulersbridge.isegoria.network.api.models;

import com.squareup.moshi.Json;

import java.util.List;

public class Country {

	@Json(name = "countryId")
	public long id;

	@Json(name = "countryName")
	public String name;
	public List<Institution> institutions;

	@Override
	public String toString() {
		return name;
	}
}
