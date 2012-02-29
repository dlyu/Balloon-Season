package com.balloon.dl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;

public class CloudManager extends GameObjectManager {
	public static int CLOUD_TYPES = 3;
	private Bitmap[] mClouds;
	
	public CloudManager() {
		mLoaded = false;
	}
	
	public void setAttributes(int xMin, int xMax, int yMin, int yMax, int xSpeedMin, int xSpeedMax, int ySpeedMin, int ySpeedMax, int maxObj, double spawnPerc, Context context, boolean interactable) {
		super.setAttributes(xMin, xMax, yMin, yMax, xSpeedMin, xSpeedMax, ySpeedMin, ySpeedMax, maxObj, spawnPerc, context, interactable);
		for (int i = 0; i < mMaxObjectsOnScreen; i++) {
			Cloud cloud = new Cloud();
			appendInactive(cloud);
		}
		if (!mLoaded) {
			mLoaded = true;
			mClouds = new Bitmap[3];
			mClouds[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud1_sheet);
			mClouds[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud2_sheet);
			mClouds[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud3_sheet);
		}

	}

	@Override
	public void reset() {
		super.reset();
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
	public void appendInactive(GameObject balloon) {
		super.appendInactive(balloon);
	}

	@Override
	public void removeInactive(GameObject balloon) {
		super.removeInactive(balloon);
	}

	@Override
	public void drawObjects(Canvas canvas) {
		Cloud curr = (Cloud)mActiveHead;
		while (curr != null) {
			curr.draw(canvas);
			curr = (Cloud)curr.getNext();
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
    		Cloud newBal = (Cloud) createObject();
    		if (newBal != null)
    			appendActive(newBal);
    	}		
	}

	@Override
	public int updateObjects() {
		int escaped = 0;
		GameObject currCloud = mActiveHead;
    	while (currCloud != null) {
    		if (!currCloud.updatePosition(0, mMaxX)) {
    			//Must store next cloud in a temporary reference before the current cloud gets put into the inactive queue
    			Cloud temp = (Cloud)currCloud.getNext();
    			removeActive(currCloud);
    			appendInactive(currCloud);
    			currCloud = temp;
    		}
    		else
    			currCloud = currCloud.getNext();
    	}
		return escaped;
	}

	@Override
	protected GameObject createObject() {
    	if (mObjectsOnScreen < mMaxObjectsOnScreen) {
    		int direction = Utilities.generateRandomSign();

			Cloud newCloud = (Cloud)mInactiveTail;
			removeInactive(mInactiveTail);
			int cloudId = (int)(Math.random() * 3);
    		newCloud.setAttributes(mClouds[cloudId], cloudId, mMaxX * (1 - direction)/2, Utilities.generateRandomYCoordinate(mMaxY - 100) - (1 + direction), direction * 2, 0);
    		return newCloud;
    	}
    	return null;
	}

	@Override
	public void timeEvent(int time) {
		// Animate the clouds
		Cloud currCloud = (Cloud)mActiveHead;
		while (currCloud != null) {
    		currCloud.update(time);
			currCloud = (Cloud)currCloud.getNext();
		}
	}

	@Override
	public GameObject checkForTouch(int x, int y, int offset) {
		if (!mInteractable)
			return null;
    	GameObject curr = mActiveHead;
    	while (curr != null) {
    		if (curr.getHitBox(offset).contains(x, y))
    			return curr;    			
    		curr = curr.getNext();
    	}
		return null;
	}

	@Override
	protected void restoreObjects(Bundle savedState) {
		for (int count = 0;;count++) {
        	int[] data = savedState.getIntArray("cloud"+count);
        	if (data == null)
        		break;
        	else {
        		Cloud cloud = (Cloud)mInactiveTail;
        		removeInactive(mInactiveTail);
        		cloud.setAttributes(mClouds[data[0]], data[0], data[1], data[2], data[3], data[4]);
        		appendActive(cloud);
        	}
        	count++;
        }
	}

	@Override
	protected void backupObjects(Bundle savedState) {
    	int count = 0;
    	Cloud currCloud = (Cloud)mActiveHead;
    	while (currCloud != null) {
    		int[] cloudData = new int[5];
    		cloudData[0] = currCloud.getCloudID();
    		cloudData[1] = currCloud.getX();
    		cloudData[2] = currCloud.getY();
    		cloudData[3] = currCloud.getXSpeed();
    		cloudData[4] = currCloud.getYSpeed();
    		
    		savedState.putIntArray("cloud" + count, cloudData);

    		count++;
    		currCloud = (Cloud)currCloud.getNext();
    	}
	}

	@Override
	protected void cleanUp() {
		for (int i = 0; i < CLOUD_TYPES; i++)
			mClouds[i].recycle();
		mClouds = null;
		
		Cloud curr = (Cloud)mActiveHead;
		while (curr != null) {
			curr.cleanUp();
			curr = (Cloud)curr.getNext();
		}
		curr = (Cloud)mInactiveHead;
		while (curr != null) {
			curr.cleanUp();
			curr = (Cloud)curr.getNext();
		}
	}

	@Override
	void interactObjects() {
		// TODO Auto-generated method stub
		
	}
}
