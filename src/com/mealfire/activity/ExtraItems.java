package com.mealfire.activity;

import java.util.ArrayList;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.mealfire.R;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.Ingredient;
import com.mealfire.model.IngredientList;

public class ExtraItems extends MealfireActivity {
	private IngredientListAdapter adapter;
	private IngredientList ingredientList;
	private EditText addInput;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.list);
		setTitle("Extra Items");
		adapter = new IngredientListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.list_view);
		View addRow = getLayoutInflater().inflate(R.layout.add_extra_item_row, null);
		addInput = (EditText) addRow.findViewById(R.id.add_input);
		Button addButton = (Button) addRow.findViewById(R.id.add_button);
		listView.addHeaderView(addRow);
		listView.setAdapter(adapter);
		listView.setDivider(null);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				IngredientRow row = adapter.getRows().get(position - 1);
				row.checked = !row.checked;
				adapter.notifyDataSetChanged();
			}
		});
		
		addButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addItem();
			}
		});
		
		addInput.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					addItem();
					return true;
				}
				return false;
			}
		});
				
		API<IngredientList> api = IngredientList.getExtras();
		api.setActivity(this);
		
		api.setSuccessHandler(new DataRunnable<IngredientList>() {
			public void run(IngredientList list) throws JSONException {
				ingredientList = list;
				adapter.loadData(list);
			}
		});
		
		api.run();
	}
	
	private void addItem() {
		String item = addInput.getText().toString().trim();
		
		if (item.length() == 0) {
			return;
		}
		
		API<IngredientList> api = IngredientList.addExtraItem(item);
		
		api.setSuccessHandler(new DataRunnable<IngredientList>() {
			public void run(IngredientList list) throws JSONException {
				ingredientList = list;
				adapter.loadData(list);
			}
		});
		
		api.run();
		
		addInput.getText().clear();
		
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))  
        	.hideSoftInputFromWindow(addInput.getWindowToken(), 0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.extra_items, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
	    switch (item.getItemId()) {
	    case R.id.remove_checked:
	        removeChecked();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void removeChecked() {
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
		
		API<String> api = ingredientList.hideExtraItems(checked);
		api.setActivity(this);
		
		api.setSuccessHandler(new DataRunnable<String>() {
			public void run(String data) throws JSONException {
				adapter.loadData(ingredientList);
			}
		});
		
		api.run();
	}
}
