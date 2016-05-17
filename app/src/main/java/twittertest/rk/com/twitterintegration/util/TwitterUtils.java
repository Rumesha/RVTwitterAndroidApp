package twittertest.rk.com.twitterintegration.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by ASER ASPIRE on 5/16/2016.
 */
public class TwitterUtils {

	public static boolean isAuthenticated(SharedPreferences prefs) {

		String[] tokens = new SharedPreferencesCredentialStore(prefs).read();
		AccessToken a = new AccessToken(tokens[0],tokens[1]);
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(TwitterConstant.API_KEY, TwitterConstant.API_SECRET);
		twitter.setOAuthAccessToken(a);
		
		try {
			twitter.getAccountSettings();
			return true;
		} catch (TwitterException e) {
			return false;
		}
	}
	
	public static ResponseList<Status> getHomeTimeline(SharedPreferences prefs) throws Exception {
		String[] tokens = new SharedPreferencesCredentialStore(prefs).read();
		AccessToken a = new AccessToken(tokens[0],tokens[1]);
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(TwitterConstant.API_KEY, TwitterConstant.API_SECRET);
		twitter.setOAuthAccessToken(a);
		Paging paging = new Paging(1,200);
        ResponseList<Status> homeTimeline = twitter.getHomeTimeline(paging);
        return homeTimeline;
	}
	
	public static void sendTweet(SharedPreferences prefs,String msg) throws Exception {
		String[] tokens = new SharedPreferencesCredentialStore(prefs).read();
		AccessToken a = new AccessToken(tokens[0],tokens[1]);
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(TwitterConstant.API_KEY, TwitterConstant.API_SECRET);
		twitter.setOAuthAccessToken(a);
        twitter.updateStatus(msg);
	}

	public static boolean isConnectingToInternet(Context context){
		ConnectivityManager cm =
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return (activeNetwork != null &&
				activeNetwork.isConnectedOrConnecting());

	}
}
