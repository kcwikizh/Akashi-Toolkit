package rikka.akashitoolkit.ship_list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.ArrayList;
import java.util.List;

import rikka.akashitoolkit.R;
import rikka.akashitoolkit.adapter.SimpleAdapter;
import rikka.akashitoolkit.expedition.ExpeditionDisplayFragment;
import rikka.akashitoolkit.model.ShipType;
import rikka.akashitoolkit.staticdata.ShipTypeList;
import rikka.akashitoolkit.ui.fragments.DrawerFragment;
import rikka.akashitoolkit.ui.fragments.IBackFragment;

/**
 * Created by Rikka on 2016/10/4.
 */

public class ShipListFragment extends DrawerFragment implements SimpleAdapter.Listener, IBackFragment {

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_recycler_in_frame, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SimpleAdapter<String> adapter = new SimpleAdapter<>(R.layout.item_ship_type);
        mRecyclerView.setAdapter(adapter);

        List<String> list = new ArrayList<>();
        for (ShipType shipType :
                ShipTypeList.get(getContext())) {
            if (shipType.getId() == 1 || shipType.getId() == 12 || shipType.getId() == 15) {
                continue;
            }

            list.add(shipType.getName().get());
        }
        adapter.setItemList(list);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void OnClick(int position) {
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.dir_enter, R.anim.dir_leave, R.anim.dir_enter, R.anim.dir_leave)
                .add(R.id.fragment_container, new ExpeditionDisplayFragment())
                .addToBackStack(null)
                .commit();

        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(150);
        animation.setFillAfter(true);
        animation.setInterpolator(new FastOutSlowInInterpolator());
        mRecyclerView.clearAnimation();
        mRecyclerView.startAnimation(animation);
    }

    @Override
    public boolean onBackPressed() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();

            Animation animation = new AlphaAnimation(0, 1);
            animation.setDuration(350);
            animation.setFillAfter(true);
            animation.setInterpolator(new FastOutSlowInInterpolator());
            mRecyclerView.clearAnimation();
            mRecyclerView.startAnimation(animation);

            onShow();

            return true;
        }
        return false;
    }

    @Override
    public void onShow() {
        super.onShow();

        setToolbarTitle(R.string.ship);
    }
}
