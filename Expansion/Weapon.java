package com.balloon;

import android.graphics.Canvas;

public abstract class Weapon {
	protected int mMaxGauge; //The maximum amount of points that can be used for the weapon
	protected int mGauge; //The amount of points the weapon currently has
	protected int mCost; //The cost of the weapon's usage
	protected int mRechargeRate; //How much points the weapon regains per second
	protected int mTimer; //The time counter for recharging
	protected GameCanvas mGameCanvas;
	
	protected Weapon(int max, int cost, int chargeRate, GameCanvas gc) {
		this.mMaxGauge = max;
		this.mCost = cost;
		this.mRechargeRate = chargeRate;
		this.mGameCanvas = gc;
	}
	protected abstract Weapon onActivation();
	protected abstract void onTouch();
	protected abstract void onDeactivation();
	
	protected abstract void draw(Canvas canvas);
	
	protected boolean ready() {
		return mCost < mGauge;
	}
	
	protected void recharge (int millisec) {
		mTimer+=millisec;
		if (mTimer >= 1000) {
			mTimer -= 1000;
			mGauge+=mRechargeRate;
			if (mGauge > mMaxGauge)
				mGauge = mMaxGauge;
		}
	}
	
	protected void reset() {
		mTimer = 0;
		mGauge = mMaxGauge;
	}
}
