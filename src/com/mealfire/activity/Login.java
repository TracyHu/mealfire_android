package com.mealfire.activity;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.mealfire.R;
import com.mealfire.api.API;
import com.mealfire.api.DataRunnable;
import com.mealfire.model.User;

public class Login extends MealfireActivity {
	private EditText email;
	private EditText password;
	private String errorMessage;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.login);
		setTitle("Login");
		
		email = (EditText) findViewById(R.id.email_input);
		password = (EditText) findViewById(R.id.passowrd_input);
		
	}
	
	public void submit(View view) {		
		API<String> api = User.authorize(
			email.getText().toString(),
			password.getText().toString());
		
		api.setActivity(this);
		
		api.setErrorHandler(new DataRunnable<String>() {
			public void run(String error) throws JSONException {
				errorMessage = error;
				
				runOnUiThread(new Runnable() {
					public void run() {
						showDialog(errorMessage.hashCode());
					}
				});
			}
		});
		
		api.setSuccessHandler(new DataRunnable<String>() {
			public void run(String token) throws JSONException {
				// Save our token.
				SharedPreferences.Editor editor = getSharedPreferences("com.mealfire", MODE_PRIVATE).edit();
				editor.putString("token", token);
				editor.commit();
				
				// And in memory.
				User.setToken(token);
				
				runOnUiThread(new Runnable() {
					public void run() {
						Login.this.finish();
					}
				});
			}
		});
		
		api.run();
	}
	
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage(errorMessage)
			.setCancelable(false)
		    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int id) {
		    		dialog.cancel();
		        }
		    });
		
		return builder.create();
	}
}
