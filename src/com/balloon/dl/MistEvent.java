package com.balloon.dl;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;

public class MistEvent extends Event {
	private final int MAX_ALPHA = 230;
	private final int RGB = 120;
	
	private int mAnimCounter;
	private int mCurrentState;
	private int mAlpha;
	private Paint mMistOverlay;

	public MistEvent() {
		mKey = "MistEvent";
	}
	
	public void setAttributes(int duration, double percentage, GameCanvas gc) {
		super.setAttributes(percentage, gc);
		mDuration = (int)((7.0/9.0)*duration);
		mEnterTime = (int)((1.0/9.0)*duration);
		mExitTime = (int)((1.0/9.0)*duration);
		mMistOverlay = new Paint();
	}

	protected void reset() {
		super.reset();
		mAnimCounter = 0;
		mCurrentState = STATE_INACTIVE;
		mAlpha = 0;
		mMistOverlay.setARGB(0, RGB, RGB, RGB);
	}


	@Override
	protected void endEvent() {
		reset();
	}

	@Override
	protected void startEvent() {
		// Enable fog foreground
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
					//Update alpha so that it becomes less transparent
					mAlpha = (int)(((double)mAnimCounter/(double)mEnterTime)*MAX_ALPHA); 
					mMistOverlay.setARGB(mAlpha, RGB, RGB, RGB);
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
					//Update alpha so that it becomes more transparent
					mAlpha = (int)((1 - (double)mAnimCounter/(double)mExitTime)*MAX_ALPHA); 
					mMistOverlay.setARGB(mAlpha, RGB, RGB, RGB);
				}
				break;
			default:
				break;
		}

		return true;
	}
	
	@Override
	protected void drawBackground(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void drawForeground(Canvas canvas) {
		canvas.drawPaint(mMistOverlay);
	}

	@Override
	protected void backupEventData(String key, Bundle map) {
		int[] data = new int[4];
		data[0] = mCurrentState;
		data[1] = mAnimCounter;
		data[2] = mTimeCounter;
		data[3] = mAlpha;
		map.putIntArray(key, data);
		
		//Log.d("Data", "Current State = " + data[0] + ", Animation Counter = " + data[1] + ", Time Counter = " + data[2] + ", Alpha = " + data[3]);
	}

	@Override
	protected void restoreEventData(String key, Bundle savedState) {
		int[] data = savedState.getIntArray(key);
		if (data != null) {
			mCurrentState = data[0];
			mAnimCounter = data[1];
			mTimeCounter = data[2];
			mAlpha = data[3];
			mMistOverlay.setARGB(mAlpha, RGB, RGB, RGB);
			//Log.d("Data", "Current State = " + data[0] + ", Animation Counter = " + data[1] + ", Time Counter = " + data[2] + ", Alpha = " + data[3]);
		}
	}
}
