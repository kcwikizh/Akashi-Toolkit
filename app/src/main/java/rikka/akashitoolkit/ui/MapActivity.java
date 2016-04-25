package rikka.akashitoolkit.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import rikka.akashitoolkit.R;
import rikka.akashitoolkit.model.EnemyShip;
import rikka.akashitoolkit.model.MapDetail;
import rikka.akashitoolkit.model.Ship;
import rikka.akashitoolkit.staticdata.EnemyShipList;
import rikka.akashitoolkit.staticdata.MapDetailList;
import rikka.akashitoolkit.staticdata.ShipList;
import rikka.akashitoolkit.utils.KCStringFormatter;
import rikka.akashitoolkit.utils.Utils;
import rikka.akashitoolkit.widget.ExpandableLayout;

public class MapActivity extends BaseItemDisplayActivity {
    public static final String EXTRA_ITEM_ID = "EXTRA_ITEM_ID";
    public static final String EXTRA_ITEM_NAME = "EXTRA_ITEM_NAME";

    private int mSeaId;
    private int mId;
    private String mTitle;
    private MapDetail mItem;

    private Toolbar mToolbar;
    private LinearLayout mContentContainer;
    private CoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private NestedScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ITEM_ID)) {
            mId = intent.getIntExtra(EXTRA_ITEM_ID, 11);
            mItem = MapDetailList.findItemById(this, mId);

            mSeaId = mId / 10;
            mId = mId % 10;
            mTitle = intent.getStringExtra(EXTRA_ITEM_NAME);
        }

        if (mItem == null) {
            finish();
            return;
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mContentContainer = (LinearLayout) findViewById(R.id.linearLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        setViews();
    }

    @Override
    protected ViewGroup getRootView() {
        return mCoordinatorLayout;
    }

    @Override
    protected View[] getAnimFadeViews() {
        return new View[0];
    }

    @Override
    protected String getTaskDescriptionLabel() {
        return mTitle;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("DefaultLocale")
    private void setViews() {
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(mTitle);

        if (isNightMode()) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
            mToolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
            mToolbar.setSubtitleTextColor(Color.parseColor("#DE000000"));
        } else {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_24dp_dark);
            mToolbar.setTitleTextColor(Color.parseColor("#000000"));
            mToolbar.setSubtitleTextColor(Color.parseColor("#ff757575"));
        }

        String url = Utils.getKCWikiFileUrl(String.format("Map%d-%d.png", mSeaId, mId));
        Log.d(MapActivity.class.getSimpleName(), url);

        Glide.with(this)
                .load(url)
                .crossFade()
                .into((ImageView) findViewById(R.id.imageView));

        try {
            ViewGroup parent;
            parent = (ViewGroup) addCell(mContentContainer, "路线分歧").findViewById(R.id.content_container);
            addBranch(parent);
            parent = (ViewGroup) addCell(mContentContainer, "敌舰配置").findViewById(R.id.content_container);
            addPoint(parent);
            parent = (ViewGroup) addCell(mContentContainer, "掉落").findViewById(R.id.content_container);
            addDrop(parent);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private ViewGroup addCell(ViewGroup parent, int ResId) {
        return addCell(parent, getString(ResId));
    }

    private ViewGroup addCell(ViewGroup parent, String title) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.item_map, parent, false);
        ((TextView) view.findViewById(android.R.id.title)).setText(title);
        parent.addView(view);

        final ExpandableLayout expandableLayout = ((ExpandableLayout) view.findViewById(R.id.expandableLayout));
        expandableLayout.setOnHeightUpdatedListener(new ExpandableLayout.OnHeightUpdatedListener() {
            @Override
            public void OnHeightUpdate(ExpandableLayout v, int height, int change) {
                if (v.isExpanded()) {
                    mScrollView.scrollTo(mScrollView.getScrollX(), mScrollView.getScrollY() + change);
                }
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.toggle();
            }
        });
        return view;
    }

    private void addBranch(ViewGroup parent) {
        for (MapDetail.RouteEntity branch:
                mItem.getRoute()) {
            addBranch(parent, branch);
        }
    }

    private void addBranch(ViewGroup parent, MapDetail.RouteEntity branch) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.item_map_branch, parent, false);
        ((TextView) view.findViewById(R.id.textView)).setText(branch.getStart());
        ((TextView) view.findViewById(R.id.text_content)).setText(branch.getCondition());

        parent.addView(view);
    }
    
    private void addPoint(ViewGroup parent) {
        for (MapDetail.PointsEntity point : mItem.getPoints()) {
            addPoint(parent, point);
        }
    }

    private void addPoint(ViewGroup parent, MapDetail.PointsEntity point) {
        switch (point.getType()) {
            case 0:
                addEnemy(parent, point);
                break;
        }
    }
    
    private void addEnemy(ViewGroup parent, MapDetail.PointsEntity point) {
        String lastPoint = "";

        for (MapDetail.PointsEntity.FleetsEntity fleets: point.getFleets()) {
            ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.item_map_enemy, parent, false);

            if (!lastPoint.equals(point.getPoint())) {
                lastPoint = point.getPoint();
                ((TextView) view.findViewById(R.id.textView)).setText(point.getPoint());
            }

            ((TextView) view.findViewById(R.id.textView2)).setText(KCStringFormatter.getFormation(fleets.getFormation()));

            StringBuilder sb = new StringBuilder();
            for (Integer shipId:
                    fleets.getShips()) {
                EnemyShip ship = EnemyShipList.findItemById(this, shipId);
                if (ship != null) {
                    sb.append(ship.getName().get(this)).append("\t");
                }
            }
            ((TextView) view.findViewById(R.id.text_content)).setText(sb.toString());

            parent.addView(view);
        }
    }

    private void addDrop(ViewGroup parent) {
        for (MapDetail.DropEntity drop:
                mItem.getDrop()) {
            addDrop(parent, drop);
        }
    }

    private void addDrop(ViewGroup parent, MapDetail.DropEntity drop) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.item_map_drop, parent, false);
        ((TextView) view.findViewById(R.id.textView)).setText(drop.getName());

        StringBuilder sb = new StringBuilder();
        for (Integer shipId:
             drop.getShip()) {
            Ship ship = ShipList.findItemById(this, shipId);
            if (ship != null) {
                sb.append(ship.getName().get(this)).append("\t");
            }
        }
        ((TextView) view.findViewById(R.id.text_content)).setText(sb.toString());
        parent.addView(view);
    }
}
