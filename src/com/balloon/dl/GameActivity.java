package com.balloon.dl;

import com.balloon.dl.GameCanvas.GameThread;
import com.balloon.dl.SoundManager;
import com.google.ads.*;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public abstract class GameActivity extends Activity {
	protected final int SOUND_EFFECT_POP = 1;
	
	protected Handler mPauseUIHandler;
	protected Handler mTimesUpHandler;
	protected Handler mGameOverUIHandler;
	protected Handler mScoreBoardHandler;
	protected Handler mPauseHandler;
	protected Handler mHudHandler;
	protected final int mInterval = 15;
	
	protected View mResumeButtonPauseMenu;
	protected View mRestartButtonPauseMenu;
	protected View mExitButtonPauseMenu;
	protected View mRestartButtonGameOverMenu;
	protected View mExitButtonGameOverMenu;
	
	protected View mPauseLabel;
	protected View mTimesUpLabel;
	protected View mPauseUIBottomElements;
	
	protected Animation mSlideInToTop;
	protected Animation mSlideInToBottom;
	protected Animation mSlideInToLeftMiddle;
	protected Animation mSlideInToRightMiddle;
	
	protected Animation mSlideOutToTop;
	protected Animation mSlideOutToBottom;
	protected Animation mSlideOutToLeft;
	protected Animation mSlideOutToRight;
	
	protected Animation mSlideInFromTop;
	
	protected Animation mExpand;
	protected Animation mShrink;
	
	protected SoundManager mSoundManager;
	protected boolean mLoaded;
	protected int mBalloonPopStreams = 5;
	protected int[] mSoundIds;
	protected int mSoundIdx = 0;
	
	protected GameCanvas game;
	protected GameThread thread;
	protected Handler mHandler;
	
	protected EndScoreLabel mScoreLabel;
	
	protected View mPauseUI;
	protected View mGameOverUI;
	
	CheckBox mOptionSound, mOptionColourWheel, mOptionVibrate;
	
	protected AdView mAdView;
	
	//protected HUD mHUD;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.arcademode);
		mLoaded = false;
		
		//mHUD = (HUD)findViewById(R.id.hud);
		mPauseUI = (View)findViewById(R.id.pause_ui);
		mGameOverUI = (View)findViewById(R.id.gameover_ui);
        initializeMedia();
        initializeButtonListeners();
        initializeHandlers();
        initializeAnimations();
        initializeAds();
        getNewAds();

        mScoreLabel = (EndScoreLabel)findViewById(R.id.gameover_scoreboard);
	}

	@Override
	public void onPause() {
		super.onPause();
		
		if (thread.getCurrentState() == GameCanvas.STATE_RUNNING)
			pauseGame();
		game.setKeepScreenOn(false);
		Utilities.updateOptions(mOptionSound, mOptionColourWheel, mOptionVibrate);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		toggleAllButtons(true);
		game.setKeepScreenOn(true);
		Utilities.resetOptionButtons(mOptionSound, mOptionColourWheel, mOptionVibrate);
	}

	@Override
	public void onStop() {
		super.onStop();
		
		if (thread.getCurrentState() == GameCanvas.STATE_RUNNING)
			pauseGame();
		game.setKeepScreenOn(false);
	}

	public void onRestart() {
		super.onRestart();
		
		Utilities.resetOptionButtons(mOptionSound, mOptionColourWheel, mOptionVibrate);
	}
	
	public void onDestroy() {
		super.onDestroy();
		/*
		
		*/
		//android.util.Log.d("Activity destroyed!", "Cleaning up!");
		mAdView.destroy();
		mSoundManager.cleanup();
		thread.cleanUp();
		game.setKeepScreenOn(false);
		game = null;
		thread = null;
	}

    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     *
     * @param outState a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        thread.saveState(outState);
    }
    
    /*
     * Initializes the game screen and the background music
     * */
	protected void initializeMedia() {
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mSoundManager = new SoundManager(this);
		mSoundManager.addSound(SOUND_EFFECT_POP, R.raw.pop);
    }
    
    /*
     * Assigns the OnClickListener to all buttons used in this activity
     * */
	protected void initializeButtonListeners() {
		mOptionSound = (CheckBox) findViewById(R.id.option_button_sound);
		mOptionColourWheel = (CheckBox) findViewById(R.id.option_button_colour_wheel);
		mOptionVibrate = (CheckBox) findViewById(R.id.option_button_vibrate);
		
		mResumeButtonPauseMenu = findViewById(R.id.resume_arcade);
		mResumeButtonPauseMenu.setOnClickListener(resumeButtonListener);
		
		mRestartButtonPauseMenu = findViewById(R.id.play_again_arcade);
		mRestartButtonPauseMenu.setOnClickListener(retryButtonListener);

		mExitButtonPauseMenu = findViewById(R.id.quit_arcade);
		mExitButtonPauseMenu.setOnClickListener(exitButtonListener);

		mRestartButtonGameOverMenu = findViewById(R.id.play_again_arcade_2);
		mRestartButtonGameOverMenu.setOnClickListener(retryButtonListenerAlt);
		
		mExitButtonGameOverMenu = findViewById(R.id.exit_arcade);
		mExitButtonGameOverMenu.setOnClickListener(exitButtonListenerAlt);		

		Utilities.resetOptionButtons(mOptionSound, mOptionColourWheel, mOptionVibrate);
	}
    
	/*
	 * Initialize all Handlers: the handler responsible for the timer update, the handler responsible for the score update,
	 * and the timer responsible for the pause UI
	 * */
	protected void initializeHandlers() {
		mHandler = new Handler();
        mPauseUIHandler = new Handler() {
            @Override
            public void handleMessage(Message m) {
            	View mStatusUI = (View)findViewById(R.id.pause_ui);
            	mStatusUI.setVisibility(m.getData().getInt("viz"));
            }
        };
        mGameOverUIHandler = new Handler() {
            @Override
            public void handleMessage(Message m) {
            	View mStatusUI = (View)findViewById(R.id.gameover_ui);
            	mStatusUI.setVisibility(m.getData().getInt("viz"));

            	mStatusUI = (View)findViewById(R.id.pause_ui);
            	mStatusUI.setVisibility(View.INVISIBLE);
            	
            	mStatusUI = (View)findViewById(R.id.times_up_label);
            	mStatusUI.setVisibility(View.INVISIBLE);
            }
        };
        mTimesUpHandler = new Handler() {
            @Override
            public void handleMessage(Message m) {
            	View mStatusUI = (View)findViewById(R.id.times_up_label);
            	mStatusUI.setVisibility(m.getData().getInt("viz"));
            }
        };
        mHandler.removeCallbacks(mUpdateTimeTask);
	}
	
	protected void initializeAnimations() {		
		mPauseLabel = findViewById(R.id.pause_label);
		mTimesUpLabel = findViewById(R.id.times_up_label);
		mPauseUIBottomElements = findViewById(R.id.pause_bottom_elements);
		
		mSlideInToTop = AnimationUtils.loadAnimation(this, R.anim.top_slide_in);
		mSlideInToBottom = AnimationUtils.loadAnimation(this, R.anim.bottom_slide_in);
		mSlideInToLeftMiddle = AnimationUtils.loadAnimation(this, R.anim.left_middle_slide_in);
		mSlideInToRightMiddle = AnimationUtils.loadAnimation(this, R.anim.right_middle_slide_in);
		
		mSlideOutToTop = AnimationUtils.loadAnimation(this, R.anim.top_slide_out);
		mSlideOutToLeft = AnimationUtils.loadAnimation(this, R.anim.left_middle_slide_out);
		mSlideOutToRight = AnimationUtils.loadAnimation(this, R.anim.right_middle_slide_out);
		
		mSlideInFromTop = AnimationUtils.loadAnimation(this, R.anim.top_slide_in_from_top);
		
		mExpand = AnimationUtils.loadAnimation(this, R.anim.expand);
		mShrink = AnimationUtils.loadAnimation(this, R.anim.shrink);
	}
	
	public void initializeAds() {
	    mAdView = new AdView(this, AdSize.BANNER, this.getString(R.string.ad_id));
	    LinearLayout layout = (LinearLayout)findViewById(R.id.ad_placeholder);

	    // Add the adView to it
	    layout.addView(mAdView);
	    
	}
	
	public void getNewAds() {
		// Initiate a generic request to load it with an ad
		AdRequest request = new AdRequest();
		
		if ((0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))) {
		    request.addTestDevice(AdRequest.TEST_EMULATOR);
		    request.addTestDevice("A4D3A1B00E32ED50C9F5BF27A97ACBE3");			
		}

		mAdView.loadAd(request);
	}
	
    /*
     * Captures the back button press to pause the game instead of killing the activity
     * */
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        //if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
    	if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) && event.getRepeatCount() == 0) {
            togglePause();
            
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
	
	/*
	 * Toggles the pause depending on the current state of the game
	 * */
		
	public void togglePause() {
		int state = thread.getCurrentState();
    	if (game.mPaused) { 
    		unpauseGame();
    	}
    	else if (state != GameCanvas.STATE_GAME_END_3) {
    		pauseGame();
    	}
        return;
    }   
    
    public void pauseGame() {
		mPauseLabel.clearAnimation();
		mResumeButtonPauseMenu.clearAnimation();
		mRestartButtonPauseMenu.clearAnimation();
		mPauseUIBottomElements.clearAnimation();
		
		mPauseLabel.startAnimation(mSlideInToTop);
		/*
		mResumeButtonPauseMenu.startAnimation(mSlideInToLeftMiddle);
		mRestartButtonPauseMenu.startAnimation(mSlideInToRightMiddle);
		*/
		View buttonGroup = findViewById(R.id.button_group);
		buttonGroup.startAnimation(mSlideInToTop);
		mPauseUIBottomElements.startAnimation(mSlideInToBottom);
		
    	mHandler.removeCallbacks(mUpdateTimeTask);
    	thread.togglePause(true);
    	toggleAllButtons(true);
    }
    
    public void unpauseGame() {
    	Utilities.updateOptions(mOptionSound, mOptionColourWheel, mOptionVibrate);
    	
		mPauseLabel.clearAnimation();
		mResumeButtonPauseMenu.clearAnimation();
		mRestartButtonPauseMenu.clearAnimation();
		mPauseUIBottomElements.clearAnimation();
		
		mSlideOutToTop.reset();
		mSlideOutToTop.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				//thread.setState(GameCanvas.STATE_RUNNING);
				//thread.setState(mOldState);
				thread.togglePause(false);
		    	mHandler.postDelayed(mUpdateTimeTask, mInterval);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
				
			}
			
		} );
		
		mPauseLabel.startAnimation(mSlideOutToTop);
		/*
		mResumeButtonPauseMenu.startAnimation(mSlideOutToLeft);
		mRestartButtonPauseMenu.startAnimation(mSlideOutToRight);
		*/
		View buttonGroup = findViewById(R.id.button_group);
		buttonGroup.startAnimation(mSlideOutToTop);
		
		mPauseUIBottomElements.startAnimation(mSlideOutToTop);
    }
    
    // Restarting from the game over UI
    public void restartGame() {
    	mGameOverUI.setVisibility(View.INVISIBLE);
		mPauseUI.setVisibility(View.INVISIBLE);
    	thread.initializeGame();
    	thread.setState(GameCanvas.STATE_RUNNING);
    	mHandler.postDelayed(mUpdateTimeTask, mInterval);
    	getNewAds();
    }
    
    public void alertTimesUp() {
    	mTimesUpLabel.clearAnimation();
    	mTimesUpLabel.startAnimation(mExpand);
    }
    
    public void clearTimesUp() {
		mShrink.reset();
		mShrink.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				mTimesUpLabel.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
				
			}
			
		} );

		mTimesUpLabel.clearAnimation();
		mTimesUpLabel.startAnimation(mShrink);
    }
    
    public void presentGameOver() {
    	mScoreLabel.drawScore(game.mScore, thread.checkHighScore());
		
		mScoreLabel.clearAnimation();
		mExitButtonGameOverMenu.clearAnimation();
		mRestartButtonGameOverMenu.clearAnimation();

		mScoreLabel.startAnimation(mSlideInFromTop);
		mExitButtonGameOverMenu.startAnimation(mSlideInToRightMiddle);
		mRestartButtonGameOverMenu.startAnimation(mSlideInToLeftMiddle);
		
		toggleAllButtons(true);
    }
    
    protected void playPop() {
    	if (PreferencesManager.mEnableSound) {
    		mSoundManager.playSound(SOUND_EFFECT_POP);
    	}
    }
    
    protected void toggleAllButtons(boolean mode) {
		mResumeButtonPauseMenu.setClickable(mode);
		mRestartButtonPauseMenu.setClickable(mode);
		mExitButtonPauseMenu.setClickable(mode);

		mRestartButtonGameOverMenu.setClickable(mode);
		mExitButtonGameOverMenu.setClickable(mode);		
    }
    /*
    protected HUD getHUD() {
    	return mHUD;
    }*/
    
	// Retry button in the pause menu
	protected OnClickListener retryButtonListener = new View.OnClickListener() {     	
        public void onClick(View v) {
        	toggleAllButtons(false);
    		mPauseLabel.clearAnimation();
    		mResumeButtonPauseMenu.clearAnimation();
    		mRestartButtonPauseMenu.clearAnimation();
    		mPauseUIBottomElements.clearAnimation();
    		
    		mSlideOutToTop.setAnimationListener(new AnimationListener() {

    			@Override
    			public void onAnimationEnd(Animation arg0) {
    		    	restartGame();
    			}

    			@Override
    			public void onAnimationRepeat(Animation arg0) {
    				// TODO Auto-generated method stub
    				
    			}

    			@Override
    			public void onAnimationStart(Animation arg0) {
    				// TODO Auto-generated method stub
    				
    			}
    			
    		} );
    		
    		mPauseLabel.startAnimation(mSlideOutToTop);
    		View buttonGroup = findViewById(R.id.button_group);
    		buttonGroup.startAnimation(mSlideOutToTop);
    		mPauseUIBottomElements.startAnimation(mSlideOutToTop);
        }
    };
    
    // Retry button in the game over menu
	protected OnClickListener retryButtonListenerAlt = new View.OnClickListener() {
        public void onClick(View v) {
        	toggleAllButtons(false);
    		mExitButtonGameOverMenu.clearAnimation();
    		mRestartButtonGameOverMenu.clearAnimation();
    		mScoreLabel.clearAnimation();

    		mSlideOutToLeft.reset();
    		mSlideOutToLeft.setAnimationListener(new AnimationListener() {

    			@Override
    			public void onAnimationEnd(Animation arg0) {
    				restartGame();
    			}

    			@Override
    			public void onAnimationRepeat(Animation arg0) {
    				// TODO Auto-generated method stub
    				
    			}

    			@Override
    			public void onAnimationStart(Animation arg0) {
    				// TODO Auto-generated method stub
    				
    			}
    			
    		} );
    		
    		mScoreLabel.startAnimation(mSlideOutToTop);
    		mExitButtonGameOverMenu.startAnimation(mSlideOutToRight);
    		mRestartButtonGameOverMenu.startAnimation(mSlideOutToLeft);
        }
    };
    
    // Exit button in the pause menu
    protected OnClickListener exitButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
        	toggleAllButtons(false);
        	mPauseLabel.clearAnimation();
    		mResumeButtonPauseMenu.clearAnimation();
    		mRestartButtonPauseMenu.clearAnimation();
    		mPauseUIBottomElements.clearAnimation();
    		
    		mSlideOutToTop.reset();
    		mSlideOutToTop.setAnimationListener(new AnimationListener() {

    			@Override
    			public void onAnimationEnd(Animation arg0) {
    		    	mGameOverUI.setVisibility(View.INVISIBLE);
    				mPauseUI.setVisibility(View.INVISIBLE);
    		    	finish();
    			}

    			@Override
    			public void onAnimationRepeat(Animation arg0) {
    				// TODO Auto-generated method stub
    				
    			}

    			@Override
    			public void onAnimationStart(Animation arg0) {
    				// TODO Auto-generated method stub
    				
    			}
    			
    		} );
    		
    		mPauseLabel.startAnimation(mSlideOutToTop);

    		View buttonGroup = findViewById(R.id.button_group);
    		buttonGroup.startAnimation(mSlideOutToTop);
    		mPauseUIBottomElements.startAnimation(mSlideOutToTop);
        }
    };
    
    // Exit button in the game over UI
    protected OnClickListener exitButtonListenerAlt = new View.OnClickListener() {
        public void onClick(View v) {
        	toggleAllButtons(false);
    		mExitButtonGameOverMenu.clearAnimation();
    		mRestartButtonGameOverMenu.clearAnimation();
			mScoreLabel.clearAnimation();

    		mSlideOutToLeft.reset();
    		mSlideOutToLeft.setAnimationListener(new AnimationListener() {

    			@Override
    			public void onAnimationEnd(Animation arg0) {
    		    	mGameOverUI.setVisibility(View.INVISIBLE);
    				mPauseUI.setVisibility(View.INVISIBLE);
    		    	finish();
    			}

    			@Override
    			public void onAnimationRepeat(Animation arg0) {
    				// TODO Auto-generated method stub
    				
    			}

    			@Override
    			public void onAnimationStart(Animation arg0) {
    				// TODO Auto-generated method stub
    				
    			}
    			
    		} );
    		
    		mScoreLabel.startAnimation(mSlideOutToTop);
    		mExitButtonGameOverMenu.startAnimation(mSlideOutToRight);
    		mRestartButtonGameOverMenu.startAnimation(mSlideOutToLeft);
        }
    };
    
    // Resume button in the pause menu
    protected OnClickListener resumeButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
        	toggleAllButtons(false);
     	   	togglePause();
        }
    };
    
	protected OnTouchListener mTouchEvent = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {			
			int action = arg1.getAction();
			int actionCode = action & MotionEvent.ACTION_MASK;
			int pointerId = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			int x;
			int y;
			
			x = (int)arg1.getX(pointerId);
			y = (int)arg1.getY(pointerId);
			if (actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_POINTER_DOWN) {
				//playPop();
				thread.checkForTouch(x, y);
			}
				
				
			return true;
		}
	};
		
		
	/*
	 * Pushes this runnable every second to the message queue to update the Arcade mode's timer
	 * */
	protected Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
			   thread.secondsEvent(mInterval);
		       if (game.mCurrentTime > 0) {
		    	   //Call this Runnable again after 1 second
		    	   mHandler.postDelayed(this, mInterval);
		       }
		       else
		       {
		    	   if (thread.getCurrentState() == GameCanvas.STATE_RUNNING) {
		    		   thread.setState(GameCanvas.STATE_GAME_END_1);
		    		   mHandler.postDelayed(this, mInterval);
		    		   alertTimesUp();
		    	   }

		    	   else if (thread.getCurrentState() == GameCanvas.STATE_GAME_END_1 && thread.noBalloonsOnScreen()) {
		    		   thread.setState(GameCanvas.STATE_GAME_END_2);
		    		   mHandler.postDelayed(this, mInterval);
		    	   }
		    	   else if (thread.getCurrentState() == GameCanvas.STATE_GAME_END_2 && game.doneCleanup() && !game.mPaused) {
		    		   thread.setState(GameCanvas.STATE_GAME_END_3);
		    		   mHandler.removeCallbacks(mUpdateTimeTask);
		    		   presentGameOver();
		    	   }
		    	   else
		    		   mHandler.postDelayed(this, mInterval);
		       }
		   }
		};
}
