package com.mealfire.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mealfire.api.API;
import com.mealfire.api.DataTransformer;

public class Store {
	private int id;
	private String name;
	
	public Store(JSONObject obj) throws JSONException {
		id = obj.getInt("id");
		name = obj.getString("name");
	}
	
	public static API<ArrayList<Store>> all() {
		return new API<ArrayList<Store>>("me/stores", new StoresTransformer());
	}
	
	public int getId() { return id; }
	public String getName() { return name; }
	
	private static class StoresTransformer implements DataTransformer<ArrayList<Store>> {
		public ArrayList<Store> transform(JSONArray array) throws JSONException {
			ArrayList<Store> stores = new ArrayList<Store>();
			JSONArray items = array.getJSONArray(1);
			
			for (int i = 0; i < items.length(); i++) {
				stores.add(new Store(items.getJSONObject(i)));
			}
			
			return stores;
		}
	}
}
