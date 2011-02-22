package com.mealfire.api;

import org.json.JSONException;

public interface DataRunnable<T> {
	public void run(T data) throws JSONException;
}
