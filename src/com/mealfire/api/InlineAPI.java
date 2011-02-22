package com.mealfire.api;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;

import android.net.http.AndroidHttpClient;

import com.mealfire.model.User;

public class InlineAPI extends AbstractInlineAPI {
	private String action;
	
	public InlineAPI(String action) {
		this.action = action;
	}
	
	public void setParameters(Hashtable<String, String> data) {
		this.data = data;
	}
	
	public JSONArray run() throws JSONException, IOException {
		if (shouldSetToken) {
			setParameter("token", User.getToken());
		}
		
		HttpResponse response = null;
		String json = null;
		
		AndroidHttpClient client = AndroidHttpClient.newInstance("Mealfire for Android");
		
		// Create the query string.
		StringBuilder query = new StringBuilder();
		int i = 0;
		
		for(String key : data.keySet()) {
			if (i > 0)
				query.append("&");
			
			try {
				query.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(data.get(key), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			
			i++;
		}
	
		response = client.execute(new HttpGet(
			String.format("http://mealfire.com/api/v2/%s.js?%s", action, query.toString())));
						
		StringWriter writer = new StringWriter();
		IOUtils.copy(response.getEntity().getContent(), writer);
		json = writer.toString();

		return new JSONArray(json);
	}
}
