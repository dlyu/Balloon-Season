package com.balloon;

import android.graphics.Canvas;

public class WeaponManager {
	static private WeaponManager sInstance;
	private Weapon[] mWeaponList;
	private int mMaxWeaponCount;
	private int mWeaponCount;
	
	private Weapon mActiveWeapon;
	
	
	public static WeaponManager getInstance() {
		if (sInstance == null)
			sInstance = new WeaponManager();
		return sInstance;
	}
	
	private WeaponManager() {
		
	}
	
	public void setAttributes(int max) {
		mWeaponList = new Weapon[max];
		mWeaponCount = 0;
	}
	
	public void setWeapon(Weapon wep) {
		if (mWeaponCount < mMaxWeaponCount)
			mWeaponList[mWeaponCount++] = wep;
	}
	
	/*
	 * Draw the weapon inventory.
	 * */
	public void draw(Canvas canvas) {
		if (mActiveWeapon != null) {
			//Draw an indicator that shows which weapon is currently selected
			//Also draw effects if necessary
		}
		for (int i = 0; i < mWeaponCount; i++)
			mWeaponList[i].draw(canvas);
	}
	
	public void recharge(int millisec) {
		for (int i = 0; i < mWeaponCount; i++)
			mWeaponList[i].recharge(millisec);		
	}
}
