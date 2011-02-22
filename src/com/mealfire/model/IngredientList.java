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
import com.mealfire.api.API;
import com.mealfire.api.AbstractInlineAPI;
import com.mealfire.api.DataTransformer;
import com.mealfire.api.InlineAPI;

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
	
	public ArrayList<IngredientGroup> getIngredientGroups() { return ingredientGroups; }
	public int getId() { return id; }
	public DateTime getCreatedAt() { return createdAt; }

	private static class IngredientListTransformer implements DataTransformer<IngredientList> {
		public IngredientList transform(JSONArray array) throws JSONException {
			return new IngredientList(array.getJSONObject(1));
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
