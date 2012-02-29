package com.balloon.dl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Balloon extends GameObject{
	public static final int STATE_ACTIVE = 0;
	public static final int STATE_POPPED = 1;
	public static final int STATE_POPPED_COMBO = 2;
	
	private Bitmap mComboUp;
	
	private Bitmap mSpriteSheet;
	private int mColour;

	//private final int mTotalFrames = 8;
	public static final int mTotalFrames = 8;
	private final int mFPS = 1000/15;
	private long mFrameTimer;
	private int mCurrentFrame;
	private Rect mSRectangle;
	
	private int mSpriteHeight;
	private int mBalloonWidth;
	private int mBalloonHeight;
	private int mState;
	
	private Rect mDest;
	
	/*
	 * LIST OF COLOURS:
	 * 0 = pink
	 * 1 = red
	 * 2 = yellow
	 * 3 = green
	 * 4 = blue
	 */
	
	public Balloon () {
		super();
		mDest = new Rect();
	}
	
	public void setAttributes(Bitmap sprite, Bitmap combo, int COLOUR, int x, int y, int xSp, int ySp) {
		mSpriteSheet = sprite;
		mSpriteHeight = mSpriteSheet.getHeight()/2;
		mBalloonHeight = mSpriteHeight/2;
		mBalloonWidth = mSpriteSheet.getWidth()/mTotalFrames;
		
		mColour = COLOUR;
		mCurrentFrame = 0;
		mFrameTimer = 0;
		mSRectangle = new Rect();
		mSRectangle.top = 0;
	    mSRectangle.bottom = mSpriteHeight;
	    mSRectangle.left = 0;
	    mSRectangle.right = mBalloonWidth;
		
		updateSpeed(xSp, ySp);
		
		mX = x;
		mY = y;
		
		mState = STATE_ACTIVE;
		mComboUp = combo;
	}
	
	public int getColour() {
		return mColour;
	}
	
	public int getState() {
		return mState;
	}
	
	public boolean updatePosition(int minX, int maxX) {
		// Don't bother updating the position of popped balloons
		if (mState != STATE_ACTIVE)
			return true;
		
		mX += mXSpeed; 
		mY += mYSpeed;
		
		if (mX < minX) {
			mXSpeed = Math.abs(mXSpeed);
			mX = minX;
		}
			
		else if (mX > maxX - mBalloonWidth) {
			mXSpeed = -1 * Math.abs(mXSpeed);
			mX = maxX - mBalloonWidth;
		}
			
		if (mY + mSpriteHeight <= 0) 
			return false;
		
		return true;
	}

	public int getBalloonWidth() {
		return mBalloonWidth;
	}
	
	public int getBalloonHeight() {
		return mBalloonHeight;
	}
	
	public void updateSpeed(int mXSpeed, int mYSpeed) {
		//if (collisionChecked)
			//return;
		this.mXSpeed = mXSpeed;
		this.mYSpeed = mYSpeed;
	}
	
	@Override
	public Rect getHitBox(int offset) {
		mHitbox.set(mX - offset, mY - offset, mX + mBalloonWidth + offset, mY + mBalloonHeight + offset);
		return mHitbox;
	}
	
	public void setState(int state) {
		mState = state;
		mCurrentFrame = 0;
		mFrameTimer = 0;
	}

	@Override
	public boolean update(int interval) {
		mFrameTimer+=interval;
	    if(mFrameTimer > mFPS ) {
	        mFrameTimer = 0;
	        mCurrentFrame +=1;
	 
        	if (mCurrentFrame >= mTotalFrames) {
        		if (mState == STATE_ACTIVE)
        			mCurrentFrame = 0;
        		else
        			return false;
        	}
        	mSRectangle.top = (mState == STATE_ACTIVE ? 0 : 1) * mSpriteHeight;
        	mSRectangle.bottom = ((mState == STATE_ACTIVE ? 0 : 1) + 1) * mSpriteHeight;
            //mCurrentFrame = ++mCurrentFrame % mTotalFrames;
    	    mSRectangle.left = mCurrentFrame * mBalloonWidth;
    	    mSRectangle.right = mSRectangle.left + mBalloonWidth;
	    }
	    return true;
	}

	@Override
	public void draw(Canvas canvas) {
		mDest.set(getX(), getY(), getX() + mBalloonWidth, getY() + mSpriteHeight);
	    canvas.drawBitmap(mSpriteSheet, mSRectangle, mDest, null);
		if (mState == STATE_POPPED_COMBO) {
			double perc = (1.0 - (double)mCurrentFrame/(double)mTotalFrames);
			canvas.drawBitmap(mComboUp, mX, mY + (int)(mBalloonHeight/2 * perc), null);
		}
			
	}
	
	public void cleanUp() {
		mComboUp = null;
		mSpriteSheet = null;
	}

	/*
	 * This method takes care of the event where it collides with another GameObject. It takes two parameters:
	 * 		GameObject other: The GameObject this instance is colliding with
	 * 		boolean isArbitrator: A boolean value that determines if this instance will arbitrate the collision process so that the collision logic only involves the two GameObject instances being collided
	 * */
	void onCollision(GameObject other, boolean isArbitrator) {
		// When a balloon collides with another balloon
		if (other instanceof Balloon) {
			Balloon otherBalloon = (Balloon)other;
			
			int otherX = otherBalloon.getX();
			int otherY = otherBalloon.getY();
			
			int otherXSpeed = otherBalloon.getXSpeed();
			int otherYSpeed = otherBalloon.getYSpeed();			
			
			Rect otherHitbox = otherBalloon.getHitBox(0);
			
			otherHitbox.left += otherXSpeed;
			otherHitbox.right += otherXSpeed;
			otherHitbox.top -= otherYSpeed;
			otherHitbox.bottom -= otherYSpeed;
			
			//If true, then collision occurred
			//Make sure momentum between the balloons are conserved
			if (Rect.intersects(this.getHitBox(0), otherHitbox)) {
				double dx = Math.abs(otherX - this.getX());
				double dy = Math.abs(otherY - this.getY());
				
				//Treat it as a pure vertical collision; swap only the y speeds
				if (dx/this.getBalloonWidth() <= dy/this.getBalloonHeight()) {
					// The initial object must call the other and pass on false to prevent infinite recursive loop
					if (isArbitrator)
						otherBalloon.onCollision(this, false);
					this.updateSpeed(this.getXSpeed(), otherYSpeed);
				}
				
				//Treat it as a pure horizontal collision; swap only the x speeds
				else {
					// The initial object must call the other and pass on false to prevent infinite recursive loop
					if (isArbitrator)
						otherBalloon.onCollision(this, false);
					this.updateSpeed(otherXSpeed, this.getYSpeed());				
				}
			}	
		}
	}
}
