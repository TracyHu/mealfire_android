package com.mealfire.activity;

import java.util.ArrayList;

import org.json.JSONException;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.mealfire.R;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.Ingredient;
import com.mealfire.model.IngredientList;

public class ShoppingList extends MealfireActivity {
	private IngredientList ingredientList;
	private IngredientListAdapter adapter;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.list);
		setTitle("Shopping List");
		adapter = new IngredientListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.list_view);
		listView.setAdapter(adapter);
		listView.setDivider(null);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				IngredientRow row = adapter.getRows().get(position);
				row.checked = !row.checked;
				adapter.notifyDataSetChanged();
			}
		});
		
		DataRunnable<IngredientList> listHandler = new DataRunnable<IngredientList>() {
			public void run(IngredientList list) throws JSONException {
				ingredientList = list;
				adapter.loadData(list);
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
		
		for (IngredientRow row : adapter.getRows()) {
			if (row.getType() == IngredientRow.TYPE_INGREDIENT && row.checked) {
				checked.add(row.ingredient);
			}
		}
		
		API<String> api = ingredientList.hideIngredients(checked);
		api.setActivity(this);
		
		api.setSuccessHandler(new DataRunnable<String>() {
			public void run(String data) throws JSONException {
				adapter.loadData(ingredientList);
			}
		});
		
		api.run();
	}
}
