package com.balloon.dl.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class HUD extends RelativeLayout {

	protected Context mContext;

	public HUD(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
}
