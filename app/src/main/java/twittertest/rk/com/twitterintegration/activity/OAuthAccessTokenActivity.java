package twittertest.rk.com.twitterintegration.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.http.apache.ApacheHttpTransport;

import java.io.IOException;

import twittertest.rk.com.twitterintegration.util.CredentialStore;
import twittertest.rk.com.twitterintegration.util.QueryStringParser;
import twittertest.rk.com.twitterintegration.util.SharedPreferencesCredentialStore;
import twittertest.rk.com.twitterintegration.util.TwitterConstant;

@SuppressLint("SetJavaScriptEnabled")
public class OAuthAccessTokenActivity extends Activity {

	final String TAG = getClass().getName();
	
	private SharedPreferences prefs;

	private boolean handled;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting task to retrieve request token.");
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	private WebView webview;
	
	@Override
	protected void onResume() {
		super.onResume();
		webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);  
        webview.setVisibility(View.VISIBLE);
        setContentView(webview);
        
        handled=false;
        
        new PreProcessToken().execute();
        
	}
	
	private class PreProcessToken extends AsyncTask<Uri, Void, Void> {

		final OAuthHmacSigner signer = new OAuthHmacSigner();
		private String authorizationUrl;
		
		@Override
		protected Void doInBackground(Uri...params) {

			try {
			  
		        signer.clientSharedSecret = TwitterConstant.API_SECRET;
		        
				OAuthGetTemporaryToken temporaryToken = new OAuthGetTemporaryToken(TwitterConstant.REQUEST_URL);
				temporaryToken.transport = new ApacheHttpTransport();
				temporaryToken.signer = signer;
				temporaryToken.consumerKey = TwitterConstant.API_KEY;
				temporaryToken.callback = TwitterConstant.OAUTH_CALLBACK_URL;
				
				OAuthCredentialsResponse tempCredentials = temporaryToken.execute();
				signer.tokenSharedSecret = tempCredentials.tokenSecret;
				
				OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(TwitterConstant.AUTHORIZE_URL);
				authorizeUrl.temporaryToken = tempCredentials.token;
				authorizationUrl = authorizeUrl.build();

		        Log.i(TwitterConstant.TAG, "Using authorizationUrl = " + authorizationUrl);
		        
		        handled=false;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		        
            return null;
		}

		
		/**
		 * When we're done and we've retrieved either a valid token or an error from the server,
		 * we'll return to our original activity 
		 */
		@Override
		protected void onPostExecute(Void result) {

	        Log.i(TAG, "Retrieving request token from Google servers");

	        webview.setWebViewClient(new WebViewClient() {  

	        	@Override  
	            public void onPageStarted(WebView view, String url,Bitmap bitmap)  {  
	        		Log.i(TwitterConstant.TAG, "onPageStarted : " + url + " handled = " + handled);
	            }
	        	@Override  
	            public void onPageFinished(final WebView view, final String url)  {
	        		Log.i(TwitterConstant.TAG, "onPageFinished : " + url + " handled = " + handled);
	        		
	        		if (url.startsWith(TwitterConstant.OAUTH_CALLBACK_URL)) {
		        		if (url.indexOf("oauth_token=")!=-1) {
			        		webview.setVisibility(View.INVISIBLE);
			        		
			        		if (!handled) {
			        			new ProcessToken(url,signer).execute();
			        		}
		        		} else {
		        			webview.setVisibility(View.VISIBLE);
		        		}
	        		}
	            }

	        });  
	        
	        webview.loadUrl(authorizationUrl);	

		}

	}	
	
	private class ProcessToken extends AsyncTask<Uri, Void, Void> {

		String url;
		private OAuthHmacSigner signer;
		
		public ProcessToken(String url,OAuthHmacSigner signer) {
			this.url=url;
			this.signer = signer;
		}
		
		@Override
		protected Void doInBackground(Uri...params) {

			Log.i(TwitterConstant.TAG, "doInbackground called with url " + url);
			if (url.startsWith(TwitterConstant.OAUTH_CALLBACK_URL) && !handled) {
        		try {
					
        			if (url.indexOf("oauth_token=")!=-1) {
        				handled=true;
            			String requestToken  = extractParamFromUrl(url,"oauth_token");
            			String verifier= extractParamFromUrl(url,"oauth_verifier");
						
            			signer.clientSharedSecret = TwitterConstant.API_SECRET;

            			OAuthGetAccessToken accessToken = new OAuthGetAccessToken(TwitterConstant.ACCESS_URL);
            			accessToken.transport = new ApacheHttpTransport();
            			accessToken.temporaryToken = requestToken;
            			accessToken.signer = signer;
            			accessToken.consumerKey = TwitterConstant.API_KEY;
            			accessToken.verifier = verifier;

            			OAuthCredentialsResponse credentials = accessToken.execute();
            			signer.tokenSharedSecret = credentials.tokenSecret;

            			CredentialStore credentialStore = new SharedPreferencesCredentialStore(prefs);
						credentialStore.write(new String[] {credentials.token,credentials.tokenSecret});
			  		      
        			} else if (url.indexOf("error=")!=-1) {
        				new SharedPreferencesCredentialStore(prefs).clearCredentials();
        			}
        			
				} catch (IOException e) {
					e.printStackTrace();
				}

        	}
            return null;
		}

		private String extractParamFromUrl(String url,String paramName) {
			String queryString = url.substring(url.indexOf("?", 0)+1,url.length());
			QueryStringParser queryStringParser = new QueryStringParser(queryString);
			return queryStringParser.getQueryParamValue(paramName);
		}   
		
		@Override
		protected void onPreExecute() {
			
		}

		/**
		 * When we're done and we've retrieved either a valid token or an error from the server,
		 * we'll return to our original activity 
		 */
		@Override
		protected void onPostExecute(Void result) {
			startActivity(new Intent(OAuthAccessTokenActivity.this,TimeLineActivity.class));
			finish();
		}

	}	

}
