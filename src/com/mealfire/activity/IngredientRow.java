package com.mealfire.activity;

import com.mealfire.model.Ingredient;
import com.mealfire.model.IngredientGroup;

public class IngredientRow {
	public static final int TYPE_GROUP = 0;
	public static final int TYPE_INGREDIENT = 1;
	
	public Ingredient ingredient;
	public IngredientGroup ingredientGroup;
	public boolean checked = false;
	
	public IngredientRow(Ingredient ingredient) {
		this.ingredient = ingredient;
	}
	
	public IngredientRow(IngredientGroup ingredientGroup) {
		this.ingredientGroup = ingredientGroup;
	}
	
	public String toString() {
		if (ingredient != null) {
			return ingredient.toString();
		} else {
			return ingredientGroup.getName();
		}
	}
	
	public int getType() {
		if (ingredient != null) {
			return TYPE_INGREDIENT;
		} else {
			return TYPE_GROUP;
		}
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode() + getType();
	}
	
	@Override
	public boolean equals(Object o) {
		return this.hashCode() == o.hashCode();
	}
}
