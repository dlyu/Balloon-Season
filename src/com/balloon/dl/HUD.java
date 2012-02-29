package com.balloon.dl;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

/*
 * A class that determines what appears in the game's heads-up display (HUD)
 * */
public class HUD {
	public static final int RULE_SHOW_SCORE = 0;
	public static final int RULE_SHOW_COMBO = 1;
	public static final int RULE_SHOW_HIGHSCORE = 2;
	public static final int RULE_SHOW_TIMER = 3;
	public static final int RULE_SHOW_COLOUR_WHEEL = 4; // This is just in case we never want to show the colour wheel regardless of user's input via the option buttons
	
	private final int HUD_LABEL_TEXT_COLOUR = 0xFFECECFF;
	private final int HUD_LABEL_BORDER_COLOUR = 0xFF777777;
	
	// These values are for mdpi
	private final int HUD_LABEL_TEXT_SIZE = 27;
	private final int HUD_LABEL_BORDER_SIZE = 3;
	
	private final int HUD_MINI_LABEL_TEXT_SIZE = 18;
	private final int HUD_MINI_LABEL_BORDER_SIZE = 2;
	
	private Paint mBorderPaint;
	private Paint mTextPaint;
	private Paint mBorderPaintMini;
	private Paint mTextPaintMini;
	
	private double mDensityRatio;
	
	private HashMap<Integer, Boolean> mRules;
	
   /*
    * Coordinates for UI elements
    * */
    private int mScoreCoordinateX;
    private int mScoreCoordinateY;
    
    private int mComboCoordinateX;
    private int mComboCoordinateY;
    
    private int mHighScoreCoordinateX;
    private int mHighScoreCoordinateY;

    private int mTimerCoordinateX;
    private int mTimerCoordinateY;
    
    private int mTargetColourCoordinateX;
    private int mTargetColourCoordinateY;
    
    private Drawable[] mTargetColours;
	
	public HUD(Context context, double density, int width, int height) {
		mDensityRatio = density;
		
		//Log.d("Screen density info", "Device's density = " + dm.densityDpi + ", Standard density = 120, Ratio = " + dm.densityDpi/120);
		
		mBorderPaint = new Paint();
		mBorderPaint.setColor(HUD_LABEL_BORDER_COLOUR);
	    mBorderPaint.setTextSize((float) (HUD_LABEL_TEXT_SIZE * mDensityRatio));
	    mBorderPaint.setTypeface(Typeface.DEFAULT_BOLD);
	    mBorderPaint.setStyle(Paint.Style.STROKE);
	    mBorderPaint.setStrokeWidth((float) (HUD_LABEL_BORDER_SIZE * mDensityRatio));
		
		mTextPaint = new Paint();
		mTextPaint.setColor(HUD_LABEL_TEXT_COLOUR);
		mTextPaint.setTextSize((float) (HUD_LABEL_TEXT_SIZE * mDensityRatio));
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		
		mBorderPaintMini = new Paint();
		mBorderPaintMini.setColor(HUD_LABEL_BORDER_COLOUR);
	    mBorderPaintMini.setTextSize((float) (HUD_MINI_LABEL_TEXT_SIZE * mDensityRatio));
	    mBorderPaintMini.setTypeface(Typeface.DEFAULT_BOLD);
	    mBorderPaintMini.setStyle(Paint.Style.STROKE);
	    mBorderPaintMini.setStrokeWidth((float) (HUD_MINI_LABEL_BORDER_SIZE * mDensityRatio));
		
		mTextPaintMini = new Paint();
		mTextPaintMini.setColor(HUD_LABEL_TEXT_COLOUR);
		mTextPaintMini.setTextSize((float) (HUD_MINI_LABEL_TEXT_SIZE * mDensityRatio));
		mTextPaintMini.setTypeface(Typeface.DEFAULT_BOLD);
		
		mRules = new HashMap<Integer, Boolean>();
		
        mTargetColours = new Drawable[6];
        mTargetColours[0] = context.getResources().getDrawable(R.drawable.colour_wheel_target_pink);
        mTargetColours[1] = context.getResources().getDrawable(R.drawable.colour_wheel_target_red);
        mTargetColours[2] = context.getResources().getDrawable(R.drawable.colour_wheel_target_yellow);
        mTargetColours[3] = context.getResources().getDrawable(R.drawable.colour_wheel_target_green);
        mTargetColours[4] = context.getResources().getDrawable(R.drawable.colour_wheel_target_blue);
        mTargetColours[5] = context.getResources().getDrawable(R.drawable.colour_wheel_target_null);
		
		int textHeight = (int) (-(mBorderPaint.ascent()) + mBorderPaint.descent());
		int textHeightMini =  (int) (-(mBorderPaintMini.ascent()) + mBorderPaintMini.descent());
		
        // Set coordinates for HUD elements as dynamically as possible (for different dpi)
        mScoreCoordinateX = (int) ((10) * mDensityRatio);
        mScoreCoordinateY = (int) (3 * mDensityRatio + textHeight);
        
        mComboCoordinateX = (int) (10 * mDensityRatio);
        mComboCoordinateY = (int) (mScoreCoordinateY + 3 * mDensityRatio + textHeight);
        
        mHighScoreCoordinateX = (int) (10 * mDensityRatio);
        mHighScoreCoordinateY = (int) (mComboCoordinateY + 3 * mDensityRatio + textHeightMini);

        mTimerCoordinateX = (int) (10 * mDensityRatio);
        mTimerCoordinateY = (int) (height - 5 * mDensityRatio);
        
        mTargetColourCoordinateX = (int) (width - mTargetColours[0].getIntrinsicWidth());
        mTargetColourCoordinateY = (int) (5 * mDensityRatio);
	}
	
