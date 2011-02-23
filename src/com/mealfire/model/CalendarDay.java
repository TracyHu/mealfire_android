package com.mealfire.model;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mealfire.api.API;
import com.mealfire.api.DataTransformer;

public class CalendarDay {
	private Recipe recipe;
	private DateTime day;
	
	public CalendarDay(JSONObject obj) throws JSONException {
		recipe = new Recipe(obj.getJSONObject("recipe"));
		String[] dayParts = obj.getString("day").split("-");
		day = new DateTime(
			Integer.parseInt(dayParts[0]),
			Integer.parseInt(dayParts[1]),
			Integer.parseInt(dayParts[2]),
			0, 0, 0, 0);
	}
	
	public static API<ArrayList<CalendarDay>> getCalendar() {
		return new API<ArrayList<CalendarDay>>("me/calendar",
			new CalendarTransformer());
	}

	public Recipe getRecipe() { return recipe; }
	public DateTime getDay() { return day; }
	
	private static class CalendarTransformer implements DataTransformer<ArrayList<CalendarDay>> {
		public ArrayList<CalendarDay> transform(JSONArray array) throws JSONException {
			ArrayList<CalendarDay> days = new ArrayList<CalendarDay>();
			JSONArray objects = array.getJSONArray(1);
			
			for (int i = 0; i < objects.length(); i++) {
				days.add(new CalendarDay(objects.getJSONObject(i)));
			}
			
			return days;
		}
		
	}
}
