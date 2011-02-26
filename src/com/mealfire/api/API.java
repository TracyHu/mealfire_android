package com.mealfire.api;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import com.mealfire.UserException;
import com.mealfire.activity.MealfireActivity;

public class API<T> {
	private AbstractInlineAPI inlineAPI;
	private MealfireActivity activity;
	private DataRunnable<String> errorHandler;
	private DataRunnable<T> successHandler;
	private DataTransformer<T> transformer;
	
	public API(String action, DataTransformer<T> transformer) {
		inlineAPI = new InlineAPI(action);
		this.transformer = transformer;
	}
	
	public API(AbstractInlineAPI api, DataTransformer<T> transformer) {
		inlineAPI = api;
		this.transformer = transformer;
	}
	
	public API<T> setParameter(String key, String value) {
		inlineAPI.setParameter(key, value);
		return this;
	}
	
	public API<T> setParameter(String key, int value) {
		inlineAPI.setParameter(key, value);
		return this;
	}
	
	public API<T> setErrorHandler(DataRunnable<String> runnable) {
		errorHandler = runnable;
		return this;
	}
	
	public API<T> setSuccessHandler(DataRunnable<T> runnable) {
		successHandler = runnable;
		return this;
	}
	
	public API<T> setActivity(MealfireActivity activity) {
		this.activity = activity;
		return this;
	}
	
	public void dontSetToken() {
		inlineAPI.dontSetToken();
	}
	
	public void run() {
		if (activity != null) {
			activity.setProgressBarIndeterminateVisibility(true);
		    activity.setProgressBarVisibility(true);
		}
		
		(new Thread() {
			public void run() {
				checkedThreadedGet();
			}
		}).start();
	}
	
	private void checkedThreadedGet() {
		try {
			threadedGet();
		} catch (final UserException e) {
			cancelProgresBar();
			
			if (activity != null) {
				activity.runOnUiThread(new Runnable() {
					public void run() {
						activity.handleAPIUserException(API.this, e);
					}
				});
			}
		} catch (JSONException e) {
			handleAPIException(e);
		} catch (IOException e) {
			handleAPIException(e);
		}
	}
	
	private void handleAPIException(final Exception e) {
		cancelProgresBar();
		
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					activity.handleAPIException(API.this, e);
				}
			});
		}
	}
	
	private void threadedGet() throws JSONException, UserException, IOException {
		JSONArray array = inlineAPI.run();
		
		if (array != null) {
			if (array.getBoolean(0)) {
				if (successHandler != null)
					successHandler.run(transformer.transform(array));
			} else if (errorHandler != null) {
				errorHandler.run(array.getString(1));
			}
		}
		
		cancelProgresBar();
	}
	
	private void cancelProgresBar() {
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					activity.setProgressBarIndeterminateVisibility(false);
				    activity.setProgressBarVisibility(false);
				}
			});
		}
	}
}
