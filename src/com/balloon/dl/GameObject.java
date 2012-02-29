package com.balloon.dl;

import android.graphics.Canvas;
import android.graphics.Rect;

public abstract class GameObject {
	protected int mXSpeed;
	protected int mYSpeed;
	protected int mX;
	protected int mY;
	protected GameObject mNext;
	protected GameObject mPrev;
	protected Rect mHitbox;
	
	public GameObject() {
		mHitbox = new Rect();
	}
	public abstract boolean updatePosition(int minX, int maxX);
	public abstract Rect getHitBox(int offset);
	public abstract void draw(Canvas canvas);
	public abstract boolean update(int gameTime);
	
	protected int getX() {
		return mX;
	}
	protected int getY() {
		return mY;
	}
	protected int getXSpeed() {
		return mXSpeed;
	}
	protected int getYSpeed() {
		return mYSpeed;
	}
	protected void setX(int x) {
		mX = x;
	}
	protected void setY(int y) {
		mY = y;
	}
	protected void setXSpeed(int xSpeed) {
		mXSpeed = xSpeed;
	}
	protected void setYSpeed(int ySpeed) {
		mYSpeed = ySpeed;
	}
	protected void updateSpeed(int xSpeed, int ySpeed) {
		setXSpeed(xSpeed);
		setYSpeed(ySpeed);
	}
	protected GameObject getNext() {
		return mNext;
	}
	protected GameObject getPrev() {
		return mPrev;
	}
	protected void setNext(GameObject n) {
		mNext = n;
	}
	protected void setPrev(GameObject p) {
		mPrev = p;
	}
	abstract void onCollision(GameObject other, boolean isArbitrator);
	abstract void cleanUp();
}
