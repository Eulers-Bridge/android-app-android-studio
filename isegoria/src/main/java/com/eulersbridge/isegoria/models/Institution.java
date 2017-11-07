package com.eulersbridge.isegoria.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Institution {
	private String id;
	private String name;

	Institution(JSONObject jsonObject) {
		try {
			this.id = jsonObject.getString("institutionId");
			this.name = jsonObject.getString("institutionName");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
