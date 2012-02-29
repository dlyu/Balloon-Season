package com.balloon.dl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;

public class BalloonManager extends GameObjectManager {
	public static int COLOUR_COUNT = 5;

    private int mObjectWidth;
    private int mObjectHeight;
    
    private int mMinXSpeed;
    private int mMaxXSpeed;
    private int mMinYSpeed;
    private int mMaxYSpeed;
    
    private int mOnlyColour;
    
    private Bitmap[] mBalloons;
    private Bitmap mComboUp;
    
    private Rect mBalloonArea;
        
	public BalloonManager() {
		mLoaded = false;
		mBalloonArea = new Rect();
	}
	
	public void setAttributes(int xMin, int xMax, int yMin, int yMax, int xSpeedMin, int xSpeedMax, int ySpeedMin, int ySpeedMax, int maxObj, double spawnPerc, Context context, boolean interactable) {
		super.setAttributes(xMin, xMax, yMin, yMax, xSpeedMin, xSpeedMax, ySpeedMin, ySpeedMax, maxObj, spawnPerc, context, interactable);
		restoreDefaultSpeed();
		
		for (int i = 0; i < mMaxObjectsOnScreen; i++) {
			Balloon bal = new Balloon();
			appendInactive(bal);
		}
		if (!mLoaded) {
			mLoaded = true;			
			mComboUp = BitmapFactory.decodeResource(context.getResources(), R.drawable.combo_up);
			
			mBalloons = new Bitmap[5];
			mBalloons[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.balloon_pink_sheet);
			mBalloons[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.balloon_red_sheet);
			mBalloons[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.balloon_yellow_sheet);
			mBalloons[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.balloon_green_sheet);
			mBalloons[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.balloon_blue_sheet);
			
			mObjectWidth = mBalloons[0].getWidth()/Balloon.mTotalFrames;
			mObjectHeight = mBalloons[0].getHeight()/2;
		}
	}

	@Override
	public void reset() {
		super.reset();
		restoreDefaultSpeed();
		mOnlyColour = -1;
	}

	@Override
	public int count() {
		return super.count();
	}
	
	@Override
	public void appendActive(GameObject balloon) {
		super.appendActive(balloon);
	}

	@Override
	public void removeActive(GameObject balloon) {
		super.removeActive(balloon);
	}

	@Override
	public void drawObjects(Canvas canvas) {
		Balloon curr = (Balloon)mActiveHead;
		while (curr != null) {
			curr.draw(canvas);
			curr = (Balloon)curr.getNext();
		}
	}

	public void setHead(GameObject head) {
		super.setHead(head);
	}
	public void setTail(GameObject tail) {
		super.setTail(tail);
	}
	
	@Override
	public void spawnObject() {
    	if (Math.random() * 100 < mSpawnPercentage) {
    		//The following loop makes sure that we never get overlapping balloons
    		Balloon newBal = (Balloon) createObject();
    		if (newBal != null)
    			appendActive(newBal);
    	}		
	}

	@Override
	public int updateObjects() {
		int escaped = 0;
		Balloon currBalloon = (Balloon)mActiveHead;
    	while (currBalloon != null) {
    		if (!currBalloon.updatePosition(0, mMaxX)) {
    			// Must store next balloon in a temporary reference before the current balloon gets put into the inactive queue
    			Balloon temp = (Balloon)currBalloon.getNext();
    			removeActive(currBalloon);
    			appendInactive(currBalloon);
    			currBalloon = temp;
    			escaped++;
    		}
    		else
    			currBalloon = (Balloon)currBalloon.getNext();
    	}
		return escaped;
	}

	@Override
	protected GameObject createObject() {
    	if (mObjectsOnScreen < mMaxObjectsOnScreen) {
    		// The following loop makes sure that we never get overlapping balloons
    		int x = Utilities.generateRandomXCoordinate(mMaxX - mObjectWidth);
    		int y = mMaxY;
    		mBalloonArea.set(x, y, x + mObjectWidth, y + mObjectHeight);

    		Balloon curr = (Balloon) mActiveHead;
    		while (curr != null) {
    			if (Utilities.checkOverlap(curr.getHitBox(0), mBalloonArea))
    				return null;
    			curr = (Balloon)curr.getNext();
    		}
    		Balloon newBal = (Balloon)mInactiveTail;
    		removeInactive(mInactiveTail);
    		int colour = mOnlyColour < 0 ? (int)(Math.random() * COLOUR_COUNT) : mOnlyColour;
    		
    		newBal.setAttributes(mBalloons[colour], mComboUp, colour, x, y, Utilities.generateRandomXSpeed(mMinXSpeed, mMaxXSpeed), Utilities.generateRandomYSpeed(mMinYSpeed, mMaxYSpeed));
    		return newBal;
    	}
    	return null;
	}

