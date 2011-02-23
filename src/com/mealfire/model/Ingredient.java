package com.mealfire.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Ingredient {
	private String string;
	private String food;
	
	public Ingredient(JSONObject obj) throws JSONException {
		this.string = obj.getString("string");
		
		if (obj.has("food")) {
			this.food = obj.getString("food");
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o.getClass() == Ingredient.class) {
			return this.food.equals(((Ingredient) o).food);
		}
		return false;
	}
	
	public String toString() { return string; }
	public String getFood() { return food; }
}
