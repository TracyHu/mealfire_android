package com.mealfire.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import com.mealfire.Utils;
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
		
		DefaultHttpClient client = new DefaultHttpClient();
		
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
		
		HttpGet request = new HttpGet(
			String.format("http://mealfire.com/api/v2/%s.js?%s", action, query.toString()));
		
		request.addHeader("Accept-Encoding", "gzip");
		response = client.execute(request);
		
		Header encoding = response.getEntity().getContentEncoding();
		InputStream is = response.getEntity().getContent();
		
		if (encoding != null) {
			for (HeaderElement element : encoding.getElements()) {
				if (element.getName().equalsIgnoreCase("gzip")) {
					is = new GZIPInputStream(is);
				}
			}
		}
						
		StringWriter writer = new StringWriter();
		Utils.copy(is, writer);
		json = writer.toString();
		
		return new JSONArray(json);
	}
}
