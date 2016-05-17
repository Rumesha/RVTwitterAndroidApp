package twittertest.rk.com.twitterintegration.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import twittertest.rk.com.twitterintegration.R;
import twittertest.rk.com.twitterintegration.util.TwitterUtils;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_login);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TwitterUtils.isConnectingToInternet(MainActivity.this)){
                    startActivity(new Intent(MainActivity.this,OAuthAccessTokenActivity.class));
                    finish();
                }else {
                    Toast.makeText(getApplicationContext(),"Check your Internet Conection",Toast.LENGTH_SHORT).show();
                }



            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
       new HttpRequestTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new HttpRequestTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class HttpRequestTask extends AsyncTask<Void, Void, Boolean> {
        boolean result = false;
        private ProgressBar progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(progressBar == null){
                progressBar = new ProgressBar(getApplicationContext());
                progressBar.setVisibility(View.VISIBLE);
           }

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                 result = TwitterUtils.isAuthenticated(prefs);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
            }
            if(result){
                startActivity(new Intent(getApplicationContext(),TimeLineActivity.class));
                finish();
            }
        }

    }

}
