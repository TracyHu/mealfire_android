package com.mealfire.api;

import org.json.JSONArray;
import org.json.JSONException;

public class StringTransformer implements DataTransformer<String> {
	public String transform(JSONArray array) throws JSONException {
		return array.getString(1);
	}
}
