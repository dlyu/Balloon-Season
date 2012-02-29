package com.balloon.dl;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameCanvas extends SurfaceView implements SurfaceHolder.Callback {
    /*
     * Objects necessary for the game to run
     * */
    private GameThread mThread;
	private SurfaceHolder mHolder;
    private GameActivity mActivity;
	
    private final int MAX_COMBO = 30;
    private final int CLEANUP_TIME = 100;
    
    /*
     * Game over cause flags
     * */
    public static final int GAME_OVER_TIMEOUT = 0; //Game is over when the time runs out
    public static final int GAME_OVER_BALLOON_ESCAPE = 1; //Game is over when a certain number of balloons escape the screen
    
    /*
     * State-tracking constants/flags
     */
    
    /*
     * The state of the game's run-time.
     * */
    public static final int STATE_RUNNING = 0;

    /*
     * The state between the actual game run-time and the game's conclusion. It is during this state where the user can
     * pop remaining balloons before they are presented with the option to retry or to return to the main menu.
     * */
    public static final int STATE_GAME_END_1 = 2;
    
    /*
     * The state where the screen does not have any balloons and the screen is just cleaning up.
     * */
    public static final int STATE_GAME_END_2 = 3;
    
    /*
     * The state of the game's conclusion. Here the user is presented with the final score as well as the option to retry
     * or to return to the main menu.
     * */
    public static final int STATE_GAME_END_3 = 4;

    /*
     * The 3-second countdown phase before the game begins
     * */
    public static final int STATE_COUNTDOWN = 5;
    
    public static final int COLOUR_COUNT = 5;
    
    /*
     * Constant values
     * */
    private int mTouchErrorOffset = 15;
    private final int mComboDelayMax = 1000;
    private final int mComboDelayMin = 500;
    
    /*
     * Dynamic game variables that can be set on an activity basis
     */
    private int mGameWidth;
    private int mGameHeight;
    // private int mMaxBalloonEscaped = 2; // Will be used in future updates
    private int mInitialTime = 60000;

    /*
     * Essential gameplay variables
     * */
    public int mScore;
    public int mLastColour;
    public int mCombo;
    private int mHighScore;
    private boolean mHighScoreFlag;
    private int mCleanupTime;
    private boolean mDoneCleanup;

    public int mCurrentTime;
    public BalloonManager mBalloonList;
    public CloudManager mCloudList;
    public int mBalloonsEscaped;
    public boolean mTimerOn;
    
    /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
    private int mMode;

    /** Indicate whether the surface has been created & is ready to draw */
    private boolean mRun = true;

    /** Handle to the surface manager object we interact with */
    private SurfaceHolder mSurfaceHolder;
    
   /*
    * Coordinates for UI elements
    * 
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
*/
    /*
     * Objects for drawable UI elements
     * */
    //private Bitmap mNumberSheet;
    //private Rect mNumberFrame;
    //private Rect mNumberBounds;
    //private int mNumberWidth;
    //private int mNumberHeight;
    private Rect mGameBorders;
    
    //private Drawable[] mTargetColours;
    //private Drawable mScoreLabel;
    //private Drawable mComboLabel;
    //private Drawable mHighScoreLabel;
    //private Drawable mColon;
    public Bitmap mBackgroundImage;

    public Context mContext;

    private Event[] mEventList;
    private int mEventIndex;
    private Event mCurrentEvent;
    private int mEventCount;
    private final int mMaxEvents = 3;
    
    private final int mEventPercentage = 5;
    private final int mInitialEventDelay = 3000;
    private int mEventDelay;
    
    /*
     * Combo delay variables
     * */
    /*
     * Basically the time delay gauge will change colours as the combo increases. There will be four stages to the colour change. First stage starts off with only the blue colour in the RGB values
     * and the green value will steadily increase to 255. Then in the second stage, the blue value will go down to 0. Third the red will go up to 255, and fourth the green will go down to 0.
     * */ 
    private int mComboDelayCap;
    private int mComboDelay;

    private int mComboScrewUps;
    private final double mComboScrewUpsWeight = 0.5; 
    
    public boolean mPaused;
    
    // A generic Bundle used for all UI requests for the Activity
    private Bundle mMessageBundle;
    
    // A Bundle used to send data to the HUD object
    private Bundle mHUDBundle;
    
    private HUD mHUD;
    
	class GameThread extends Thread {	    	    
	    public GameThread(SurfaceHolder surfaceHolder, Context context) {
	    	//mScoreLabel = context.getResources().getDrawable(R.drawable.score_label);
	    	//mComboLabel = context.getResources().getDrawable(R.drawable.combo_label);
	    	//mHighScoreLabel = context.getResources().getDrawable(R.drawable.best_label);
	    	//mColon = context.getResources().getDrawable(R.drawable.font_colon);
	    		
	        // get handles to some important objects
	        mSurfaceHolder = surfaceHolder;
	        mContext = context;

	        Resources res = context.getResources();
	        mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.sky_background);
	        
	        //mNumberSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.numbers);
	        
	        //mNumberFrame = new Rect();
	        //mNumberBounds = new Rect();
	        //mNumberWidth = mNumberSheet.getWidth();
	        //mNumberHeight = mNumberSheet.getHeight()/10;

	        mMessageBundle = new Bundle();
	        mHUDBundle = new Bundle();
	    }
	    
	    public void setHUD(HUD hud) {
	    	mHUD = hud;
	    }

	    /**
	     * Set various variables for different game modes.
	     * @param width: The width of the game screen.
	     * @param height: The height of the game screen.
	     * @param maxBalloonEscape: The maximum number of balloons that can escape.
	     * @param initialTime: The initial value of the timer when the game starts.
	     * @param gameOverFactor: The condition in which the game ends (i.e. out of time, X balloons escaped, etc).
	     */
	    public void setGameVariables(int width, int height, int maxBalloonEscape, int initialTime, int gameOverFactor) {
	        mGameWidth = width;
	        mGameHeight = height;
	        //mMaxBalloonEscaped = maxBalloonEscape;
	        mInitialTime = initialTime;
	        
	        mGameBorders = new Rect(0, 0, mGameWidth, mGameHeight);
	        
	        // Set coordinates for HUD elements as dynamically as possible (for different dpi)
	        /*
            mScoreCoordinateX = 10;
            mScoreCoordinateY = 5;
            
            mComboCoordinateX = 10;
            mComboCoordinateY = mScoreCoordinateY + mScoreLabel.getIntrinsicHeight() + 5;
            
            mHighScoreCoordinateX = 10;
            mHighScoreCoordinateY = mComboCoordinateY + mHighScoreLabel.getIntrinsicHeight() + 5;

            mTimerCoordinateX = 10;
            mTimerCoordinateY = mGameHeight - mNumberHeight;
            
            mTargetColourCoordinateX = mGameWidth - mTargetColours[0].getIntrinsicWidth();
            mTargetColourCoordinateY = 5;*/
	    }
	    
	    @Override
	    public void run() {
	    	Canvas c = null;
	    	c = mSurfaceHolder.lockCanvas(null);
            doDraw(c);
            mSurfaceHolder.unlockCanvasAndPost(c);
            
	        while (mRun) {
	            try {
	            	if (!mPaused & mMode != STATE_GAME_END_3) {
                    	c = mSurfaceHolder.lockCanvas();
                    	if (mMode == STATE_RUNNING) {
                    		mBalloonList.spawnObject();
                    		mCloudList.spawnObject();
                    	}
                    	updatePhysics();
	                    doDraw(c);
	                    mSurfaceHolder.unlockCanvasAndPost(c);
                    }

	            }
            	catch (Exception e) {}
	        }
	    }

	    /**
	     * Starts a random event to alter the state of gameplay. 
	     */
	    private void startEvent() {
    		double roll = Math.random()*100;
    		double percentage = 0;
    		for (int i = 0; i < mEventCount; i++) {
    			if (mEventList[i] != null) {
    				percentage+=mEventList[i].getPercentage();
    				if (roll < percentage) {
    					mCurrentEvent = mEventList[i];
    					mEventList[i].startEvent();
    					mEventIndex = i;
    					break;
    				}
    			}
    		}
	    }

	    /**
	     * Performs time-dependent events such as updating frames, updating timers, etc after one millisecond has passed.
	     * */
	    public void secondsEvent() {
	    	secondsEvent(1);
	    }
	    
	    /**
	     * Performs time-dependent events such as updating frames, updating timers, etc.
	     * @param millisec: The time interval that has elapsed.
	     * */
	    synchronized public void secondsEvent(int millisec) {
	    	// Start events only when the game is running
	    	//if (mMode == STATE_RUNNING) {
	    	if (mMode == STATE_RUNNING) {
        		if (mCurrentEvent == null && mEventDelay <= 0) {
        			if (Math.random()*100 < mEventPercentage)
        				startEvent();
        		}
        			
	    	}
	    	
	    	if (mMode == STATE_GAME_END_2) {
	    		mCleanupTime+=millisec;
	    		if (mCleanupTime >= CLEANUP_TIME && mComboDelay <= 0)
	    			mDoneCleanup = true;
	    	}
	    	
	    	// Decrease the event timer
	    	if (mEventDelay > 0)
	    		mEventDelay-=millisec;
	    	
	    	// Decrease the game timer
	    	if (mCurrentTime > 0 && mTimerOn)
	    		mCurrentTime-=millisec;
	    	
	    	// Decrease the combo delay
	    	if (mComboDelay > 0)
	    		mComboDelay-=millisec;
	    	else if (mCombo > 1) {
	    		comboScrewUp();
	    	}

	    	mBalloonList.timeEvent(millisec);
	    	mCloudList.timeEvent(millisec);
    		if (mCurrentEvent != null) {
        		if (!mCurrentEvent.update(millisec)) {
        			mCurrentEvent = null;
        			mEventDelay = mInitialEventDelay;
        			mEventIndex = -1;
        		}		
    		}
    		
    		/*
	    	Message msg = mActivity.mHudHandler.obtainMessage();
	    	mMessageBundle.putInt("score", mScore);
	    	mMessageBundle.putInt("combo", mCombo);
	    	mMessageBundle.putInt("highscore", mHighScore);
	    	mMessageBundle.putInt("timer", mCurrentTime);
            msg.setData(mMessageBundle);
	    	mActivity.mHudHandler.sendMessage(msg);
	    	
    		((HUDForTimedGame)mActivity.mHUD).updateScore(mScore);
    		((HUDForTimedGame)mActivity.mHUD).updateCombo(mCombo);
    		((HUDForTimedGame)mActivity.mHUD).updateHighScore(mHighScore);
    		((HUDForTimedGame)mActivity.mHUD).updateTimer(mCurrentTime);
    		*/
	    }
	    
	    /**
	     * Set the balloon manager object that manages the balloon sprites.
	     */
	    public void setBalloonManager(BalloonManager bManager) {
	    	mBalloonList = bManager;
	    }
	    
	    /**
	     * Set the cloud manager object that manages the cloud sprites.
	     */
	    public void setCloudManager(CloudManager cManager) {
	    	mCloudList = cManager;
	    }
	    
	    /**
	     * Add an event object such as sun, snow, and fog.
	     */
	    public void addEvent(Event e) {
	    	if (mEventList == null) {
		        mEventList = new Event[mMaxEvents];
		        mEventCount = 0;	    		
	    	}

	    	if (mEventCount == mMaxEvents)
	    		return;
	    	mEventList[mEventCount] = e;
	    	mEventCount++;
	    }
	    
	    /**
	     * Reset all the game settings so that it reflects the start of the game.
	     * Game settings include score, timer, event status, etc.
	     * */
	    public void initializeGame() {
	    	mTimerOn = true; // Timer is active upon start of the game
			mScore = 0; // Score is set to zero
			mLastColour = -1; // The last colour is set to none
			mCombo = 1; // Combo is set to 1
			mCurrentTime = mInitialTime; // The value of the current timer is set to the value of the initial timer
			mBalloonsEscaped = 0; // The number of balloons that escaped is 0
	    	mBalloonList.reset(); // Resets everything in the balloon manager object
	    	
	    	// Don't reset the clouds because I think it would look better if the clouds magically "did not disappear"
	    	
	        mCurrentEvent = null; // There are no events running at the start of the game
	        mEventDelay = mInitialEventDelay; // There should be a delay between the start of the game and the first event
	        mEventIndex = -1; // The event index is set to a null value because no events are running right now
	        
	        // Reset every events that exist in the event list
	        if (mEventList != null) {
		        for (int i = 0; i < mEventList.length; i++) {
		        	if (mEventList[i] != null)
		        		mEventList[i].reset(); 
	        	}	        	
	        }

	        mComboDelayCap = mComboDelayMax;
	        mComboDelay = 0;
	        mHighScore = PreferencesManager.getHighScore(mActivity);
	        mHighScoreFlag = false;
	        
	        mCleanupTime = 0;
	        mDoneCleanup = false;
	        
	        mComboScrewUps = 0;
	        
	        mPaused = false;
	        /*
	    	((HUDForTimedGame)mActivity.getHUD()).updateScore(mScore);
	    	((HUDForTimedGame)mActivity.getHUD()).updateCombo(mCombo);
	    	((HUDForTimedGame)mActivity.getHUD()).updateHighScore(mHighScore);
	    	((HUDForTimedGame)mActivity.getHUD()).updateTimer(mCurrentTime);
	    	*/
	    }
	    
	    /**
	     * The method that gets called when the game is over. It is mainly responsible for saving high scores.
	     * */
	    public boolean checkHighScore() {
	    	if (mHighScoreFlag) {
	    		PreferencesManager.saveHighScore(mActivity, mScore);
	    		return true;
	    	}
	    	return false;	
	    }

	    /**
	     * Dump game state to the provided Bundle. Typically called when the
	     * Activity is being suspended.
	     *
	     * @return Bundle with this view's state
	     */
	    public Bundle saveState(Bundle map) {
	        synchronized (mSurfaceHolder) {
	            if (map != null) {
	            	String prefix = getModePrefix();
	            	if (prefix == null)
	            		return map;
	            	mBalloonList.backupObjects(map);
            		mCloudList.backupObjects(map);
	            	
            		// Backup all essential game variables into the Bundle
	            	map.putInt(prefix + "score", mScore);
	            	map.putInt(prefix + "lastColour", mLastColour);
	            	map.putInt(prefix + "balloonsMissed", mBalloonsEscaped);
	            	map.putInt(prefix + "time", mCurrentTime);
	            	map.putInt(prefix + "status", mMode);
	            	map.putInt(prefix + "combo", mCombo);
	            	map.putInt(prefix + "eventIndex", mEventIndex);
	            	map.putInt(prefix + "eventDelay", mEventDelay);
	            	map.putBoolean(prefix + "timerSwitch", mTimerOn);
	            	
	    	        for (int i = 0; i < mEventCount; i++)
	            		mEventList[i].backupEventData(prefix + mEventList[i].mKey, map);
	            }
	        }
	        return map;
	    }
	    

	    /**
	     * Restores game state from the indicated Bundle. Typically called when
	     * the Activity is being restored after having been previously
	     * destroyed.
	     *
	     * @param savedState Bundle containing the game state
	     */
	    public synchronized void restoreState(Bundle savedState) {
	        synchronized (mSurfaceHolder) {
	            
            	String prefix = getModePrefix();
            	if (prefix == null)
            		return;
	            mBalloonList.restoreObjects(savedState);
	            mCloudList.restoreObjects(savedState);
	            
        		// Restore all essential game variables from the Bundle
	            setState(savedState.getInt(prefix + "status"));
	            mScore = savedState.getInt(prefix + "score");
	            mLastColour = savedState.getInt(prefix + "lastColour");
	            mBalloonsEscaped = savedState.getInt(prefix + "balloonsMissed");
	            mCurrentTime = savedState.getInt(prefix + "time");
	            mCombo = savedState.getInt(prefix + "combo");
	            mEventIndex = savedState.getInt(prefix + "eventIndex");
	            mEventDelay = savedState.getInt(prefix + "eventDelay");
	            mTimerOn = savedState.getBoolean(prefix + "timerSwitch");
	            
    	        for (int i = 0; i < mEventCount; i++)
            		mEventList[i].restoreEventData(prefix + mEventList[i].mKey, savedState);
    	        
	            if (mEventIndex > -1)
	            	mCurrentEvent = mEventList[mEventIndex];
	        }
	    }
	    
	    public String getModePrefix(Activity activity) {
        	if (activity instanceof ArcadeModeActivity)
        		return "am_";
        	else if (activity instanceof RushModeActivity)
        		return "rm_";
        	else
        		return null;	    	
	    }
	    
	    public String getModePrefix() {
	    	return getModePrefix(mActivity);
	    }

	    /**
	     * Used to signal the thread whether it should be running or not.
	     * Passing true allows the thread to run; passing false will shut it
	     * down if it's already running. Calling start() after this was most
	     * recently called with false will result in an immediate shutdown.
	     *
	     * @param b true to run, false to shut down
	     */
	    public void setRunning(boolean b) {
	        mRun = b;
	    }

	    public int getCurrentState() {
	    	return mMode;
	    }

	    /**
	     * Sets the game mode. That is, whether we are running, paused, in the
	     * failure state, in the victory state, etc.
	     *
	     * @param mode one of the STATE_* constants
	     * @param message string to add to screen or null
	     */
	    public void setState(int mode) {
	        /*
	         * This method optionally can cause a text message to be displayed
	         * to the user when the mode changes. Since the View that actually
	         * renders that text is part of the main View hierarchy and not
	         * owned by this thread, we can't touch the state of that View.
	         * Instead we use a Message + Handler to relay commands to the main
	         * thread, which updates the user-text View.
	         */
	        synchronized (mSurfaceHolder) {
	            mMode = mode;

	            if (mMode == STATE_RUNNING)
	            	setRunningUI();
            	else if (mMode == STATE_GAME_END_1)
	            	setTimesUpUI();
            	else if (mMode == STATE_GAME_END_2)
	            	mActivity.clearTimesUp();
	            else if (mMode == STATE_GAME_END_3)
            		setGameOverUI();
	        }
	    }

	    /* Callback invoked when the surface dimensions change. */
	    public void setSurfaceSize(int width, int height) {
	    }

	    private void setRunningUI() {
	    	Message msg = mActivity.mPauseUIHandler.obtainMessage();

	    	mMessageBundle.putInt("viz", View.INVISIBLE);
            msg.setData(mMessageBundle);
            mActivity.mPauseUIHandler.sendMessage(msg);
            
            msg = mActivity.mGameOverUIHandler.obtainMessage();
            msg.setData(mMessageBundle);
            mActivity.mGameOverUIHandler.sendMessage(msg);
            
        	msg = mActivity.mTimesUpHandler.obtainMessage();
            msg.setData(mMessageBundle);
            mActivity.mTimesUpHandler.sendMessage(msg);
	    }
	    
	    private void setTimesUpUI() {
	    	Message msg = mActivity.mTimesUpHandler.obtainMessage();
	    	
	    	mMessageBundle.putInt("viz", View.VISIBLE);
            msg.setData(mMessageBundle);

            mActivity.mTimesUpHandler.sendMessage(msg);	    	
	    }

	    /**
	     * Pauses the physics update & animation.
	     */
	    public void togglePause(boolean state) {
	        synchronized (mSurfaceHolder) {
	        	if (mMode == STATE_GAME_END_3)
	        		return;
	        	mPaused = state;
	        	setPauseUI(state);
	        }
	    }

	    
	    private void setPauseUI(boolean state) {
	    	Message msg = mActivity.mPauseUIHandler.obtainMessage();
	    	mMessageBundle.putInt("viz", state ? View.VISIBLE : View.INVISIBLE);
            msg.setData(mMessageBundle);

            mActivity.mPauseUIHandler.sendMessage(msg);
	    }
	    
	    private void setGameOverUI() {
	    	Message msg = mActivity.mGameOverUIHandler.obtainMessage();
	    	
	    	mMessageBundle.putInt("viz", View.VISIBLE);
            msg.setData(mMessageBundle);

            mActivity.mGameOverUIHandler.sendMessage(msg);
	    }

	    /**
	     * Draws the ship, fuel/speed bars, and background to the provided
	     * Canvas.
	     */
	    synchronized private void doDraw(Canvas canvas) {
	        // Draw the background image. Operations on the Canvas accumulate
	        // so this is like clearing the screen.
	        // The background image always has the lowest z-index
	    	//canvas.drawBitmap(mBackgroundImage, 0, 0, null);
	    	canvas.drawBitmap(mBackgroundImage, null, mGameBorders, null);

	    	//The event foreground needs to have a higher z-index than the overall background but lower than the game elements
    		if (mCurrentEvent != null) {
    			mCurrentEvent.drawBackground(canvas);
    		}
	        
	    	mCloudList.drawObjects(canvas);
	    	mBalloonList.drawObjects(canvas);

	    	//The event foreground needs to have a higher z-index than the game elements
    		if (mCurrentEvent != null) {
    			mCurrentEvent.drawForeground(canvas);
    		}
    		
    		/*
    		//It is better to get the preference directly because it is always up to date
    		if (PreferencesManager.getBoolean("showColourWheel")) {
    			int index = mLastColour == -1 ? 5 : mLastColour;
    	    	mTargetColours[index].setBounds(mTargetColourCoordinateX, mTargetColourCoordinateY, mTargetColourCoordinateX + mTargetColours[index].getIntrinsicWidth(), mTargetColourCoordinateY + mTargetColours[index].getIntrinsicHeight());
    	    	mTargetColours[index].draw(canvas);    			
    		}*/
	    	
			//Labels should always have a higher z-index than everything else

    		/*
	    	Message msg = mActivity.mHudHandler.obtainMessage();
	    	mMessageBundle.putInt("score", mScore);
	    	mMessageBundle.putInt("combo", mCombo);
	    	mMessageBundle.putInt("highscore", mHighScore);
	    	mMessageBundle.putInt("timer", mCurrentTime);
            msg.setData(mMessageBundle);
	    	mActivity.mHudHandler.sendMessage(msg);
	    	*/
    		/*
	    	drawScore(canvas);
    		drawHighScore(canvas);
    		drawCombo(canvas);
	    	drawTime(canvas);
	    	*/
    		
	    	mHUDBundle.putInt("SCORE", mScore);
	    	mHUDBundle.putInt("COMBO", mCombo);
	    	mHUDBundle.putInt("HIGHSCORE", mHighScore);
	    	mHUDBundle.putInt("TIMER", mCurrentTime);
	    	mHUDBundle.putInt("COLOUR", mLastColour == -1 ? 5 : mLastColour);
	    	mHUD.drawHUD(canvas, mHUDBundle);
	    }
	    
	    /**
	     * This method takes in an integer number and draws it in sprites.
	     * @param value: The integer value of the number to draw. Only positive integers are supported.
	     * @param x: The X coordinate in which to draw the number.
	     * @param y: The Y coordinate in which to draw the number.
	     * @param canvas: The Canvas object in which to draw the number.
	     * */
	    /*
	    private void numberToSprite(int value, int x, int y, Canvas canvas) {
	    	// Get the number of digits to draw.
	    	int valueCopy = value;
	    	int valueLog10;
	    	if (valueCopy == 0)
	    		valueLog10 = 1;
	    	else
	    		valueLog10 = (int)(Math.floor(Math.log10(value) + 1));
	    	
	    	// Draw each digit.
	    	for (int i = valueLog10; i > 0; i--) {
	    		int digitValue = (int)(valueCopy/Math.pow(10, i - 1));
	    		
	    		mNumberFrame.set(0, digitValue * mNumberHeight, mNumberWidth, (digitValue + 1) * mNumberHeight);
	    		mNumberBounds.set(x, y, x + mNumberWidth, y + mNumberHeight);
	    	    canvas.drawBitmap(mNumberSheet, mNumberFrame, mNumberBounds, null);
	    	    x+=mNumberWidth;
	    	    valueCopy %= (int)(Math.pow(10, i - 1));
	    	}
	    }
*/
	    /**
	     * This method draws the value of the timer in sprites.
	     * @param canvas: The Canvas in which the timer should be drawn.
	     * *//*
	    private void drawTime(Canvas canvas) {
	    	double time = mCurrentTime;
	    	int seconds = (int)Math.ceil(time/1000.0);
	    	int minutes = (int)(seconds / 60);
	    	seconds -= minutes * 60;
	    	
	    	
	    	
	    	// Draw the number of minutes
	    	int x = mTimerCoordinateX;
	    	numberToSprite(minutes, x, mTimerCoordinateY, canvas);
	    	x+=mNumberWidth;
	    	
	    	mColon.setBounds(x, mTimerCoordinateY, x + mColon.getIntrinsicWidth(), mTimerCoordinateY + mColon.getIntrinsicHeight());
	    	mColon.draw(canvas);
	    	x+=mColon.getIntrinsicWidth();
	    	
	    	// Draw the number of seconds
	    	
	    	// Draw a leading zero for seconds less than 10, i.e. have only one digit.
	    	if (seconds < 10) {
	    		numberToSprite(0, x, mTimerCoordinateY, canvas);
	    		x+=mNumberWidth;
	    	}
	    	// Draw the number of seconds.
	    	numberToSprite(seconds, x, mTimerCoordinateY, canvas);
	    	
	    }*/

	    /**
	     * This method draws the score label as well as the score.
	     * @param canvas: The Canvas in which the score should be drawn.
	     * */
	    /*
	    private void drawScore(Canvas canvas) {
	    	mScoreLabel.setBounds(mScoreCoordinateX, mScoreCoordinateY, mScoreCoordinateX + mScoreLabel.getIntrinsicWidth(), mScoreCoordinateY + mScoreLabel.getIntrinsicHeight());
	    	mScoreLabel.draw(canvas);
	    	
	    	int x = mScoreCoordinateX + mScoreLabel.getIntrinsicWidth() + 5;
	    	mColon.setBounds(x, mScoreCoordinateY, x + mColon.getIntrinsicWidth(), mScoreCoordinateY + mColon.getIntrinsicHeight());
	    	mColon.draw(canvas);
	    	x+=(mColon.getIntrinsicWidth() + 5);
	    	
	    	numberToSprite(mScore, x, mScoreCoordinateY, canvas);
	    }*/

	    /**
	     * This method draws the combo label as well as the combo.
	     * @param canvas: The Canvas in which the combo should be drawn.
	     * */
	    /*
	    private void drawCombo(Canvas canvas) {
	    	mComboLabel.setBounds(mComboCoordinateX, mComboCoordinateY, mComboCoordinateX + mComboLabel.getIntrinsicWidth(), mComboCoordinateY + mComboLabel.getIntrinsicHeight());
	    	mComboLabel.draw(canvas);
	    	
	    	int x = mComboCoordinateX + mComboLabel.getIntrinsicWidth() + 5;
	    	mColon.setBounds(x, mComboCoordinateY, x + mColon.getIntrinsicWidth(), mComboCoordinateY + mColon.getIntrinsicHeight());
	    	mColon.draw(canvas);
	    	x+=(mColon.getIntrinsicWidth() + 5);
	    	
	    	numberToSprite(mCombo, x, mComboCoordinateY, canvas);
	    }*/

	    /**
	     * This method draws the high score.
	     * @param canvas: The Canvas in which the high score should be drawn.
	     * */
	    /*
	    private void drawHighScore(Canvas canvas) {
	    	mHighScoreLabel.setBounds(mHighScoreCoordinateX, mHighScoreCoordinateY, mHighScoreCoordinateX + mHighScoreLabel.getIntrinsicWidth(), mHighScoreCoordinateY + mHighScoreLabel.getIntrinsicHeight());
	    	mHighScoreLabel.draw(canvas);
	    	
	    	int x = mHighScoreCoordinateX + mHighScoreLabel.getIntrinsicWidth() + 5;
	    	mColon.setBounds(x, mHighScoreCoordinateY, x + mColon.getIntrinsicWidth(), mHighScoreCoordinateY + mColon.getIntrinsicHeight());
	    	mColon.draw(canvas);
	    	x+=(mColon.getIntrinsicWidth() + 5);
	    	
	    	numberToSprite(mHighScore, x, mHighScoreCoordinateY, canvas);
	    }
	    */
	    
	    /**
	     * This method updates all the physics in the game. Updates all collisions and positions.
	     * */
	    private void updatePhysics() {
	    	mBalloonList.interactObjects();
	    	updateAllPositions();
	    }
	    
	    /**
	     * Updates the positions of all balloons and clouds.
	     * */
	    private void updateAllPositions() {    
	    	int escaped = mBalloonList.updateObjects();
	    	
    		if (escaped > 0) {
    			mBalloonsEscaped+=escaped;
    		}
    		mCloudList.updateObjects();
	    }


	    /**
	     * This method checks if the user has popped any balloons. Called whenever the user touches the screen.
	     * @param x: The X coordinate of the point the user has touched.
	     * @param y: The Y coordinate of the point the user has touched.
	     * */
	    public void checkForTouch(int x, int y) {
	        synchronized (mSurfaceHolder) {
	        	if ((mMode == STATE_RUNNING || mMode == STATE_GAME_END_1) && !mPaused) {
	        		// For now only balloons are interactable
		        	Balloon currBalloon = (Balloon)mBalloonList.checkForTouch(x, y, mTouchErrorOffset);
		        	
		        	// currBalloon is the balloon object that was touched
	        		if (currBalloon != null) {
	        			mActivity.playPop();
	        			PreferencesManager.vibrate();
	        			// A combo is made
	        			if (checkForCombo(currBalloon.getColour())) {
	        				mLastColour = currBalloon.getColour();
	        				if (mCombo < MAX_COMBO)
	        					mCombo++;
		        			if (mComboDelayCap > mComboDelayMin)
		        				mComboDelayCap-=2;
		        			mComboDelay = mComboDelayCap;
		        			currBalloon.setState(Balloon.STATE_POPPED_COMBO);
	        			}
	        			// A combo is broken
	        			else {
	        				mLastColour = currBalloon.getColour();
	        				comboScrewUp();
	        				currBalloon.setState(Balloon.STATE_POPPED);
	        			}

	        			// Score increases
	        			mScore+=mCombo;
	        			if (mScore > mHighScore) {
	        				mHighScoreFlag = true;
	        				mHighScore = mScore;
	        			}
	        			
	        			return;
	        		}
	        	}
	    	}
	    }
	    
	    /**
	     * This method checks if the player has scored a combo based on the balloon he/she popped.
	     * @param colour: The colour of the balloons that was popped.
	     * */
	    public boolean checkForCombo(int colour) {
	    	/*
	    	 * LIST OF COLOURS:
	    	 * 0 = red
	    	 * 1 = blue
	    	 * 2 = yellow
	    	 * 3 = green
	    	 * 4 = pink
	    	 */
	    	if (colour == mLastColour || mLastColour == -1)
	    		return true;
	    	int newColour1 = mLastColour + 1;
	    	newColour1 %= COLOUR_COUNT;
	    	int newColour2 = mLastColour == 0 ? COLOUR_COUNT - 1 : mLastColour - 1;
	    	
	    	if (newColour1 == colour || newColour2 == colour)
	    		return true;
	    	return false;
	    }
	    
	    /**
	     * This method checks if there are any active balloons on screen.
	     * @return: True if there are no balloons on screen, false otherwise.
	     * */
	    public boolean noBalloonsOnScreen() {
	    	return mBalloonList.count() == 0;
	    }
	    
	    private void comboScrewUp() {
	    	// Combo decreases for the number of times the player screwed up on their combo
	    	mComboScrewUps++;
			mCombo-=(int)(mComboScrewUps * mComboScrewUpsWeight);
			if (mCombo < 1)
				mCombo = 1;
			mComboDelayCap = mComboDelayMax;
			mComboDelay = mComboDelayCap;	
	    }
	    
	    // This method cleans up Bitmaps when the game is exiting so that I don't get the "bitmap size exceeds VM budget"
	    public void cleanUp() {
	    	//mNumberSheet.recycle();
	        mBackgroundImage.recycle();
	        //mNumberSheet = null;
	        mBackgroundImage = null;
	        
	        mBalloonList.cleanUp();
	        mCloudList.cleanUp();
	        mContext = null;
	        mActivity = null;
	        
	        //mTargetColours = null;
	        //mScoreLabel = null;
	        //mComboLabel = null;
	        //mHighScoreLabel = null;
	        //mColon = null;
	    }
	}
	
	public GameCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        
        mContext = context;
        mHolder = holder;
        
        // create thread only; it's started in surfaceCreated()
        mThread = new GameThread(holder, context);

        setFocusable(true); // make sure we get key events
    }
	
    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    public GameThread getThread() {
        return mThread;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) 
        	mThread.togglePause(true);
    }
    /**
     * Callback invoked when the Surface has been created and is ready to be
     * used. Also called when the activity containing this thread is resumed.
     */
    public void surfaceCreated(SurfaceHolder holder) {
    	//If the user exits the app and comes back, the canvas is destroyed and we need to create a new thread.
        if(mThread.getState() == Thread.State.TERMINATED) {
			mThread = new GameThread(mHolder, mContext);
			mThread.setRunning(true);
			mThread.start();
        } else {
        	mThread.setRunning(true);
        	mThread.start();
        }
    }

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        mThread.setRunning(false);
        while (retry) {
            try {
                mThread.join();
                retry = false;

            } catch (InterruptedException e) {
            }
        }
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		//Log.d("Debug log", "Surface changed!");
	}

	/**
     * Installs a pointer to the ArcadeModeActivity to let this class change UI elements
     */
    public void setActivityReference(GameActivity activity) {
    	mActivity = activity;
    }
	
	public int getGameWidth() {
		return mGameWidth;
	}
	
	public int getGameHeight() {
		return mGameHeight;
	}
	
	public boolean doneCleanup() {
		return mDoneCleanup;
	}
}