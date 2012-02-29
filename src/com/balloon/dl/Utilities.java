package com.balloon.dl;

import android.graphics.Rect;
import android.widget.CheckBox;

public class Utilities {
	public static int[] signs = new int[] {1, -1};
    public static int generateRandomSign() {
    	return signs[(int)(Math.random() * 2)];
    }
    
    public static int generateRandomYCoordinate(int boundary) {
    	int rand = (int)(Math.random() * (boundary));
    	return rand;
    }

    public static int generateRandomXCoordinate(int boundary) {
    	int rand = (int)(Math.random() * (boundary));
    	return rand;
    }

    /*
     * Returns a random value between the two boundaries defined by the parameters. Returns a value of either sign.
     * */
    public static int generateRandomXSpeed(int min, int max) {
    	int rand = min + (int)(Math.random()*(max - min));
    	return generateRandomSign() * rand;
    }
    
    /*
     * Returns a random value between the two boundaries defined by the parameters. Always returns a negative velocity.
     * */
    public static int generateRandomYSpeed(int min, int max) {
    	int rand = min + (int)(Math.random()*(max - min));
    	return -1*rand;
    }
    
    public static int getSign(double val) {
    	if (val == 0)
    		return 0;
    	return (int)(val/Math.abs(val));
    }
    
	public static boolean checkOverlap(int x1, int x2, int y1, int y2, int width1, int width2, int height1, int height2) {
		Rect bal1 = new Rect();
		Rect bal2 = new Rect();
		bal1.set(x1, y1, x1 + width1, y1 + height1);
		bal2.set(x2, y2, x2 + width2, y2 + height2);
		return bal1.intersect(bal2);
	}
	
	public static boolean checkOverlap(Rect r1, Rect r2) {
		return r1.intersect(r2);
	}

	public static void resetOptionButtons(CheckBox sound, CheckBox colourWheel, CheckBox vibrate) {
		sound.setChecked(PreferencesManager.getBoolean("enableSound"));
		colourWheel.setChecked(PreferencesManager.getBoolean("showColourWheel"));
		vibrate.setChecked(PreferencesManager.getBoolean("vibrateOn"));	
	}
	
	public static void updateOptions(CheckBox sound, CheckBox colourWheel, CheckBox vibrate) {
		PreferencesManager.toggleSound(sound.isChecked());
		PreferencesManager.toggleColourWheel(colourWheel.isChecked());
		PreferencesManager.toggleVibrate(vibrate.isChecked());	
	}	

	public static void toggleOptionButtons(CheckBox sound, CheckBox colourWheel, CheckBox vibrate, boolean mode) {
		if (sound != null)
			sound.setClickable(mode);
		if (colourWheel != null)
			colourWheel.setClickable(mode);
		if (vibrate != null)
			vibrate.setClickable(mode);
	}
}
