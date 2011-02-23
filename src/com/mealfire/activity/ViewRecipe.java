package com.mealfire.activity;

import java.util.ArrayList;

import org.json.JSONException;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mealfire.R;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.Ingredient;
import com.mealfire.model.IngredientGroup;
import com.mealfire.model.Recipe;

public class ViewRecipe extends MealfireActivity {
	private Recipe recipe;
	private ArrayList<IngredientRow> rows = new ArrayList<IngredientRow>();
	private LinearLayout ingredients;
	private WebView directions;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.recipe);
		ingredients = (LinearLayout) findViewById(R.id.ingredients);
		directions = (WebView) findViewById(R.id.directions);
		
		API<Recipe> api = Recipe.getRecipe(getIntent().getExtras().getInt("recipeID"));
		api.setParameter("include", "ingredient_groups[ingredients],directions");
		api.setActivity(this);
		
		api.setSuccessHandler(new DataRunnable<Recipe>() {
			public void run(Recipe recipe) throws JSONException {
				ViewRecipe.this.recipe = recipe;
				
				for (int i = 0; i < recipe.getIngredientGroups().size(); i++) {
					IngredientGroup ig = recipe.getIngredientGroups().get(i);
					
					if (ig.getName() != null && !ig.getName().equals("") || i > 0) {
						rows.add(new IngredientRow(ig));
					}
					
					for (Ingredient ingredient : ig.getIngredients()) {
						rows.add(new IngredientRow(ingredient));
					}
				}
				
				runOnUiThread(new Runnable() {
					public void run() {
						setup();
					}
				});
			}
		});
		
		api.run();
	}
	
	private void setup() {
		setTitle(recipe.getName());
		
		// Show the ingredients.
		for (IngredientRow row : rows) {
			if (row.getType() == IngredientRow.TYPE_INGREDIENT) {
				View view = getLayoutInflater().inflate(R.layout.row, null);
				TextView textView = (TextView) view.findViewById(R.id.text);
				textView.setText(row.toString());
				ingredients.addView(view);
			} else {
				View view = getLayoutInflater().inflate(R.layout.row_header, null);
				TextView textView = (TextView) view.findViewById(R.id.header_text);
				textView.setText(row.toString());
				ingredients.addView(view);
			}
		}
		
		// Show the directions.
		directions.loadDataWithBaseURL("http://mealfire.com",
			recipe.getDirections(), "text/html", "utf-8", null);
	}
}
