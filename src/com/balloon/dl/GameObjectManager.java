package com.balloon.dl;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;

public abstract class GameObjectManager {
	protected GameObject mActiveHead;
	protected GameObject mActiveTail;
	protected GameObject mInactiveHead;
	protected GameObject mInactiveTail;
	protected Context mContext;
	protected double mSpawnPercentage;
	protected int mMinX;
	protected int mMaxX;
	protected int mMinY;
	protected int mMaxY;
	protected int mObjectsOnScreen;
	protected int mObjectsInactive;
	protected int mMaxObjectsOnScreen;
	
	protected int mDefaultXSpeedMin;
	protected int mDefaultXSpeedMax;
	protected int mDefaultYSpeedMin;
	protected int mDefaultYSpeedMax;
	
	protected boolean mInteractable;
	protected boolean mLoaded;
	
	protected void setAttributes(int xMin, int xMax, int yMin, int yMax, int xSpeedMin, int xSpeedMax, int ySpeedMin, int ySpeedMax, int maxObj, double spawnPerc, Context context, boolean interactable) {
		mMinX = xMin;
		mMaxX = xMax;
		mMinY = yMin;
		mMaxY = yMax;
		mDefaultXSpeedMin = xSpeedMin;
		mDefaultXSpeedMax = xSpeedMax;
		mDefaultYSpeedMin = ySpeedMin;
		mDefaultYSpeedMax = ySpeedMax;
		mMaxObjectsOnScreen = maxObj;
		mSpawnPercentage = spawnPerc;
		mObjectsOnScreen = 0;
		mObjectsInactive = 0;
		mActiveHead = null;
		mActiveTail = null;
		mContext = context;
		mInteractable = interactable;
	}
	protected abstract GameObject createObject();
	protected void reset() {
		GameObject curr = mActiveHead;
		while (curr != null) {
			GameObject temp = curr.getNext();
			removeActive(curr);
			appendInactive(curr);
			curr = temp;
		}
		mActiveHead = null;
		mActiveTail = null;
		
		//mObjectsOnScreen = 0;
	}
	protected int count() {
		return mObjectsOnScreen;
	}
	protected GameObject getActiveHead() {
		return mActiveHead;
	}
	protected GameObject getActiveTail() {
		return mActiveTail;
	}
	protected void setHead(GameObject head) {
		mActiveHead = head;
	}
	protected void setTail(GameObject tail) {
		mActiveTail = tail;
	}
	protected void removeActive(GameObject obj) {
    	GameObject next = obj.getNext();
    	GameObject prev = obj.getPrev();

    	//mTail
    	if (next == null) 
    		mActiveTail = prev;
    	else
        	next.setPrev(prev);
    	
    	//mHead
    	if (prev == null) 
    		mActiveHead = next;
    	else
    		prev.setNext(next);
    	
    	//The object should be "isolated"
    	obj.setNext(null);
    	obj.setPrev(null);
    	mObjectsOnScreen--;
    	
	}
	
	protected void removeInactive(GameObject obj) {
    	GameObject next = obj.getNext();
    	GameObject prev = obj.getPrev();
    	
    	//mTail
    	if (next == null) 
    		mInactiveTail = prev;
    	else
        	next.setPrev(prev);
    	
    	//mHead
    	if (prev == null) 
    		mInactiveHead = next;
    	else
    		prev.setNext(next);
    	
    	//The object should be "isolated"
    	obj.setNext(null);
    	obj.setPrev(null);
    	mObjectsInactive--;
	}
	
	protected void appendInactive(GameObject obj) {
    	if (mInactiveHead == null) {
    		mInactiveHead = obj;
    		mInactiveTail = mInactiveHead;
    	}
    	else {
    		obj.setPrev(mInactiveTail);
    		mInactiveTail.setNext(obj);
    		mInactiveTail = obj;
    	}
    	mObjectsInactive++;
	}
	
	protected void appendActive(GameObject obj) {
    	if (mActiveHead == null) {
    		mActiveHead = obj;
    		mActiveTail = mActiveHead;
    	}
    	else {
    		obj.setPrev(mActiveTail);
    		mActiveTail.setNext(obj);
    		mActiveTail = obj;
    	}
    	mObjectsOnScreen++;
	}
	protected abstract void spawnObject();
	protected abstract int updateObjects();
	protected abstract void drawObjects(Canvas canvas);
	protected abstract void timeEvent(int time);
	protected abstract GameObject checkForTouch(int x, int y, int offset);
	protected abstract void backupObjects(Bundle savedState);
	protected abstract void restoreObjects(Bundle savedState);
	abstract void interactObjects();
	protected abstract void cleanUp();
}