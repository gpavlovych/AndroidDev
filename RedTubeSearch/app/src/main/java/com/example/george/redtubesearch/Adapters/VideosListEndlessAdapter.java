package com.example.george.redtubesearch.Adapters;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.example.george.redtubesearch.Contract.VideoItem;
import com.example.george.redtubesearch.R;
import com.example.george.redtubesearch.Tasks.DownloadVideoListXmlTask;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 10/11/2015.
 */
public class VideosListEndlessAdapter extends EndlessAdapter {
    private static final String REDTUBE_ORDERING_URL = "&ordering=%s";
    private static final String REDTUBE_PERIOD_URL = "&period=%s";
    private final String searchTerm;
    private static final String REDTUBE_URL = "http://api.redtube.com/?data=redtube.Videos.searchVideos&output=xml&thumbsize=all&page=%s";
    private static final String REDTUBE_SEARCH_URL="&search=%s";
    private static final String REDTUBE_CATEGORY_URL="&category=%s";
    private static final String REDTUBE_TAGS_URL="&tags[]=%s";
    private static final String REDTUBE_STARS_URL="&stars[]=%s";
    private final boolean isSorted;
    private final String sortingMethod;
    private final String sortingParameter;
    private final boolean hasCategory;
    private final String category;
    private final String tags;
    private final String stars;
    private View pendingView = null;
    private RotateAnimation rotate=null;

    public VideosListEndlessAdapter(Activity activity, ArrayList<VideoItem> list, String searchTerm, boolean isSorted, String sortingMethod, String sortingParameter,boolean hasCategory, String category, String tags,String stars) {
        super(new VideosListAdapter(activity,list));
        this.isSorted = isSorted;
        this.sortingMethod = sortingMethod;
        this.sortingParameter = sortingParameter;
        this.hasCategory = hasCategory;
        this.category = category;
        this.tags = tags;
        this.stars = stars;
        rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotate.setDuration(600);
        rotate.setRepeatMode(Animation.RESTART);
        rotate.setRepeatCount(Animation.INFINITE);
        this.searchTerm = searchTerm;
    }
    private int pageNumber = 0;
    private List<VideoItem> _items;
    void startProgressAnimation() {
        if (pendingView!=null) {
            pendingView.startAnimation(rotate);
        }
    }
    @Override
    protected View getPendingView(ViewGroup parent){
        View row= LayoutInflater.from(parent.getContext()).inflate(R.layout.thumblist, null);

        pendingView=row.findViewById(R.id.icon);
        pendingView.setVisibility(View.GONE);
        pendingView=row.findViewById(R.id.itemName);
        pendingView.setVisibility(View.GONE);
        pendingView=row.findViewById(R.id.throbber);
        pendingView.setVisibility(View.VISIBLE);
        startProgressAnimation();

        return(row);
    }
    @Override
    protected boolean cacheInBackground() throws Exception {
        pageNumber++;
        try {
            String url = (String.format(REDTUBE_URL, pageNumber) +
                    ((!TextUtils.isEmpty(searchTerm)) ? String.format(REDTUBE_SEARCH_URL, URLEncoder.encode(searchTerm, java.nio.charset.StandardCharsets.UTF_8.name()), searchTerm) : "") +
                    (isSorted ? String.format(REDTUBE_ORDERING_URL, sortingMethod) : "") +
                    (isSorted && (sortingMethod.equals("rating") || sortingMethod.equals("mostviewed")) ? String.format(REDTUBE_PERIOD_URL, sortingParameter) : "") +
                    (hasCategory ? String.format(REDTUBE_CATEGORY_URL, URLEncoder.encode(category, java.nio.charset.StandardCharsets.UTF_8.name())) : "") +
                    ((!TextUtils.isEmpty(tags)) ? String.format(REDTUBE_TAGS_URL, URLEncoder.encode(tags, java.nio.charset.StandardCharsets.UTF_8.name())) : "") +
                    ((!TextUtils.isEmpty(stars)) ? String.format(REDTUBE_STARS_URL, URLEncoder.encode(stars, java.nio.charset.StandardCharsets.UTF_8.name())) : ""));
            _items = new DownloadVideoListXmlTask().execute(url).get().getVideos();
        } catch (Exception e) {
            _items = null;
        }
        return _items != null && _items.size() > 0;
    }

    @Override
    protected void appendCachedData() {
        VideosListAdapter a=(VideosListAdapter)getWrappedAdapter();
        if (_items != null)
        for (VideoItem videoItem : _items) { a.add(videoItem); }
    }
}
