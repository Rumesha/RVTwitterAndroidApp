package twittertest.rk.com.twitterintegration.activity;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Status;
import twittertest.rk.com.twitterintegration.Adapter.TimeLineRecyclerAdapter;
import twittertest.rk.com.twitterintegration.R;
import twittertest.rk.com.twitterintegration.util.TwitterUtils;

public class TimeLineActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "TimeLineActivity";
    private List<twitter4j.Status> statusList;
    private RecyclerView mRecyclerView;
    private TimeLineRecyclerAdapter adapter;
    private ProgressBar progressBar;
    private SharedPreferences prefs;
    private SwipeRefreshLayout swipeRefreshLayout;
    List<twitter4j.Status> curentDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //Initialize swipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setOnRefreshListener(this);
        new TwitterExecutor().execute();
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
           @Override
            public void run() {
               swipeRefreshLayout.setRefreshing(true);

               new TwitterExecutor().execute();
           }
        });

    }

    @Override
    public void onRefresh() {
        new TwitterExecutor().execute();
    }


    private class TwitterExecutor extends AsyncTask<Uri, Void, Integer> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            // showing refresh animation before making http call
            swipeRefreshLayout.setRefreshing(true);
            if(progressDialog == null){
                progressDialog = new ProgressDialog(TimeLineActivity.this);
                progressDialog.setMessage("Downloading...");
            }
            if(!swipeRefreshLayout.isShown())
                progressDialog.show();


        }

        @Override
        protected Integer doInBackground(Uri...params) {
            Integer result = 0;
            try {
                ResponseList<twitter4j.Status> homeTimeline = TwitterUtils.getHomeTimeline(prefs);
                statusList = new ArrayList<twitter4j.Status>();
                for (twitter4j.Status status : homeTimeline) {
                    statusList.add(status);
                    result = 1;
                }

            } catch (Exception ex) {
                result = 0;
                ex.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (progressDialog != null)
                progressDialog.dismiss();
            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);

            if (result == 1) {
                adapter = new TimeLineRecyclerAdapter(TimeLineActivity.this, statusList);
                mRecyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(TimeLineActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public List<twitter4j.Status> createLimitDataList(List<twitter4j.Status> allStatusList, List<twitter4j.Status> currentList,boolean scrol){

        if(scrol){
            for(int i = currentList.size(); i < currentList.size()+10; i++){
                Status item = statusList.get(i);
                currentList.add(item);

            }
            return currentList;

        } else {
            for(int i = 0; i < 10; i++){
                Status item = statusList.get(i);
                currentList.add(item);

            }
            return currentList;
        }

    }


}
