package com.mealfire.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IngredientGroup {
	private ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
	private String name;

	public IngredientGroup(JSONObject obj) throws JSONException {
		this.name = obj.getString("name");
		JSONArray jsonArray = obj.getJSONArray("ingredients");
		
		for (int i = 0; i < jsonArray.length(); i++) {
			ingredients.add(new Ingredient(jsonArray.getJSONObject(i)));
		}
		
		if (this.name.equals("null"))
			this.name = null;
	}
	
	public ArrayList<Ingredient> getIngredients() { return ingredients; }
	public String getName() { return name; }
}
