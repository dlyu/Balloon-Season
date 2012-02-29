package com.balloon.dl;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

/*
 * Note that this activity should be an interface; a bridge between the game and the player. The GameCanvas and GameThread
 * classes take care of the game elements while the ArcadeModeActivity merely relays input and output between the game and the
 * user.
 * */
public class RushModeActivity extends GameActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesManager.getInstance(this);
        
		game = (GameCanvas)findViewById(R.id.arcadecanvas);
		game.setOnTouchListener(mTouchEvent);
		thread = game.getThread();
        
        initializeGame();
        
        if (savedInstanceState == null) {
            // we were just launched: set up a new game
        	mHandler.postDelayed(mUpdateTimeTask, mInterval);
            thread.setState(GameCanvas.STATE_RUNNING);
            
        } else {
            // we are being restored: resume a previous game
            thread.restoreState(savedInstanceState);   
            thread.togglePause(true);
        }
    }
	
    /*
     * Captures the back button press to pause the game instead of killing the activity
     * */
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        return super.onKeyDown(keyCode, event);
    }
    
    private void initializeGame() {
    	Utilities.resetOptionButtons(mOptionSound, mOptionColourWheel, mOptionVibrate);
		
    	DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;

		BalloonManager b = new BalloonManager();
		b.setAttributes(0, width, 0, height, 3, 5, 2, 6, 15, 40, getBaseContext(), true);
		thread.setBalloonManager(b);

		CloudManager c = new CloudManager();
		c.setAttributes(0, width, 0, height, -1, -1, -1, -1, 4, 2, getBaseContext(), false);
		thread.setCloudManager(c);

		thread.setGameVariables(width, height, -1, 90000, GameCanvas.GAME_OVER_TIMEOUT);
		game.setKeepScreenOn(true);
		
		HUD hud = new HUD(this, dm.density, width, height);
    	hud.setRule(HUD.RULE_SHOW_SCORE, true);
    	hud.setRule(HUD.RULE_SHOW_COMBO, true);
    	hud.setRule(HUD.RULE_SHOW_HIGHSCORE, true);
    	hud.setRule(HUD.RULE_SHOW_TIMER, true);
    	hud.setRule(HUD.RULE_SHOW_COLOUR_WHEEL, true);
    	thread.setHUD(hud);
		
        // give the GameCanvas a means to update UI elements
        game.setActivityReference(this);
        thread.initializeGame();
    }
    
    /*
    protected void initializeHandlers() {
    	super.initializeHandlers();
    	mHudHandler = new Handler() {
            public void handleMessage(Message m) {
    	    	((HUDForTimedGame)mHUD).updateScore(m.getData().getInt("score"));
    	    	((HUDForTimedGame)mHUD).updateCombo(m.getData().getInt("combo"));
    	    	((HUDForTimedGame)mHUD).updateHighScore(m.getData().getInt("highscore"));
    	    	((HUDForTimedGame)mHUD).updateTimer(m.getData().getInt("timer"));
            }
    	};
    }*/
}
