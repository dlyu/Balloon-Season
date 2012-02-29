package com.balloon.dl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;

/*
 * A singleton class that will determine whether a MediaPlayer class should run their method (i.e. play, stop, etc) depending on the application's preferences.
 * */
public class PreferencesManager {
 
	static private PreferencesManager _instance;
	private static Context mContext;
	public static boolean mEnableSound;
	public static boolean mVibrateOn;
	public static boolean mShowColourWheel;
	private static Vibrator mVibrator;
	static SharedPreferences prefs;
 
	private PreferencesManager(Context theContext)
	{
		
		mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
	}
 
	/**
	 * Requests the instance of the Sound Manager and creates it
	 * if it does not exist.
	 *
	 * @return Returns the single instance of the PreferencesManager
	 */
	static synchronized public PreferencesManager getInstance(Context theContext)
	{
		mContext = theContext;
	    if (_instance == null)
	      _instance = new PreferencesManager(theContext);
	    updatePreference();
	    mContext = null;
	    return _instance;
	}
	
	static void updatePreference()
	{
		//prefs = mContext.getSharedPreferences(PreferencesActivity.PREFS_NAME, 0);
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mEnableSound = getBoolean("enableSound");
		mVibrateOn = getBoolean("vibrateOn");
	}
 
	/**
	 * Plays a Sound
	 *
	 * @param index - The Index of the Sound to be played
	 * @param speed - The Speed to play not, not currently used but included for compatibility
	 */
	public static void play(MediaPlayer mp)
	{
	     if (mEnableSound)
	    	 mp.start();
	}
 
	/**
	 * Stop a Sound
	 * @param index - index of the sound to be stopped
	 */
	public static void pause(MediaPlayer mp)
	{
	     if (mEnableSound)
	    	 mp.pause();
	}
	
	public static void vibrate() {
		if (mVibrateOn && mVibrator != null)
			mVibrator.vibrate(75);
		//Log.d("Is it vibrating?", "" + mVibrateOn);
		//Log.d("Is the vibrator null?", "" + (mVibrator == null));
	}
	
	/*
	 * Returns a high score depending on the activity calling this function.
	 * @param activity: The activity that requires the high score
	 * @return: The high score that corresponds to the activity calling this function
	 * */
	public static int getHighScore(Activity activity) {
		String key = "";
		if (activity instanceof ArcadeModeActivity)
			key = "___am";
		else if (activity instanceof RushModeActivity)
			key = "___rm";
		//else if (activity instanceof HunterModeActivity)
			//key = "___hm";
		if (prefs != null)
			return getInt(key);
		else
			return 0;
	}
	
	/*
	 * Sets a high score depending on the activity calling this function.
	 * @param activity: The activity that requires a new high score to be recorded
	 * @param score: The value of the new high score
	 * */
	public static void saveHighScore(Activity activity, int score) {
		String key = "";
		if (activity instanceof ArcadeModeActivity)
			key = "___am";
		else if (activity instanceof RushModeActivity)
			key = "___rm";
		//else if (activity instanceof HunterModeActivity)
			//key = "___hm";
		if (prefs != null) 
			updateInt(key, score);
	}
	
	public static synchronized boolean getBoolean(String key) {
		if (!prefs.contains(key))
			updateBoolean(key, true);
		return prefs.getBoolean(key, false);
	}
	
	public static synchronized int getInt(String key) {
		return prefs.getInt(key, 0);
	}
	
	public static synchronized void updateBoolean(String key, boolean value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();		
	}
	
	public static synchronized void updateInt(String key, int value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.commit();		
	}
	
	public static synchronized void toggleSound(boolean mode) {
		mEnableSound = mode;
		updateBoolean("enableSound", mode);
	}
	
	public static synchronized void toggleColourWheel(boolean mode) {
		mShowColourWheel = mode;
		updateBoolean("showColourWheel", mode);
	}
	
	public static synchronized void toggleVibrate(boolean mode) {
		mVibrateOn = mode;
		updateBoolean("vibrateOn", mode);
	}
} 