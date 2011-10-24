package com.esz.thaifloodreporter;

import android.view.Menu;

public class Constants {

	public static final String CONSUMER_KEY = "PMKMz3efUBC9DcKgscE4w";
	public static final String CONSUMER_SECRET= "P2T28jad8FtIS0ks6tWCl3wsiROfziW5qSMM9bU";

	public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";

	public static final String	OAUTH_CALLBACK_SCHEME	= "tweet";
	public static final String	OAUTH_CALLBACK_HOST		= "thaiflood";
	public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	public static final String	CALLBACK_URL			= "thaiflood://tweet";
	public static final String PREFS_NAME = "TwitterLogin";
	
	public static final int ACTIVITY_LATEST_TWEETS = Menu.FIRST + 6;

}