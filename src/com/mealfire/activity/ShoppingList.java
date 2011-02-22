package com.mealfire.activity;

import java.util.ArrayList;

import org.json.JSONException;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mealfire.R;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.Ingredient;
import com.mealfire.model.IngredientGroup;
import com.mealfire.model.IngredientList;

public class ShoppingList extends MealfireActivity {
	private ArrayList<IngredientRow> rows = new ArrayList<IngredientRow>();
	private BaseAdapter adapter;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.list);
		setTitle("Shopping List");
		adapter = new ShoppingListAdapter();
		ListView listView = (ListView) findViewById(R.id.list_list_view);
		listView.setAdapter(adapter);
		listView.setDivider(null);
		
		DataRunnable<IngredientList> listHandler = new DataRunnable<IngredientList>() {
			public void run(IngredientList list) throws JSONException {
				for (int i = 0; i < list.getIngredientGroups().size(); i++) {
					IngredientGroup ig = list.getIngredientGroups().get(i);
					
					rows.add(new IngredientRow(ig.getName(), "GROUP"));
					
					for (Ingredient ingredient : ig.getIngredients()) {
						rows.add(new IngredientRow(ingredient.toString(), "INGREDIENT"));
					}
				}
				
				runOnUiThread(new Runnable() {
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
		};
		
		API<IngredientList> api;
		
		if (getIntent().hasExtra("listID")) {
			// We were given an id, so no need to make two requests.
			api = IngredientList.getList(getIntent().getExtras().getInt("listID"));
		} else {
			api = IngredientList.getLatestList();
		}
		
		api.setActivity(this);
		api.setSuccessHandler(listHandler);
		api.run();
	}
	
	private class ShoppingListAdapter extends BaseAdapter {
		public int getCount() {
			return rows.size();
		}

		public Object getItem(int position) {
			return rows.get(position);
		}

		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			IngredientRow row = rows.get(position);
			
			if (row.type == "GROUP") {
				if (convertView == null || convertView.findViewById(R.id.ingredient_group_text_view) == null) {
					convertView = getLayoutInflater().inflate(R.layout.ingredient_group, null);
				}
				
				TextView textView = (TextView) convertView.findViewById(R.id.ingredient_group_text_view);
				textView.setText(row.toString());
			} else {
				if (convertView == null || convertView.findViewById(R.id.ingredient_text_view) == null) {
					convertView = getLayoutInflater().inflate(R.layout.ingredient, null);
				}
				
				TextView textView = (TextView) convertView.findViewById(R.id.ingredient_text_view);
				textView.setText(row.toString());
			}
			
			return convertView;
		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isEmpty() {
			return rows.size() == 0;
		}
		
		public int getItemViewType(int position) {
			return rows.get(position).type.hashCode();
		}
	}
}
