package com.mealfire.api;

import java.io.IOException;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;

import com.mealfire.UserException;

public abstract class AbstractInlineAPI {
	protected Hashtable<String, String> data = new Hashtable<String, String>();
	protected boolean shouldSetToken = true;
	
	public void setParameter(String key, String value) {
		data.put(key, value);
	}
	
	public void setParameter(String key, int value) {
		setParameter(key, Integer.toString(value));
	}
	
	public void dontSetToken() {
		shouldSetToken = false;
	}
	
	public abstract JSONArray run() throws JSONException, UserException, IOException;
}
