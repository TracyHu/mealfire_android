package com.mealfire.api;

import org.json.JSONArray;
import org.json.JSONException;

public interface DataTransformer<T> {
	public T transform(JSONArray array) throws JSONException;
}
