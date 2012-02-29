package com.balloon.dl;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class SunnyEvent extends Event {
	private Drawable mSun;
	
	private int mSunHeight;
	private int mSunWidth;
	private Rect mBounds;
	private int mCurrentState;
	private int mAnimCounter;
	private int mCanvasWidth;
	
	public SunnyEvent() {
		mKey = "SunEvent";
	}
	
	public void setAttributes(int duration, double percentage, GameCanvas gc) {
		super.setAttributes(percentage, gc);
		mDuration = (int)((0.8)*duration);
		mEnterTime = (int)((0.1)*duration);
		mExitTime = (int)((0.1)*duration);
		mSun = mGameCanvas.mContext.getResources().getDrawable(R.drawable.sun);
		mBounds = new Rect();
		mSunHeight = mSun.getIntrinsicHeight();
		mSunWidth = mSun.getIntrinsicWidth();
		mCanvasWidth = gc.getGameWidth();
		reset();
	}

	@Override
	protected void endEvent() {
		//mGameCanvas.mAlwaysCombo = false;
		mGameCanvas.mBalloonList.setOnlyColour(-1);
		reset();
	}

	protected void reset() {
		super.reset();
		mCurrentState = STATE_INACTIVE;
		mAnimCounter = 0;
		mBounds.set(mCanvasWidth, 0 - mSunHeight, mCanvasWidth + mSunWidth, 0);
	}
	
	@Override
	protected void startEvent() {
		//mGameCanvas.mAlwaysCombo = true;0;
		int colour = mGameCanvas.mLastColour < 0 ? (int)Math.random() * BalloonManager.COLOUR_COUNT : mGameCanvas.mLastColour;
		mGameCanvas.mBalloonList.setOnlyColour(colour);
		mCurrentState = STATE_ENTERING;
	}

	@Override
	protected boolean update(int millisec) {
		switch (mCurrentState) {
			case STATE_ENTERING:
				mAnimCounter+=millisec;
				if (mAnimCounter > mEnterTime) {
					mCurrentState = STATE_ACTIVE;
					mAnimCounter = 0;
				}
					
				else {
					double ratio = ((double)mAnimCounter/(double)mEnterTime);
					int x = (int)(mSunWidth*ratio);
					int y = (int)(mSunHeight*ratio);
					/*
						bounds.bottom = mSun.getIntrinsicHeight();
						bounds.top = 0;
						bounds.right = mGameCanvas.mCanvasWidth;
						bounds.left = bounds.right - mSun.getIntrinsicWidth();
					 * */
					mBounds.set(mCanvasWidth - x, y - mSunHeight, mCanvasWidth + mSunWidth - x, y);
				}
				break;
			case STATE_ACTIVE:
				mTimeCounter+=millisec;
				if (mTimeCounter > mDuration)
					mCurrentState = STATE_EXITING;
				break;
			case STATE_EXITING:
				mAnimCounter+=millisec;
				if (mAnimCounter > mExitTime) {
					endEvent();
					return false;
				}
				else {
					double ratio = ((double)mAnimCounter/(double)mExitTime);
					int x = (int)(mSunWidth*ratio);
					int y = (int)(mSunHeight*ratio);
					
					mBounds.set(mCanvasWidth - mSunWidth + x, 0 - y, mCanvasWidth + x, mSunHeight - y);
				}
				break;
			default:
				break;
		}

		return true;
	}

	@Override
	protected void drawBackground(Canvas canvas) {
		mSun.setBounds(mBounds);
		mSun.draw(canvas);
	}

	@Override
	protected void drawForeground(Canvas canvas) {
		//Nothing to draw in the foreground
	}

	@Override
	protected void backupEventData(String key, Bundle map) {
		int[] data = new int[7];
		data[0] = mCurrentState;
		data[1] = mAnimCounter;
		data[2] = mTimeCounter;
		data[3] = mBounds.left;
		data[4] = mBounds.top;
		data[5] = mBounds.right;
		data[6] = mBounds.bottom;
		map.putIntArray(key, data);
	}

	@Override
	protected void restoreEventData(String key, Bundle savedState) {
		int[] data = savedState.getIntArray(key);
		if (data != null) {
			mCurrentState = data[0];
			mAnimCounter = data[1];
			mTimeCounter = data[2];
			mBounds.set(data[3], data[4], data[5], data[6]);
		}
	}

}
