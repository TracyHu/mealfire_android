package com.mealfire.activity;

import java.util.ArrayList;

import org.json.JSONException;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mealfire.R;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.Ingredient;
import com.mealfire.model.IngredientGroup;
import com.mealfire.model.IngredientList;

public class ShoppingList extends MealfireActivity {
	private ArrayList<IngredientRow> rows = new ArrayList<IngredientRow>();
	private IngredientList ingredientList;
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
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				IngredientRow row = rows.get(position);
				row.checked = !row.checked;
				adapter.notifyDataSetChanged();
			}
		});
		
		DataRunnable<IngredientList> listHandler = new DataRunnable<IngredientList>() {
			public void run(IngredientList list) throws JSONException {
				ingredientList = list;
				reloadData();
			}
		};
		
		API<IngredientList> api;
		
		if (getIntent().hasExtra("listID")) {
			// We have an id, so no need to make two requests.
			api = IngredientList.getList(getIntent().getExtras().getInt("listID"));
		} else {
			api = IngredientList.getLatestList();
		}
		
		api.setActivity(this);
		api.setSuccessHandler(listHandler);
		api.run();
	}
	
	private void reloadData() {
		rows.clear();
		
		for (int i = 0; i < ingredientList.getIngredientGroups().size(); i++) {
			IngredientGroup ig = ingredientList.getIngredientGroups().get(i);
			
			rows.add(new IngredientRow(ig));
			
			for (Ingredient ingredient : ig.getIngredients()) {
				rows.add(new IngredientRow(ingredient));
			}
		}
		
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.shopping_list, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
	    switch (item.getItemId()) {
	    case R.id.hide_checked:
	        hideChecked();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void hideChecked() {
		// We can't hide anything until it's loaded.
		if (ingredientList == null) {
			return;
		}
		
		// Grab all our checked ingredients.
		ArrayList<Ingredient> checked = new ArrayList<Ingredient>();
		
		for (IngredientRow row : rows) {
			if (row.getType() == IngredientRow.TYPE_INGREDIENT && row.checked) {
				checked.add(row.ingredient);
			}
		}
		
		API<String> api = ingredientList.hideIngredients(checked);
		api.setActivity(this);
		
		api.setSuccessHandler(new DataRunnable<String>() {
			public void run(String data) throws JSONException {
				reloadData();
			}
		});
		
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
			
			if (row.getType() == IngredientRow.TYPE_GROUP) {
				if (convertView == null || convertView.findViewById(R.id.header_text) == null) {
					convertView = getLayoutInflater().inflate(R.layout.row_header, null);
				}
				
				TextView textView = (TextView) convertView.findViewById(R.id.header_text);
				textView.setText(row.toString());
			} else {
				if (convertView == null || convertView.findViewById(R.id.text) == null) {
					convertView = getLayoutInflater().inflate(R.layout.row_with_checkmark, null);
				}
				
				CheckedTextView textView = (CheckedTextView) convertView.findViewById(R.id.text);
				textView.setText(row.toString());
				textView.setChecked(row.checked);
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
			return rows.get(position).getType();
		}
	}
}
