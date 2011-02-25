package com.mealfire.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.mealfire.R;
import com.mealfire.model.Ingredient;
import com.mealfire.model.IngredientGroup;
import com.mealfire.model.IngredientList;

public class IngredientListAdapter extends BaseAdapter {
	private ArrayList<IngredientRow> rows = new ArrayList<IngredientRow>();
	private Activity activity;
	
	public IngredientListAdapter(Activity activity) {
		this.activity = activity;
	}
	
	public void loadData(IngredientList ingredientList) {
		rows.clear();
		
		for (int i = 0; i < ingredientList.getIngredientGroups().size(); i++) {
			IngredientGroup ig = ingredientList.getIngredientGroups().get(i);
			
			rows.add(new IngredientRow(ig));
			
			for (Ingredient ingredient : ig.getIngredients()) {
				rows.add(new IngredientRow(ingredient));
			}
		}
		
		activity.runOnUiThread(new Runnable() {
			public void run() {
				notifyDataSetChanged();
			}
		});
	}
	
	public ArrayList<IngredientRow> getRows() {
		return rows;
	}
	
	public int getCount() {
		return rows.size();
	}

	public Object getItem(int position) {
		IngredientRow row = rows.get(position);
		
		if (row.getType() == IngredientRow.TYPE_GROUP) {
			return row.hashCode();
		} else {
			if (row.ingredient.getId() > 0) {
				return row.ingredient.getId();
			} else {
				return row.hashCode();
			}
		}
	}

	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		IngredientRow row = rows.get(position);
		
		if (row.getType() == IngredientRow.TYPE_GROUP) {
			if (convertView == null || convertView.findViewById(R.id.header_text) == null) {
				convertView = activity.getLayoutInflater().inflate(R.layout.row_header, null);
			}
			
			TextView textView = (TextView) convertView.findViewById(R.id.header_text);
			textView.setText(row.toString());
		} else {
			if (convertView == null || convertView.findViewById(R.id.text) == null) {
				convertView = activity.getLayoutInflater().inflate(R.layout.row_with_checkmark, null);
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
