package com.mealfire.activity;

public class IngredientRow {
	public String text;
	public String type;
	
	public IngredientRow(String text, String type) {
		this.text = text;
		this.type = type;
	}
	
	public String toString() {
		return text;
	}
	
	public int hashCode() {
		return (type + "-" + text).hashCode();
	}
}
