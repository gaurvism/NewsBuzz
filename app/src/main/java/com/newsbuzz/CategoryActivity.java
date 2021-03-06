package com.newsbuzz;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;

import static com.newsbuzz.DbContract.NEWS_TABLE.CATEGORY;
import static com.newsbuzz.DbContract.NEWS_TABLE.DESCRIPTION;
import static com.newsbuzz.DbContract.NEWS_TABLE.LINK_IMAGE;
import static com.newsbuzz.DbContract.NEWS_TABLE.LINK_MORE;
import static com.newsbuzz.DbContract.NEWS_TABLE.PUBDATE;
import static com.newsbuzz.DbContract.NEWS_TABLE.TITLE;

public class CategoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int READ_CATEGORY = 1;
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private ArrayList<NewsItem> list;
    private LoadToast loadToast;
    private Toolbar toolbar;
    private static final String CATEGORY_NAME ="Category" ;
    private  String category;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent=getIntent();
        if(intent!=null){
            if(intent.hasExtra(CATEGORY_NAME)){
                category=intent.getStringExtra(CATEGORY_NAME);
                toolbar.setTitle("asdfg");
                Log.d("df",category);
            }
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadToast=new LoadToast(this);
        list = new ArrayList<>();
        adapter = new CategoryAdapter(this);
        recyclerView = (RecyclerView) findViewById(R.id.list_category);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        if(new Connection(this).isInternet()){
            sendRequest(getUrl(category));
            loadToast.show();
        }
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        }));
        getLoaderManager().initLoader(READ_CATEGORY, null, this);
    }

    private void sendRequest(String url) {
        StringRequest stringRequest=new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
loadToast.success();
                RssExtractor rssExtractor=new RssExtractor(response);
              ArrayList<NewsItem> list=rssExtractor.getNewsItems();
                for (int i=0;i<list.size();i++){

                    ContentValues values=new ContentValues();
                    values.put(TITLE,list.get(i).title);
                    values.put(LINK_MORE,list.get(i).link_more);
                    values.put(LINK_IMAGE,list.get(i).link_image);
                    values.put(DESCRIPTION,list.get(i).description);
                    values.put(CATEGORY,list.get(i).category);
                    values.put(PUBDATE,list.get(i).pubDate);
                    getContentResolver().insert(DbContract.insertNews(),values);

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                loadToast.error();
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
MySingleton.getInstance(MyApplication.getAppContext()).addToRequestQueue(stringRequest);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, DbContract.getNewsByCategory(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new NewsItem(cursor.getString(cursor.getColumnIndex(DbContract.NEWS_TABLE.TITLE)), cursor.getString(cursor.getColumnIndex(DbContract.NEWS_TABLE.LINK_IMAGE))));

                }
                while (cursor.moveToNext());
            }
            adapter.refresh(list);
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }
    private  String getUrl(String category){
        switch (category){
            case "Top Stories":
                return "http://news.google.co.in/news?cf=all&hl=en&pz=1&ned=in&output=rss";
        }
        return  "";
    }
}
