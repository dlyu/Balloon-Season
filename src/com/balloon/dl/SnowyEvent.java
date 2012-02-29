package com.balloon.dl;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class SnowyEvent extends Event {
	private int mAnimCounter;

	private Paint mSkyOverlay;
	private int mBGAlpha;
	private final int MAX_ALPHA = 90;
	private final int RGB = 90;
	
	private int mCurrentState;

	private final int mSnowOnScreen = 30;
	private final int mSnowAmplitude = 3;
	private final double mSnowMaxXStep = 0.05;
	private final int mSnowMaxYStep = 2;
	
	private double[] mSnowDeltaX;
	private int[] mSnowOscillationX;
	private int[] mSnowX;
	private int[] mSnowY;
	private double[] mSnowXStep;
	private int[] mSnowYStep;
	
	private Rect mBounds;
	private Drawable mSnow;
	private int mSnowWidth;
	private int mSnowAlpha;
	
	public SnowyEvent() {
		mKey = "SnowEvent";
	}
	
	public void setAttributes(int duration, double percentage, GameCanvas gc) {
		super.setAttributes(percentage, gc);
		mDuration = (int)((5.0/7.0)*duration);
		mEnterTime = (int)((1.0/7.0)*duration);
		mExitTime = (int)((1.0/7.0)*duration);
		
		mSnow = gc.mContext.getResources().getDrawable(R.drawable.snow_drop);
		mSnowWidth = mSnow.getIntrinsicWidth();

	    mAnimCounter = 0;
	    mSkyOverlay = new Paint();
	    mBounds = new Rect();
	    
	    mSnowDeltaX = new double[mSnowOnScreen];
	    mSnowOscillationX = new int[mSnowOnScreen];
	    mSnowX = new int[mSnowOnScreen];
	    mSnowY = new int[mSnowOnScreen];
	    mSnowXStep = new double[mSnowOnScreen];
	    mSnowYStep = new int[mSnowOnScreen];
	    
	    reset();
	}
	
	protected void reset() {
		super.reset();
		mAnimCounter = 0;
	    mCurrentState = STATE_INACTIVE;
	    mSkyOverlay.setARGB(0, 0, 0, RGB);
	    mSnowAlpha = 255;
	    mBGAlpha = 0;
	    
		for (int i = 0; i < mSnowOnScreen; i++) { 
			resetSnowDrop(i); 
		}
	}

	@Override
	protected void endEvent() {
		mGameCanvas.mBalloonList.restoreDefaultSpeed();
		mGameCanvas.mTimerOn = true;
		reset();
	}

	@Override
	protected void startEvent() {
		mGameCanvas.mBalloonList.overrideDefaultSpeed(0, 2, 1, 2);
		Balloon curr = (Balloon)mGameCanvas.mBalloonList.getActiveHead();
		while (curr != null) {
			int xSpeed = curr.getXSpeed();
			int ySpeed = curr.getYSpeed()/2;
			if (ySpeed == 0)
				ySpeed = -1;
			
			if (xSpeed != 0)
				curr.setXSpeed(xSpeed/(int)Math.abs(xSpeed));
			curr.setYSpeed(ySpeed);
			curr = (Balloon)curr.getNext();
		}
		mGameCanvas.mTimerOn = false;

		mCurrentState = STATE_ENTERING;
	}

	@Override
	protected boolean update(int millisec) {
		mTimeCounter+=millisec;
		
		switch (mCurrentState) {
			case STATE_ENTERING:
				mAnimCounter+=millisec;
				if (mAnimCounter > mEnterTime) {
					mCurrentState = STATE_ACTIVE;
					mAnimCounter = 0;
				}
					
				else {
					//Update alpha so that it becomes less transparent
					mBGAlpha = (int)(((double)mAnimCounter/(double)mEnterTime)*MAX_ALPHA); 
					mSkyOverlay.setARGB(mBGAlpha, 0, 0, RGB);
				}
				break;
			case STATE_ACTIVE:
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
					double ratio = 1 - (double)mAnimCounter/(double)mExitTime;
					mBGAlpha = (int)(ratio*MAX_ALPHA); 
					mSnowAlpha = (int)(ratio * 255);
					mSkyOverlay.setARGB(mBGAlpha, 0, 0, RGB);
				}
				break;
			default:
				break;
		}
    	for (int i = 0; i < mSnowOnScreen; i++) {
    		mSnowY[i] += 2*mSnowYStep[i];
    		if (mSnowY[i] > mGameCanvas.getGameHeight() && mCurrentState != STATE_EXITING)
    			resetSnowDrop(i);
    		mSnowDeltaX[i] += 2*mSnowXStep[i];
    		mSnowX[i] += (int)(mSnowOscillationX[i] * Math.sin((double)((0.75)*mSnowDeltaX[i])));
    	}

		return true;
	}

	@Override
	protected void drawBackground(Canvas canvas) {
		//canvas.drawBitmap(mSnowyWeatherLayer, 0, 0, null);
		canvas.drawPaint(mSkyOverlay);
	}

	@Override
	protected void drawForeground(Canvas canvas) {
		for (int i = 0; i < mSnowOnScreen; i++) {
			//Accelerator to prevent the drawing of snow drops outside of the screen (during STATE_EXITING)
			if (mSnowY[i] > mGameCanvas.getGameHeight())
				continue;
			
			mBounds.left = mSnowX[i];
			mBounds.top = mSnowY[i];
			mBounds.right = mSnowX[i] + mSnowWidth;
			mBounds.bottom = mSnowY[i] + mSnowWidth;
			
			mSnow.setAlpha(mSnowAlpha);
			mSnow.setBounds(mBounds);
			mSnow.draw(canvas);
		}
	}

	private void resetSnowDrop(int index) {
	    mSnowDeltaX[index] = 0;
	    mSnowOscillationX[index] = (int)(Math.random() * mSnowAmplitude);
	    mSnowX[index] = (int)(Math.random() * mGameCanvas.getGameWidth());
	    mSnowY[index] = -1*(int)(Math.random() * mGameCanvas.getGameHeight());
	    mSnowXStep[index] = Math.random() * mSnowMaxXStep;
	    mSnowYStep[index] = 1 + (int)(Math.random() * (mSnowMaxYStep - 1));	
	}

	@Override
	protected void backupEventData(String key, Bundle map) {
		int[] data = new int[5];
		data[0] = mCurrentState;
		data[1] = mAnimCounter;
		data[2] = mTimeCounter;
		data[3] = mSnowAlpha;
		data[4] = mBGAlpha;
		map.putIntArray(key, data);
	}

	@Override
	protected void restoreEventData(String key, Bundle savedState) {
		int[] data = savedState.getIntArray(key);
		if (data != null) {
			mCurrentState = data[0];
			mAnimCounter = data[1];
			mTimeCounter = data[2];
			mSnowAlpha = data[3];
			mBGAlpha = data[4];
			mSkyOverlay.setARGB(mBGAlpha, 0, 0, RGB);
		}
	}
}
