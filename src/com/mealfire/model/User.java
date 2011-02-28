package com.mealfire.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mealfire.api.API;
import com.mealfire.api.DataTransformer;
import com.mealfire.api.StringTransformer;

public class User {
	private static String token;
	
	public static boolean isLoggedIn() {
		return token != null;
	}
	
	public static API<String> authorize(String email, String password) {
		API<String> api = new API<String>("authorize", new StringTransformer());
		api.setParameter("email", email);
		api.setParameter("password", password);
		api.dontSetToken();
		return api;
	}
	
	public static API<String> validateToken() {
		return new API<String>("validate", new StringTransformer());
	}
	
	public static String getToken() {
		return token;
	}
	
	public static void setToken(String token) {
		User.token = token;
	}
	
	public static API<Stats> getUserStats() {
		return new API<Stats>("me/stats", new StatsTransformer());
	}
	
	public static class Stats {
		private int recipeCount;
		private IngredientList latestList;
		private int calendarCount;
		private int extraItemsCount;
		
		public Stats(JSONObject obj) throws JSONException {
			recipeCount = obj.getInt("recipe_count");
			calendarCount = obj.getInt("calendar_count");
			extraItemsCount = obj.getInt("extra_items_count");
			
			if (!obj.isNull("latest_list")) {
				latestList = new IngredientList(obj.getJSONObject("latest_list"));
			}
		}
		
		public int getRecipeCount() { return recipeCount; }
		public IngredientList getLatestList() { return latestList; }
		public int getCalendarCount() { return calendarCount; }
		public int getExtraItemsCount() { return extraItemsCount; }
	}
	
	private static class StatsTransformer implements DataTransformer<Stats> {
		public Stats transform(JSONArray array) throws JSONException {
			return new Stats(array.getJSONObject(1));
		}
	}
}
