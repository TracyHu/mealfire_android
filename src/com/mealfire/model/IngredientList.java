package com.mealfire.model;

import java.io.IOException;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mealfire.UserException;
import com.mealfire.Utils;
import com.mealfire.api.API;
import com.mealfire.api.AbstractInlineAPI;
import com.mealfire.api.DataTransformer;
import com.mealfire.api.InlineAPI;
import com.mealfire.api.StringTransformer;

public class IngredientList {
	private int id;
	private DateTime createdAt;
	private ArrayList<IngredientGroup> ingredientGroups = new ArrayList<IngredientGroup>();
	
	public IngredientList(JSONObject obj) throws JSONException {
		DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
		id = obj.getInt("id");
		createdAt = parser.parseDateTime(obj.getString("created_at"));
		
		
		if (obj.has("ingredient_groups")) {
			JSONArray groups = obj.getJSONArray("ingredient_groups");
			
			for (int i = 0; i < groups.length(); i++) {
				ingredientGroups.add(new IngredientGroup(groups.getJSONObject(i)));
			}
		}
	}
	
	public IngredientList(JSONArray groups) throws JSONException {
		for (int i = 0; i < groups.length(); i++) {
			ingredientGroups.add(new IngredientGroup(groups.getJSONObject(i)));
		}
	}
	
	public static API<IngredientList> createList(ArrayList<DateTime> days) {
		ArrayList<String> dayStrings = new ArrayList<String>();
		
		for (DateTime day : days) {
			dayStrings.add(day.toString("YYYY-M-d"));
		}
		
		API<IngredientList> api = new API<IngredientList>(
			"me/lists/create",
			new IngredientListTransformer());
		
		api.setParameter("days", Utils.join(dayStrings, ","));
		
		return api;
	}
	
	public static API<IngredientList> getLatestList() {
		return new API<IngredientList>(new AbstractInlineAPI() {
			@Override
			public JSONArray run() throws JSONException, UserException, IOException {
				InlineAPI listID = new InlineAPI("me/lists");
				listID.setParameter("sort", "created_at");
				listID.setParameter("order", "desc");
				listID.setParameter("limit", 1);
				
				IngredientList list = new FirstIngredientListTransformer()
					.transform(listID.run());
				
				if (list == null ) {
					throw new UserException("You have not created any shopping lists yet.", true);
				}
								
				return new InlineAPI("me/lists/" + Integer.toString(list.getId())).run();
			}
		}, new IngredientListTransformer());
	}
	
	public static API<IngredientList> getList(int id) {
		return new API<IngredientList>("me/lists/" + Integer.toString(id),
			new IngredientListTransformer());
	}
	
	public static API<IngredientList> getExtras() {
		return new API<IngredientList>("me/extra_items", new ExtraItemsTransformer());
	}
	
	public static API<IngredientList> addExtraItem(final String item) {
		return new API<IngredientList>(new AbstractInlineAPI() {
			@Override
			public JSONArray run() throws JSONException, IOException {
				InlineAPI addApi = new InlineAPI("me/extra_items/add");
				addApi.setParameter("item", item);
				addApi.run();
				
				InlineAPI getApi = new InlineAPI("me/extra_items");
				return getApi.run();
			}
		}, new ExtraItemsTransformer());
	}
	
	public API<String> hideIngredients(ArrayList<Ingredient> ingredients) {
		// Build our JSONArray.
		JSONArray array = new JSONArray();
		
		for (Ingredient i : ingredients) {
			array.put(i.getFood());
		}
		
		// Get rid of them on our end.
		removeIngredients(ingredients);
		
		API<String> api = new API<String>(
			String.format("me/lists/%d/hide_foods", this.id),
			new StringTransformer());
		
		api.setParameter("foods", array.toString());
		return api;
	}
	
	private void removeIngredients(ArrayList<Ingredient> ingredients) {
		for (IngredientGroup group : ingredientGroups) {
			for (Ingredient i : ingredients) {
				group.getIngredients().remove(i);
			}
		}
		
		ArrayList<IngredientGroup> igs = new ArrayList<IngredientGroup>();
		
		for (IngredientGroup ig : ingredientGroups) {
			if (ig.getIngredients().size() > 0) {
				igs.add(ig);
			}
		}
		
		ingredientGroups = igs;
	}
	
	public API<String> hideExtraItems(ArrayList<Ingredient> ingredients) {
		ArrayList<Integer> ids = new ArrayList<Integer>(ingredients.size());
		
		for (Ingredient i : ingredients) {
			ids.add(i.getId());
		}
		
		String idString = Utils.join(ids, ",");
		
		removeIngredients(ingredients);
		
		return new API<String>(
			String.format("me/extra_items/%s/delete", idString),
			new StringTransformer());
	}
	
	public ArrayList<IngredientGroup> getIngredientGroups() { return ingredientGroups; }
	public int getId() { return id; }
	public DateTime getCreatedAt() { return createdAt; }

	private static class IngredientListTransformer implements DataTransformer<IngredientList> {
		public IngredientList transform(JSONArray array) throws JSONException {
			return new IngredientList(array.getJSONObject(1));
		}
	}
	
	private static class ExtraItemsTransformer implements DataTransformer<IngredientList> {
		public IngredientList transform(JSONArray array) throws JSONException {
			return new IngredientList(array.getJSONArray(1));
		}
	}
	
	private static class FirstIngredientListTransformer implements DataTransformer<IngredientList> {
		public IngredientList transform(JSONArray array) throws JSONException {
			if (array.getJSONArray(1).length() > 0)
				return new IngredientList(array.getJSONArray(1).getJSONObject(0));
			else
				return null;
		}
	}
}
