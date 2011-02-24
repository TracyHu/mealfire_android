package com.mealfire.activity;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.json.JSONException;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mealfire.R;
import com.mealfire.Utils;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.CalendarDay;

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
		
		API<ArrayList<CalendarDay>> api = CalendarDay.getCalendar();
		api.setActivity(this);
		
		api.setSuccessHandler(new DataRunnable<ArrayList<CalendarDay>>() {
			public void run(ArrayList<CalendarDay> days) throws JSONException {
				DateTime lastDay = null;
				
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
	
	private static class CalendarRow {
		public CalendarDay day;
		public String headerTitle;
		
		public CalendarRow(DateTime date) {
			headerTitle = Utils.prettyDate(date);
		}
		
		public CalendarRow(CalendarDay day) {
			this.day = day;
		}
		
		public boolean isHeader() {
			return headerTitle != null;
		}
		
		public String toString() {
			if (isHeader()) {
				return headerTitle;
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
					convertView = getLayoutInflater().inflate(R.layout.row, null);
				}
				
				TextView textView = (TextView) convertView.findViewById(R.id.text);
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
			return rows.get(position).isHeader() ? 0 : 1;
		}
	}
}
