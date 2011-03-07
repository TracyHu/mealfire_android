package com.mealfire.activity;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.mealfire.ImageLoader;
import com.mealfire.R;
import com.mealfire.Utils;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.CalendarDay;
import com.mealfire.model.IngredientList;
import com.mealfire.model.Recipe;
import com.mealfire.model.Store;

public class Calendar extends MealfireActivity {
	private ArrayList<CalendarRow> rows = new ArrayList<CalendarRow>();
	private CalendarAdapter adapter;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.list);
		setTitle("Calendar");
		ListView listView = (ListView) findViewById(R.id.list_view);
		adapter = new CalendarAdapter();
		listView.setAdapter(adapter);
		listView.setDivider(null);
		registerForContextMenu(listView);
		loadData();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Recipe recipe = rows.get(position).day.getRecipe();
				
				Intent intent = new Intent(Calendar.this, ViewRecipe.class);
				intent.putExtra("recipeID", recipe.getID());
				Calendar.this.startActivity(intent);
			}
		});
	}
	
	public void loadData() {
		API<ArrayList<CalendarDay>> api = CalendarDay.getCalendar();
		api.setActivity(this);
		api.setParameter("include", "recipe[image_thumb]");
		
		api.setSuccessHandler(new DataRunnable<ArrayList<CalendarDay>>() {
			public void run(ArrayList<CalendarDay> days) throws JSONException {
				DateTime lastDay = null;
				rows.clear();
				
				for (CalendarDay calDay : days) {
					if (lastDay == null || !lastDay.equals(calDay.getDay())) {
						rows.add(new CalendarRow(calDay.getDay()));
					}
					rows.add(new CalendarRow(calDay));
					lastDay = calDay.getDay();
				}
				
				runOnUiThread(new Runnable() {
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
		});
		
		api.run();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.calendar, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
	    switch (item.getItemId()) {
	    case R.id.create_list:
	    	createList();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.calendar_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.remove_recipe:
			CalendarRow row = rows.get(info.position);
			deleteDay(row.day);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	private void deleteDay(CalendarDay day) {
		API<String> api = day.delete();
		api.setActivity(this);
		
		api.setSuccessHandler(new DataRunnable<String>() {
			public void run(String data) throws JSONException {
				loadData();
			}
		});
		
		api.run();
	}
	
	public void createList() {
		API<ArrayList<Store>> api = Store.all();
		api.setActivity(this);
		
		api.setSuccessHandler(new DataRunnable<ArrayList<Store>>() {
			public void run(final ArrayList<Store> stores) throws JSONException {
				runOnUiThread(new Runnable() {
					public void run() {
						if (stores.size() == 0) {
							createList(null);
						} else if (stores.size() == 1) {
							createList(stores.get(0));
						} else {
							chooseStore(stores);
						}
					}
				});
			}
		});
		
		api.run();
	}
	
	public void chooseStore(final ArrayList<Store> stores) {
		String[] storeNames = new String[stores.size()];
		
		for (int i = 0; i < stores.size(); i++) {
			storeNames[i] = stores.get(i).getName();
		}
			
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose a store");
		
		builder.setItems(storeNames, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int position) {
		    	createList(stores.get(position));
		    }
		});
		
		builder.create().show();
	}
	
	public void createList(Store store) {
		// Grab all the data we need.
		final ArrayList<String> dayNames = new ArrayList<String>();
		final ArrayList<DateTime> days = new ArrayList<DateTime>();
		
		for (CalendarRow row : rows) { 
			if (row.isHeader()) {
				dayNames.add(row.toString());
				days.add(row.headerDay);
			}
		}
		
		// And a list of checked values.
		final boolean[] checked = new boolean[dayNames.size()];
		
		for (int i = 0; i < checked.length; i++) {
			checked[i] = false;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose the days");
		
		builder.setMultiChoiceItems(
			(String[]) dayNames.toArray(new String[dayNames.size()]),
			checked,
			new OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checked[which] = isChecked;
				}
			});
		
		builder.setCancelable(true);
		
	    builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	   ArrayList<DateTime> checkedDays = new ArrayList<DateTime>();
        	   
        	   for (int i = 0; i < checked.length; i++) {
        		   if (checked[i]) {
        			   checkedDays.add(days.get(i));
        		   }
        	   }
        	   
        	   API<IngredientList> api = IngredientList.createList(checkedDays);
        	   api.setActivity(Calendar.this);
        	   
        	   api.setSuccessHandler(new DataRunnable<IngredientList>() {
        		   public void run(IngredientList list) throws JSONException {
        			   	Intent intent = new Intent(Calendar.this, ShoppingList.class);
       					intent.putExtra("listID", list.getId());
       					Calendar.this.startActivity(intent);
        		   }
        	   });
        	   
        	   api.run();
           }
       });
		
		builder.create().show();
	}
	
	private static class CalendarRow {
		public CalendarDay day;
		public DateTime headerDay;
		
		public CalendarRow(DateTime date) {
			headerDay = date;
		}
		
		public CalendarRow(CalendarDay day) {
			this.day = day;
		}
		
		public boolean isHeader() {
			return headerDay != null;
		}
		
		public String toString() {
			if (isHeader()) {
				return Utils.prettyDate(headerDay);
			} else {
				return day.getRecipe().getName();
			}
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
	}
	
	private class CalendarAdapter extends BaseAdapter {
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
			CalendarRow row = rows.get(position);
			
			if (row.isHeader()) {
				if (convertView == null || convertView.findViewById(R.id.header_text) == null) {
					convertView = getLayoutInflater().inflate(R.layout.row_header, null);
				}
				
				TextView textView = (TextView) convertView.findViewById(R.id.header_text);
				textView.setText(row.toString());
			} else {
				if (convertView == null || convertView.findViewById(R.id.text) == null) {
					convertView = getLayoutInflater().inflate(R.layout.recipe_row, null);
				}
				
				TextView textView = (TextView) convertView.findViewById(R.id.recipe_name);
				ImageView image = (ImageView) convertView.findViewById(R.id.icon);
				View separator = convertView.findViewById(R.id.separator);
				Recipe recipe = row.day.getRecipe();
				
				textView.setText(row.toString());
				separator.setVisibility(View.VISIBLE);
				
				if (recipe.getImageURL() != null) {
					image.setImageResource(R.drawable.placeholder);
					ImageLoader.loadImage(Calendar.this, image, recipe.getImageURL());
				} else {
					image.setImageResource(R.drawable.no_image); 
				}
			}
			
			convertView.setTag(row);
			return convertView;
		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isEmpty() {
			return rows.size() == 0;
		}
		
		public int getItemViewType(int position) {
			return rows.get(position).isHeader() ? 0 : 1;
		}
	}
}
