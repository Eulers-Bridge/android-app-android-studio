package com.eulersbridge.isegoria.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class Country {
	private String name;
	private Vector<Institution> institutions = new Vector<>();

	public Country(JSONObject jsonObject) {
		try {
			name = jsonObject.getString("countryName");

			JSONArray institutionsArray = jsonObject.getJSONArray("institutions");
			for (int j = 0; j < institutionsArray.length(); j++) {
				JSONObject institutionObject = institutionsArray.getJSONObject(j);

				Institution institution = new Institution(institutionObject);
				institutions.add(institution);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Country(String country) {
		this.name = country;
	}

	public String getName() {
		return name;
	}

	public Vector<Institution> getInstitutions() {
		return institutions;
	}
}