	public boolean setRule(int rule, boolean mode) {
		switch (rule) {
			case RULE_SHOW_SCORE:
			case RULE_SHOW_COMBO:
			case RULE_SHOW_HIGHSCORE:
			case RULE_SHOW_TIMER:
			case RULE_SHOW_COLOUR_WHEEL:
				mRules.put(rule, mode);
				break;
			default:
				return false;
		}
		return true;
	}
	
	public boolean checkRule(int rule) {
		if (mRules.get(rule) == null)
			return false;
		return (boolean)mRules.get(rule);
	}
	
	public void drawHUD(Canvas c, Bundle data) {
		if (checkRule(RULE_SHOW_SCORE))
			drawScore(c, data.getInt("SCORE"));
		if (checkRule(RULE_SHOW_COMBO))
			drawCombo(c, data.getInt("COMBO"));
		if (checkRule(RULE_SHOW_HIGHSCORE))
			drawHighScore(c, data.getInt("HIGHSCORE"));
		if (checkRule(RULE_SHOW_TIMER))
			drawTimer(c, data.getInt("TIMER"));
		if (checkRule(RULE_SHOW_COLOUR_WHEEL))
			drawColourWheel(c, data.getInt("COLOUR"));
	}
	
	private void drawScore(Canvas c, int score) {
	    c.drawText("SCORE: " + score, mScoreCoordinateX, mScoreCoordinateY, mBorderPaint);
	    c.drawText("SCORE: " + score, mScoreCoordinateX, mScoreCoordinateY, mTextPaint);
	    
	    //Log.d("Drawing score!", "x = " + mScoreCoordinateX + ", y = " + mScoreCoordinateY + ", score = " + score);
	}
	
	private void drawCombo(Canvas c, int combo) {
	    c.drawText("COMBO: " + combo, mComboCoordinateX, mComboCoordinateY, mBorderPaint);
	    c.drawText("COMBO: " + combo, mComboCoordinateX, mComboCoordinateY, mTextPaint);
	    
	    //Log.d("Drawing combo!", "x = " + mComboCoordinateX + ", y = " + mComboCoordinateY + ", combo = " + combo);
	}
	
	private void drawHighScore(Canvas c, int highScore) {
	    c.drawText("BEST: " + highScore, mHighScoreCoordinateX, mHighScoreCoordinateY, mBorderPaintMini);
	    c.drawText("BEST: " + highScore, mHighScoreCoordinateX, mHighScoreCoordinateY, mTextPaintMini);		
	    
	    //Log.d("Drawing high score!", "x = " + mHighScoreCoordinateX + ", y = " + mHighScoreCoordinateY + ", high score = " + highScore);
	}
	
	private void drawColourWheel(Canvas c, int colourWheel) {
		if (PreferencesManager.getBoolean("showColourWheel")) {
	    	mTargetColours[colourWheel].setBounds(mTargetColourCoordinateX, mTargetColourCoordinateY, mTargetColourCoordinateX + mTargetColours[colourWheel].getIntrinsicWidth(), mTargetColourCoordinateY + mTargetColours[colourWheel].getIntrinsicHeight());
	    	mTargetColours[colourWheel].draw(c);
	    	//Log.d("Drawing colour wheel!", "x = " + mTargetColourCoordinateX + ", y = " + mTargetColourCoordinateY);
		}
	}
	
	private void drawTimer(Canvas c, int milliseconds) {
    	double time = milliseconds;
    	int seconds = (int)Math.ceil(time/1000.0);
    	int minutes = (int)(seconds / 60);
    	seconds -= minutes * 60;
		
    	String timeStr = "" + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    	
	    c.drawText(timeStr, mTimerCoordinateX, mTimerCoordinateY, mBorderPaint);
	    c.drawText(timeStr, mTimerCoordinateX, mTimerCoordinateY, mTextPaint);		
	    
	    //Log.d("Drawing timer!", "x = " + mTimerCoordinateX + ", y = " + mTimerCoordinateY + ", time = " + timeStr);
	}
}
