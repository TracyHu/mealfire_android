package  com.mealfire.activity;

import java.util.ArrayList;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mealfire.ImageLoader;
import com.mealfire.PagedList;
import com.mealfire.R;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.Recipe;

public class BrowseRecipes extends MealfireActivity {
	private ListView recipesView;
	private ArrayList<Recipe> recipes = new ArrayList<Recipe>();
	private int total = 0;
	private BaseAdapter adapter;
	private String searchQuery;

	@Override
	public void onCreate(Bundle icicle) {
		setTitle("Recipes");
		super.onCreate(icicle);
		setContentView(R.layout.recipes);
		recipesView = (ListView) findViewById(R.id.RecipesListView);
		
		View searchRow = getLayoutInflater().inflate(R.layout.search_row, null);
		final EditText searchBox = (EditText) searchRow.findViewById(R.id.search_input);
		final Button clearSearchButton = (Button) searchRow.findViewById(R.id.clear_search_button);
		final ImageButton searchButton = (ImageButton) searchRow.findViewById(R.id.search_button);
		recipesView.addHeaderView(searchRow);
		adapter = new RecipesAdapter();
		recipesView.setAdapter(adapter);
		
		fetchMore();
		
		searchBox.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					searchQuery = searchBox.getText().toString();
					clearList();
					fetchMore();
					return true;
				}
				return false;
			}
		});
		
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchQuery = searchBox.getText().toString();
				clearList();
				fetchMore();
			}
		});
		
		clearSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchQuery = "";
				searchBox.getText().clear();
				clearList();
				fetchMore(); 
			}
		});
		
		recipesView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// The "Load More" item.
				if (position == recipes.size() + 1) {
					TextView loadMore = (TextView) view.findViewById(R.id.load_more_text_view);
					loadMore.setText("Loading...");
					fetchMore();
					return;
				}
				
				// A recipe.
				Recipe recipe = recipes.get(position - 1);
				
				Intent intent = new Intent(BrowseRecipes.this, ViewRecipe.class);
				intent.putExtra("recipeID", recipe.getID());
				BrowseRecipes.this.startActivity(intent);
			}
		});
	}
	
	private void clearList() {
		recipes.clear();
		total = 0;
		adapter.notifyDataSetChanged();
	}
	
	private void fetchMore() {
		API<PagedList<Recipe>> api;
		
		if (searchQuery != null && !searchQuery.trim().equals("")) {
			api = Recipe.searchRecipes(searchQuery);
		} else {
			api = Recipe.getRecipes();
			api.setParameter("sort", "name");
		}

		api.setActivity(this);
		api.setParameter("limit", 25);
		api.setParameter("offset", recipes.size());
		api.setParameter("include", "image_thumb");
		
		api.setSuccessHandler(new DataRunnable<PagedList<Recipe>>() {
			public void run(PagedList<Recipe> recipes) throws JSONException {
				BrowseRecipes.this.recipes.addAll(recipes);
				total = recipes.getTotal();
				
				runOnUiThread(new Runnable() {
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
		});
		
		api.run();
	}
	
	private boolean hasMore() {
		return total > recipes.size();
	}
	
	private class RecipesAdapter extends BaseAdapter {
		public ImageLoader imageLoader;
		
		public RecipesAdapter() {
	        imageLoader = new ImageLoader(BrowseRecipes.this.getApplicationContext());
	    }
		
		public int getCount() {
			if (hasMore())
				return recipes.size() + 1;
			else
				return recipes.size();
		}

		public Object getItem(int position) {
			if (position == recipes.size())
				return null;
			else
				return recipes.get(position);
		}

		public long getItemId(int position) {
			if (position == recipes.size())
				return -1;
			else
				return recipes.get(position).getID();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == recipes.size()) {
				// This is the "Load More" row.
				return getLayoutInflater().inflate(R.layout.recipe_load_more, null);
			}
			
			if (convertView == null || convertView.findViewById(R.id.icon) == null) {
				convertView = getLayoutInflater().inflate(R.layout.recipe_row, null);
			}
			
			TextView name = (TextView) convertView.findViewById(R.id.recipe_name);
			ImageView image = (ImageView) convertView.findViewById(R.id.icon);
			Recipe recipe = recipes.get(position);
			
			name.setText(recipe.getName());
			image.setTag(recipe.getImageURL());
			
			if (recipe.getImageURL() != null) {
				imageLoader.DisplayImage(recipe.getImageURL(), BrowseRecipes.this, image);
			} else {
				image.setImageResource(R.drawable.no_image); 
			}
			
			return convertView;
		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isEmpty() {
			return recipes.size() == 0;
		}
	}
}