package com.mealfire.activity;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

import com.mealfire.UserException;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.User;

public class MealfireActivity extends Activity {
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    requestWindowFeature(Window.FEATURE_PROGRESS);
	}
	
	public void requireLogin() {
		if (User.getToken() == null) {
			SharedPreferences pref = getSharedPreferences("com.mealfire", MODE_PRIVATE);
			
			if (pref.getString("token", null) == null) {
				Intent intent = new Intent(this, Login.class);
				startActivity(intent);
			} else {
				User.setToken(pref.getString("token", null));
			}
		}
	}
	
	public void validateLogin() {		
		if (User.getToken() == null)
			return;
		
		API<String> api = User.validateToken();
		
		api.setErrorHandler(new DataRunnable<String>() {
			public void run(String data) throws JSONException {
				Intent intent = new Intent(MealfireActivity.this, Login.class);
				startActivity(intent);
			}
		});
		
		api.run();
	}

	public void handleAPIException(final API<?> api, final Exception e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage("There was a network error. Would you like to try " +
				"the request again?")
			.setCancelable(false)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int id) {
		    		api.run();
		    		dialog.cancel();
		        }
		     })
		     .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    	 public void onClick(DialogInterface dialog, int id) {
		        	dialog.cancel();
		    	 }
		     });
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void handleAPIUserException(final API<?> api, final UserException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage(e.getMessage())
			.setCancelable(false)
		    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int id) {
		    		dialog.cancel();
		    		
		    		if (e.shouldCancelActivity()) {
		    			MealfireActivity.this.finish();
		    		}
		        }
		     });
		
		AlertDialog alert = builder.create();
		alert.show();
	}
}
