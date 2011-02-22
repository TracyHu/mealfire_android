package com.mealfire.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Ingredient {
	private String string;

	public Ingredient(JSONObject obj) throws JSONException {
		this.string = obj.getString("string");
	}
	
	public String toString() { return string; }
}
