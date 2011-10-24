package com.esz.thaifloodreporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import oauth.signpost.OAuthProvider;
//import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
//import twitter4j.auth.AccessToken;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.esz.thaifloodreporter.ThaiFloodOverlay;
import com.esz.thaifloodreporter.TwitterApp.TwDialogListener;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ThaiFloodReporterActivity extends MapActivity {
    
	LocationManager locationManager;
	MapController mapController;
	LinearLayout linearLayout;
	MapView mapView;
	TextView editText;
	Button btPreview,btSendTweet,btLogin;
	int lat;
	int lng;
	Location location;
	GeoPoint point;
	List<Overlay> mapOverlays;
	Drawable drawable;
	ThaiFloodOverlay itemizedOverlay;
	LocationListener locationListener;
	Spinner spinner;
	String TAG;

	private TwitterApp mTwitter;
//	private OAuthProvider provider;
//	private CommonsHttpOAuthConsumer consumer;
//	private int statusId;
	private String twitter_consumer_key =           Constants.CONSUMER_KEY;
	private String twitter_secret_key =        Constants.CONSUMER_SECRET;
//	private String CALLBACK_URL =           Constants.CALLBACK_URL;
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		System.setProperty("http.keepAlive", "false");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
				
		editText=(TextView)findViewById(R.id.editText);
				
		this.btLogin=(Button)this.findViewById(R.id.login);
	    btLogin.setOnClickListener(new View.OnClickListener() {
			@Override
	    	public void onClick(View v) {
				// TODO Auto-generated method stub
				mTwitter.authorize();
			}
			
		});
		locationListener = new LocationListener() {
			@Override
		    public void onLocationChanged(Location location) {
				lat = (int) (location.getLatitude() * 1E6);
		        lng = (int) (location.getLongitude() * 1E6);
				point = new GeoPoint(lat, lng);
				mapController.animateTo(point);
				itemizedOverlay = new ThaiFloodOverlay(drawable);
				OverlayItem overlayitem = new OverlayItem(point, "Your are here !!!", "Your are Reporter");
				itemizedOverlay.addOverlayItems(overlayitem);
				mapOverlays.add(itemizedOverlay);
				locationManager.removeUpdates(locationListener);
		    }

			public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };
		
		locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, 
				locationListener);
		
		mapView=(MapView)findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapController=mapView.getController();
		mapController.setZoom(16);
		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.map);
		
	    spinner = (Spinner) findViewById(R.id.spinner);
