package com.mealfire;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageLoader {
	private static HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();
	
	public static void loadImage(final Activity activity, final ImageView imageView, final String url) {
		// It's in the cache, we're done.
		synchronized (cache) {
			if (cache.containsKey(url)) {
				imageView.setImageBitmap(cache.get(url));
				return;
			}
		}
		
		(new Thread() {
			public void run() {
				final Bitmap bitmap;
				
				try {
					bitmap = loadImage(url);
				} catch (ClientProtocolException e) {
					return;
				} catch (IOException e) {
					return;
				}
				
				activity.runOnUiThread(new Runnable() {
					public void run() {
						imageView.setImageBitmap(bitmap);
					}
				});
				
				synchronized (cache) {
					cache.put(url, bitmap);
				}
			}
		}).start();
	}
	
	private static Bitmap loadImage(String url) throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(new HttpGet(url));
		Bitmap image = BitmapFactory.decodeStream(response.getEntity().getContent());
		
		if (image == null) {
			throw new IOException("Empty image.");
		}
		
		return Utils.getRoundedCornerBitmap(image, 8);
	}
}
