package com.balloon.dl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Cloud extends GameObject{
	private Bitmap mSpriteSheet;
	private int mCloudID;

	public final int ONSCREEN = 0;
	public final int OFFSCREEN_X = 1;
	public final int OFFSCREEN_Y = 2;
	
	private final int mTotalFrames = 7;
	private final int mFPS = 1000/12;
	private long mFrameTimer;
	private int mCurrentFrame;
	private Rect mSRectangle;
	
	private int mCloudHeight;
	private int mCloudWidth;
	
	public Cloud () {
		super();
	}
	
	public void setAttributes(Bitmap sprite, int cloudID, int x, int y, int xSp, int ySp) {
		mSpriteSheet = sprite;
		mCloudHeight = mSpriteSheet.getHeight();
		mCloudWidth = mSpriteSheet.getWidth()/mTotalFrames;
		
		mFrameTimer = 0;
		mCurrentFrame = 0;
		mSRectangle = new Rect();
		
		mCloudID = cloudID;
		updateSpeed(xSp, ySp);
		
		mX = x;
		mY = y;
	
		if (xSp > 0)
			this.mX-=mCloudWidth;
	}
	
	public boolean updatePosition(int minX, int maxX) {
		mX += mXSpeed; 
		mY += mYSpeed;
		
		if (mX + mCloudWidth < minX || mX > maxX)
			return false;
		
		return true;
	}

	public void updateSpeed(int xSpeed, int ySpeed) {
		this.mXSpeed = xSpeed;
		this.mYSpeed = ySpeed;
	}
	
	public int getCloudID() {
		return mCloudID;
	}

	@Override
	public Rect getHitBox(int offset) {
		mHitbox.set(mX - offset, mY - offset, mX + mCloudWidth + offset, mY + mCloudHeight + offset);
		
		return mHitbox;
	}

	@Override
	public void draw(Canvas canvas) {
		Rect bounds = getHitBox(0);
	    canvas.drawBitmap(mSpriteSheet, mSRectangle, bounds, null);
	}

	@Override
	public boolean update(int interval) {
		mFrameTimer+=interval;
	    if(mFrameTimer > mFPS ) {
	        mFrameTimer = 0;
	        mCurrentFrame +=1;
	 
        	if (mCurrentFrame >= mTotalFrames)
        			mCurrentFrame = 0;
        	
        	mSRectangle.top = 0;
        	mSRectangle.bottom = mCloudHeight;
    	    mSRectangle.left = mCurrentFrame * mCloudWidth;
    	    mSRectangle.right = mSRectangle.left + mCloudWidth;
	    }
	    return true;
	}
	
	public void cleanUp() {
		mSpriteSheet = null;
	}

	@Override
	void onCollision(GameObject other, boolean isArbitrator) {

	}
}
