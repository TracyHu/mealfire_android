package com.mealfire.activity;

import java.util.ArrayList;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mealfire.R;
import com.mealfire.Utils;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.User;

public class Home extends MealfireActivity {
	private ArrayList<HomeRow> rows = new ArrayList<HomeRow>();
	private BaseAdapter adapter;
	private int shoppingListId = 0;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.home);
		setTitle("Mealfire");
		validateLogin();
		
		ListView listView = (ListView) findViewById(R.id.home_list_view);
		adapter = new HomeAdapter();
		listView.setAdapter(adapter);
		
		HomeRow recipesRow = new HomeRow();
		
		recipesRow.title = "Recipes";
		recipesRow.image = R.drawable.recipes;
		recipesRow.onClick = new Runnable() {
			public void run() {
				Intent intent = new Intent(Home.this, BrowseRecipes.class);
				Home.this.startActivity(intent);
			}
		};
		
		rows.add(recipesRow);
		
		HomeRow shoppingListRow = new HomeRow();
		
		shoppingListRow.title = "Shopping List";
		shoppingListRow.image = R.drawable.lists;
		shoppingListRow.onClick = new Runnable() {
			public void run() {
				Intent intent = new Intent(Home.this, ShoppingList.class);
				
				if (shoppingListId > 0) {
					intent.putExtra("listID", shoppingListId);
				}
				
				Home.this.startActivity(intent);
			}
		};
		
		rows.add(shoppingListRow);
		
		HomeRow extrasRow = new HomeRow();
		
		extrasRow.title = "Extra Items";
		extrasRow.image = R.drawable.extras;
		extrasRow.onClick = new Runnable() {
			public void run() {
				Intent intent = new Intent(Home.this, ExtraItems.class);
				Home.this.startActivity(intent);
			}
		};
		
		rows.add(extrasRow);
		
		HomeRow calendarRow = new HomeRow();
		
		calendarRow.title = "Calendar";
		calendarRow.image = R.drawable.calendar;
		calendarRow.onClick = new Runnable() {
			public void run() {
				Intent intent = new Intent(Home.this, Calendar.class);
				Home.this.startActivity(intent);
			}
		};
		
		rows.add(calendarRow);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				rows.get(position).onClick.run();
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		requireLogin();
		
		// Show some stats.
		API<User.Stats> api = User.getUserStats();
		api.setActivity(this);
		
		api.setSuccessHandler(new DataRunnable<User.Stats>() {
			public void run(User.Stats stats) throws JSONException {
				if (stats.getRecipeCount() == 0) {
					rows.get(0).info = "You have no recipes";
				} else if (stats.getRecipeCount() == 1) {
					rows.get(0).info = "You have 1 recipe";
				} else {
					rows.get(0).info = String.format("You have %d recipes", stats.getRecipeCount());
				}
				
				if (stats.getLatestList() == null) {
					rows.get(1).info = "No shopping lists... yet.";
				} else {
					shoppingListId = stats.getLatestList().getId();
					rows.get(1).info = String.format("From %s", Utils.prettyDateTime(stats.getLatestList().getCreatedAt()));
				}
				
				if (stats.getExtraItemsCount() == 0) {
					rows.get(2).info = "You have no extra items";
				} else if (stats.getExtraItemsCount() == 1) {
					rows.get(2).info = "You have 1 extra item";
				} else {
					rows.get(2).info = String.format("You have %d extra items", stats.getExtraItemsCount());
				}
				
				if (stats.getCalendarCount() == 0) {
					rows.get(3).info = "You have no recipes scheduled";
				} else if (stats.getCalendarCount() == 1) {
					rows.get(3).info = "You have 1 recipe scheduled";
				} else {
					rows.get(3).info = String.format("You have %d recipes scheduled", stats.getCalendarCount());
				}
				
				runOnUiThread(new Runnable() {
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
		});
		
		if (User.isLoggedIn()) {
			api.run();
		}
	}
	
	private class HomeAdapter extends BaseAdapter {
		public int getCount() {
			return rows.size();
		}
		
		public long getItemId(int position) {
			return rows.get(position).image;
		}
		
		public boolean hasStableIds() {
			return true;
		}
		
		public boolean isEmpty() {
			return rows.size() == 0;
		}
		
		public Object getItem(int position) {
			return rows.get(position);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.home_row, null);
			}
			
			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			TextView activityName = (TextView) convertView.findViewById(R.id.activity_name);
			TextView activityInfo = (TextView) convertView.findViewById(R.id.activity_info);
			
			HomeRow row = rows.get(position);
			
			icon.setImageResource(row.image);
			activityName.setText(row.title);
			activityInfo.setText(row.info);
			
			return convertView;
		}
	}
	
	private static class HomeRow {
		public String title;
		public int image;
		public String info = "Checking...";
		public Runnable onClick;
	}
}
