package com.balloon.dl;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;

public class GameMenuActivity extends MenuActivity {
	View mTopElements;
	View mBottomElements;
	View mArcadeModeButton;
	View mRushModeButton;
	View mBackButton;
	
	Animation mTopElementSlideInAnim;
	Animation mBottomElementSlideInAnim;
	Animation mSlideOutAnim;
	
	private boolean mInGame;
	
	// The delay variable for the garbage collector to reclaim memory used by the previous game (all because of Gingerbread's concurrent garbage collector)
	private static final int sGCDelay = 1; //1000;
	
	private View.OnClickListener backButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
        	toggleAllButtons(false);
    		transitionIntoActivity(null);	
        }
    };
    
    private View.OnClickListener arcadeButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
        	mInGame = true;
        	toggleAllButtons(false);
     	   	Intent i = new Intent(GameMenuActivity.this, ArcadeModeActivity.class);
     	   	transitionIntoActivity(i);
        }
    };
    
    private View.OnClickListener comboRushButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
        	mInGame = true;
        	toggleAllButtons(false);
     	   	Intent i = new Intent(GameMenuActivity.this, RushModeActivity.class);
     	   	transitionIntoActivity(i);
        }
    };
	
    protected void transitionIntoActivity(Intent i) {
 	   	mTopElements.clearAnimation();
		mBottomElements.clearAnimation();
		
		mSlideOutAnim.reset();
		mSlideOutAnim.setAnimationListener(new StartActivityAfterAnimation(i));
		mTopElements.startAnimation(mSlideOutAnim);
		mBottomElements.startAnimation(mSlideOutAnim);    	
    }
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mInGame = false;
		setContentView(R.layout.gamemenu);

		mBackButton = findViewById(R.id.back_button);
		mBackButton.setOnClickListener(backButtonListener);
		mArcadeModeButton = findViewById(R.id.arcade_mode);
		mArcadeModeButton.setOnClickListener(arcadeButtonListener);
		mRushModeButton = findViewById(R.id.combo_rush_mode);
		mRushModeButton.setOnClickListener(comboRushButtonListener);
		
		mTopElements = findViewById(R.id.top_elements);
		mBottomElements = findViewById(R.id.bottom_elements);
		
		mTopElementSlideInAnim = AnimationUtils.loadAnimation(this, R.anim.top_slide_in);
		mBottomElementSlideInAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_slide_in);
		mSlideOutAnim = AnimationUtils.loadAnimation(this, R.anim.top_slide_out);
		
		toggleAllButtons(false);
	}

	public void onStart() {
		super.onStart();
		
		if (mInGame) {
			try {
				Thread.sleep(sGCDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mInGame = false;
		}
	}
	
	public void onPause() {
		super.onPause();

		mTopElements.setVisibility(View.INVISIBLE);
		mBottomElements.setVisibility(View.INVISIBLE);
		
		overridePendingTransition(0, 0);
	}

	public void onResume() {
		super.onResume();
		mBackButton.clearAnimation();
		mBottomElements.clearAnimation();
		
		mBottomElements.setVisibility(View.VISIBLE);
		mTopElements.setVisibility(View.VISIBLE);
		
		mTopElementSlideInAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				toggleAllButtons(true);
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
		
		mBottomElements.startAnimation(mBottomElementSlideInAnim);
		mTopElements.startAnimation(mTopElementSlideInAnim);
	}
	
    /*
     * Captures the back button press to pause the game instead of killing the activity
     * */
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (mBackButton.isClickable()) {
                transitionIntoActivity(null);
        	}
    		return false;
        }

        return super.onKeyDown(keyCode, event);
    }
    
    protected void toggleAllButtons(boolean mode) {
		mBackButton.setClickable(mode);
		mArcadeModeButton.setClickable(mode);
		mRushModeButton.setClickable(mode);
    }
}