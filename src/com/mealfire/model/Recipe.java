package com.mealfire.model;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mealfire.PagedList;
import com.mealfire.api.API;
import com.mealfire.api.DataTransformer;
import com.mealfire.api.StringTransformer;

public class Recipe {
	private int id;
	private String name;
	private String directions;
	private String imageURL;
	private ArrayList<IngredientGroup> ingredientGroups = new ArrayList<IngredientGroup>();
	
	public Recipe(JSONObject obj) throws JSONException {
		name = obj.getString("name");
		id = obj.getInt("id");
		
		if (obj.has("image_thumb"))
			imageURL = obj.getString("image_thumb");
		
		if (imageURL != null && imageURL.equals("null"))
			imageURL = null;
		
		if (obj.has("ingredient_groups")) {
			JSONArray jsonArray = obj.getJSONArray("ingredient_groups");
			
			for (int i = 0; i < jsonArray.length(); i++) {
				ingredientGroups.add(new IngredientGroup(jsonArray.getJSONObject(i)));
			}
		}
		
		if (obj.has("directions")) {
			directions = obj.getString("directions");
		}
	}
	
	public API<String> schedule(DateTime day) {
		API<String> api = new API<String>(
			String.format("me/recipes/%d/schedule", id),
			new StringTransformer());
		
		api.setParameter("year", day.getYear());
		api.setParameter("month", day.getMonthOfYear());
		api.setParameter("day", day.getDayOfMonth());
		
		return api;
	}
	
	public static API<PagedList<Recipe>> searchRecipes(String query) {
		API<PagedList<Recipe>> api = new API<PagedList<Recipe>>(
			"me/recipes/search",
			new RecipesTransformer());
		
		api.setParameter("q", query);
		return api;
	}
	
	public static API<PagedList<Recipe>> getRecipes() {
		return new API<PagedList<Recipe>>("me/recipes", new RecipesTransformer());
	}
	
	public static API<Recipe> getRecipe(int id) {
		return new API<Recipe>("me/recipes/" + id, new RecipeTransformer());
	}
	
	public String getName() { return name; }
	public Integer getID() { return id; }
	public String getDirections() { return directions; }
	public String getImageURL() { return imageURL; }
	public ArrayList<IngredientGroup> getIngredientGroups() { return ingredientGroups; }
	
	private static class RecipesTransformer implements DataTransformer<PagedList<Recipe>> {
		public PagedList<Recipe> transform(JSONArray array) throws JSONException {
			JSONArray results = array.getJSONObject(1).getJSONArray("results");
			ArrayList<Recipe> recipes = new ArrayList<Recipe>();
			
			for (int i = 0; i < results.length(); i++) {
				Recipe r = new Recipe(results.getJSONObject(i));
				recipes.add(r);
			}
						
			return new PagedList<Recipe>(recipes, array.getJSONObject(1).getInt("total"));
		}
	}
	
	private static class RecipeTransformer implements DataTransformer<Recipe> {
		public Recipe transform(JSONArray array) throws JSONException {
			return new Recipe(array.getJSONObject(1));
		}
	}
}