//	    String[] a;
//	    String readFeed = readFeed();
//		try {
//			JSONArray jsonArray = new JSONArray(readFeed);
//			for (int i = 0; i < jsonArray.length(); i++) {
//				JSONObject jsonObject = jsonArray.getJSONObject(i);
//				a[i] = jsonObject.getString("wordlist");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	    		this, R.array.tweet_select, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);	    
	    	    
	    this.btPreview=(Button)this.findViewById(R.id.ok);
	    btPreview.setOnClickListener(new View.OnClickListener() {
	    	@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
	    		DecimalFormat dec = new DecimalFormat("#.###");
				String txtGeo = dec.format(lat/1e6) + "," + dec.format(lng / 1e6);
	    		String url = "http://maps.googleapis.com/maps/api/geocode/json?sensor=true&language=th&latlng="+txtGeo;
	    		String place = " ";
	    		
	    		JSONObject json = getJSONfromURL(url);
	    		try {
	    			JSONArray resultsArray = json.getJSONArray("results");
	    			JSONObject jObj1 = resultsArray.getJSONObject(0);
	    			JSONArray addressArray = jObj1.getJSONArray("address_components");
	    			for (int i = 0; i < resultsArray.length(); i++){
	    				JSONObject address = addressArray.getJSONObject(i);
	    				String types = address.getString("types");
	    				if (types.contains("route")) place += address.getString("short_name");	
	    				if (types.contains("locality")) place += address.getString("short_name");
	    				place += " ";
	    			}
	    		} catch (JSONException e){
	    			Log.e(TAG, "Error parsing data" + e.toString());
	    		}
	    		
				String currentDateTimeString = new SimpleDateFormat("dd/MMM hh:mm").format(new Date());
				String txtTweet = "["+currentDateTimeString + "] " +
					spinner.getSelectedItem() + " (" + txtGeo + ") " + place.trim() +" #ThaiFlood";
				editText.setText(txtTweet);
			}
		});
	    
	    this.btSendTweet = (Button)this.findViewById(R.id.tweet);
	    btSendTweet.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String txtTweet = editText.getText().toString();
				// postToTwitter(txtTweet);
				postData();
			}
		});
	    
	    mTwitter 	= new TwitterApp(this, twitter_consumer_key,twitter_secret_key);
		
		mTwitter.setListener(mTwLoginDialogListener);
		
		if (mTwitter.hasAccessToken()) {
			//mTwitterBtn.setChecked(true);
			
			String username = mTwitter.getUsername();
			username		= (username.equals("")) ? "Unknown" : username;
			
			//mTwitterBtn.setText("  Twitter (" + username + ")");
			//mTwitterBtn.setTextColor(Color.WHITE);
			btLogin.setText(username);
		}
	    
	    this.btLogin = (Button) this.findViewById(R.id.login);
	    btLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mTwitter.hasAccessToken()){
					mTwitter.resetAccessToken();
					btLogin.setText("Login");
				} else {
					mTwitter.authorize();
					btLogin.setText(mTwitter.getUsername());
				}
			}
		});
	    
	    Button btRefresh = (Button) this.findViewById(R.id.refresh);
	    btRefresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, 
						locationListener);
				
			}
		});
	}
	
	private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
		@Override
		public void onComplete(String value) {
			String username = mTwitter.getUsername();
			username		= (username.equals("")) ? "No Name" : username;
			btLogin.setText(username);		
			Toast.makeText(ThaiFloodReporterActivity.this, "Connected to Twitter as " + username, Toast.LENGTH_LONG).show();
		}
		
		@Override
		public void onError(String value) {
			btLogin.setText("Login");
			
			Toast.makeText(ThaiFloodReporterActivity.this, "Twitter connection failed", Toast.LENGTH_LONG).show();
		}
	};

	public void postData() {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://thaiflood.heroku.com/eventtests");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
	        nameValuePairs.add(new BasicNameValuePair("lat", "13.8672"));
	        nameValuePairs.add(new BasicNameValuePair("lon", "100.7189"));
	        nameValuePairs.add(new BasicNameValuePair("address", "สามวาตะวันตก"));
	        nameValuePairs.add(new BasicNameValuePair("message", "ยังไม่ท่วม"));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	}
	
	public String readFeed() {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(
				"http://thaiflood.heroku.com/reports.json");
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(ThaiFloodReporterActivity.class.toString(), "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
	
	private void postToTwitter(final String review) {
		new Thread() {
			@Override
			public void run() {
				int what = 0;
				
				try {
					mTwitter.updateStatus(review);
					editText.setText("");
				} catch (Exception e) {
					what = 1;
				}
				
				mHandler.sendMessage(mHandler.obtainMessage(what));
			}
		}.start();
	}
	
	public static JSONObject getJSONfromURL(String url){
		InputStream is = null;
		String result = "";
		JSONObject jArray = null;

	    //http get
	    try{
	            HttpClient httpclient = new DefaultHttpClient();
	            HttpGet httpget = new HttpGet(url);
	            HttpResponse response = httpclient.execute(httpget);
	            HttpEntity entity = response.getEntity();
	            is = entity.getContent();

	    }catch(Exception e){
	            Log.e("log_tag", "Error in http connection "+e.toString());
	    }

	    //convert response to string
	    try{
	            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"),8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                    sb.append(line + "\n");
	            }
	            is.close();
	            result=sb.toString();
	    }catch(Exception e){
	            Log.e("log_tag", "Error converting result "+e.toString());
	    }

	    try{

            jArray = new JSONObject(result);
	    }catch(JSONException e){
	            Log.e("log_tag", "Error parsing data "+e.toString());
	    }

	    return jArray;
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = (msg.what == 0) ? "Posted to Twitter" : "Post to Twitter failed";
			
			Toast.makeText(ThaiFloodReporterActivity.this, text, Toast.LENGTH_SHORT).show();
		}
	};
}