	@Override
	public void timeEvent(int time) {
		Balloon currBalloon = (Balloon)mActiveHead;
		while (currBalloon != null) {
    		boolean result = currBalloon.update(time);
    		if (!result) {
    			Balloon temp = (Balloon)currBalloon.getNext();
    			removeActive(currBalloon);
    			appendInactive(currBalloon);
    			currBalloon = temp;
    		}
    		else
    			currBalloon = (Balloon)currBalloon.getNext();
		}
	}

	@Override
	public GameObject checkForTouch(int x, int y, int offset) {
		if (!mInteractable)
			return null;
    	Balloon curr = (Balloon)mActiveHead;
    	while (curr != null) {
    		if (curr.getHitBox(offset).contains(x, y) && curr.getState() == Balloon.STATE_ACTIVE)
    			return curr;
    					
    		curr = (Balloon)curr.getNext();
    	}
		return null;
	}
	
	public void restoreDefaultSpeed() {
	    mMinXSpeed = mDefaultXSpeedMin;
	    mMaxXSpeed = mDefaultXSpeedMax;
	    mMinYSpeed = mDefaultYSpeedMin;
	    mMaxYSpeed = mDefaultYSpeedMax;
	}
	
	public void overrideDefaultSpeed(int xMin, int xMax, int yMin, int yMax) {
	    mMinXSpeed = xMin;
	    mMaxXSpeed = xMax;
	    mMinYSpeed = yMin;
	    mMaxYSpeed = yMax;
	}

	public void setOnlyColour(int colour) {
		mOnlyColour = colour;
	}

	@Override
	protected void restoreObjects(Bundle savedState) {
        for (int count = 0;;count++) {
        	int[] data = savedState.getIntArray("balloon"+count);
        	if (data == null)
        		break;
        	else {
        		Balloon bal = (Balloon)mInactiveTail;
        		removeInactive(mInactiveTail);
        		bal.setAttributes(mBalloons[data[0]], mComboUp, data[0], data[1], data[2], data[3], data[4]);
        		appendActive(bal);
        	}
        }
	}


	@Override
	protected void backupObjects(Bundle savedState) {
    	int count = 0;
    	Balloon currBalloon = (Balloon)mActiveHead;
    	while (currBalloon != null) {
    		int[] balloonData = new int[5];
    		balloonData[0] = currBalloon.getColour();
    		balloonData[1] = currBalloon.getX();
    		balloonData[2] = currBalloon.getY();
    		balloonData[3] = currBalloon.getXSpeed();
    		balloonData[4] = currBalloon.getYSpeed();
    		
    		savedState.putIntArray("balloon" + count, balloonData);

    		count++;
    		currBalloon = (Balloon)currBalloon.getNext();
    	}
	}
	
	@Override
	protected void cleanUp() {
		for (int i = 0; i < COLOUR_COUNT; i++)
			mBalloons[i].recycle();
		mBalloons = null;
		mComboUp.recycle();
		mComboUp = null;
		
		Balloon curr = (Balloon)mActiveHead;
		while (curr != null) {
			curr.cleanUp();
			curr = (Balloon)curr.getNext();
		}
		curr = (Balloon)mInactiveHead;
		while (curr != null) {
			curr.cleanUp();
			curr = (Balloon)curr.getNext();
		}
	}

	@Override
	void interactObjects() {
		// TODO Auto-generated method stub
    	Balloon balloon1 = (Balloon)this.getActiveHead();

    	while (balloon1 != null) {
        	Balloon balloon2 = (Balloon)balloon1.getNext();
        	// Do not check collision for popped balloons
    		while (balloon1.getState() == Balloon.STATE_ACTIVE && balloon2 != null) {
    			if (balloon2.getState() == Balloon.STATE_ACTIVE)
    				balloon1.onCollision(balloon2, true);
        		balloon2 = (Balloon)balloon2.getNext();
    		}
    		balloon1 = (Balloon)balloon1.getNext();
    	}
	}
}
