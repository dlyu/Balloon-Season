package com.balloon.dl;

import android.graphics.Canvas;
import android.os.Bundle;

public abstract class Event {
	protected static final int STATE_INACTIVE = 0;
	protected static final int STATE_ENTERING = 1;
	protected static final int STATE_ACTIVE = 2;
	protected static final int STATE_EXITING  = 3;
	
	protected double mPercentage;
	protected int mDuration;
	protected int mEnterTime;
	protected int mExitTime;
	protected int mTimeCounter;
	protected GameCanvas mGameCanvas;
	
	protected String mKey;
	
	public void setAttributes(double percentage, GameCanvas gc) {
		mPercentage = percentage;
		mGameCanvas = gc;
	}
	
	protected void reset() {
		mTimeCounter = 0;
	}
	
	protected double getPercentage() {
		return mPercentage;
	}
	
	protected boolean update() {
		return update(1);
	}
	protected abstract boolean update(int millisec);
	protected abstract void drawBackground(Canvas canvas);
	protected abstract void drawForeground(Canvas canvas);
	protected abstract void startEvent();
	protected abstract void endEvent();
	protected abstract void backupEventData(String key, Bundle map);
	protected abstract void restoreEventData(String key, Bundle savedState);
}
