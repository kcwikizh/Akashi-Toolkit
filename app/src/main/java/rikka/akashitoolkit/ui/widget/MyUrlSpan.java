package rikka.akashitoolkit.ui.widget;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import rikka.akashitoolkit.equip_detail.EquipDetailActivity;
import rikka.akashitoolkit.ship_detail.ShipDetailActivity;

/**
 * Created by Rikka on 2016/4/6.
 */
public class MyUrlSpan extends URLSpan {
    public MyUrlSpan(URLSpan src) {
        super(src.getURL());
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        //ds.setColor(ds.linkColor);
        //ds.setColor(Color.parseColor("#8C9EFF"));
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        Context context = widget.getContext();
        if (url.startsWith("akshitoolkit://")) {
            Intent intent = null;
            int id = Integer.parseInt(url.split("/")[3]);
            if (url.contains("equip/")) {
                intent = new Intent(widget.getContext(), EquipDetailActivity.class);
                intent.putExtra(EquipDetailActivity.EXTRA_ITEM_ID, id);
            } else if (url.contains("ship/")) {
                intent = new Intent(widget.getContext(), ShipDetailActivity.class);
                intent.putExtra(ShipDetailActivity.EXTRA_ITEM_ID, id);
            }
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                super.onClick(widget);
            }
        } else {
            super.onClick(widget);
        }
    }
}
