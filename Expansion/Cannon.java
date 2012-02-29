package com.balloon;

import android.graphics.Canvas;

public class Cannon extends Weapon {

	protected Cannon(int max, int cost, int chargeRate, GameCanvas gc) {
		super(max, cost, chargeRate, gc);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Weapon onActivation() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	protected void onDeactivation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onTouch() {
		// TODO Auto-generated method stub
		
	}

}
