package com.eulersbridge.isegoria.network.api.models;

import android.text.TextUtils;

import com.squareup.moshi.Json;

@SuppressWarnings("unused")
public class Institution {

	@Json(name = "institutionId")
	public long id;

	public long newsFeedId;

	// Different JSON names used depending on API endpoint.
    // Try to read both, keep private and use public getter to return whichever is non-null.
	private String institutionName;
	private String name;

	public String state;
	public String campus;
	public String country;

	public String getName() {
		if (TextUtils.isEmpty(institutionName)) {
			return name;

		} else {
			return institutionName;
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}
