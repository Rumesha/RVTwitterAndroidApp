package twittertest.rk.com.twitterintegration.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import twitter4j.Status;
import twittertest.rk.com.twitterintegration.R;

/**
 * Created by ASER ASPIRE on 5/16/2016.
 */
public class TimeLineRecyclerAdapter  extends RecyclerView.Adapter<TimeLineRecyclerAdapter.CustomViewHolder> {

    private final Context mContext;
    private List<Status> tweetList;
    
    public TimeLineRecyclerAdapter(Context context, List<Status> tweetList) {
        this.tweetList = tweetList;
        this.mContext = context;
    }




    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Status tweetItem = tweetList.get(i);

        //Download image using picasso library
        Picasso.with(mContext).load(tweetItem.getUser().getProfileImageURL())
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(customViewHolder.imageView);

        //Setting text view title
        customViewHolder.textView.setText(Html.fromHtml(tweetItem.getText()));
    }
    @Override
    public int getItemCount() {
        return (null != tweetList ? tweetList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;
        protected TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            this.textView = (TextView) view.findViewById(R.id.title);
        }
    }
}
