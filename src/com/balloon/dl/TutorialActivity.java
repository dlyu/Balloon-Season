package com.balloon.dl;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;

public class TutorialActivity extends Activity {
	static final int sPageCount = 11;
	private int[] mPages;
	private int mCurrentPage;
	
	private Animation mSlideInLeft;
	private Animation mSlideOutLeft;
	private Animation mSlideInBottom;
	private Animation mSlideOutBottom;
	
	View mOldPage;
	View mNewPage;
	
	boolean mAnimating;
	
	private AnimationListener mSetInvisible = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mOldPage.setVisibility(View.INVISIBLE);
			mAnimating = false;
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	private AnimationListener mCloseTutorial = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			findViewById(R.id.tutorial_frame).setVisibility(View.INVISIBLE);
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
		
	};
	
	private AnimationListener mAnimationStopped = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mAnimating = false;
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	private View.OnClickListener exitButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
        	if (!mAnimating)
        		exitTutorial(mSlideOutBottom);
        }
    };
    
	private View.OnClickListener nextPageButtonListener = new View.OnClickListener() {
        public void onClick(View v) {	
    		goToPage(1);
        }
    };
    
	private View.OnClickListener prevPageButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
    		goToPage(-1);
        }
    };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tutorial);
		ImageButton exitButton = (ImageButton)findViewById(R.id.exit_tutorial);
		exitButton.setOnClickListener(exitButtonListener);
		
		ImageButton nextButton = (ImageButton)findViewById(R.id.goto_next_page);
		nextButton.setOnClickListener(nextPageButtonListener);
		
		ImageButton prevButton = (ImageButton)findViewById(R.id.goto_prev_page);
		prevButton.setOnClickListener(prevPageButtonListener);

		mPages = new int[sPageCount];
		mPages[0] = R.id.tutorial_page_1;
		mPages[1] = R.id.tutorial_page_2;
		mPages[2] = R.id.tutorial_page_3;
		
		mPages[3] = R.id.tutorial_page_4;
		mPages[4] = R.id.tutorial_page_5;
		mPages[5] = R.id.tutorial_page_6;
		/**/
		mPages[6] = R.id.tutorial_page_7;
		mPages[7] = R.id.tutorial_page_8;
		mPages[8] = R.id.tutorial_page_9;
		mPages[9] = R.id.tutorial_page_10;
		mPages[10] = R.id.tutorial_page_11;
		
		mCurrentPage = 0;
		
		mSlideInBottom = AnimationUtils.loadAnimation(this, R.anim.tutorial_bottom_slide_in);
		mSlideOutBottom = AnimationUtils.loadAnimation(this, R.anim.tutorial_bottom_slide_out);
		mSlideInLeft = AnimationUtils.loadAnimation(this, R.anim.tutorial_left_slide_in);
		mSlideOutLeft = AnimationUtils.loadAnimation(this, R.anim.tutorial_left_slide_out);
	}

	public void onStart() {
		super.onStart();
	}
	
	public void onPause() {
		super.onPause();

		overridePendingTransition(0, 0);
	}

	public void onResume() {
		super.onResume();
		
    	mSlideInBottom.reset();
    	mSlideInBottom.setAnimationListener(mAnimationStopped);
    	mAnimating = true;
    	findViewById(R.id.tutorial_frame).startAnimation(mSlideInBottom);
	}
	
    /*
     * Captures the back button press to pause the game instead of killing the activity
     * */
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	if (!mAnimating)
        		goToPage(-1);
    		
    		// Returning false is important! The activity should not consume the event so that the OS doesn't kill the activity.
    		return false;
        }

        return super.onKeyDown(keyCode, event);
    }
    
    synchronized void goToPage(int direction) {
    	if (mAnimating)
    		return;
    	
    	if (direction != -1 && direction != 1)
    		return;
    	int newPageIndex = mCurrentPage + direction;
    	mAnimating = true;
    	if (newPageIndex < 0 | newPageIndex >= sPageCount) {
    		exitTutorial(newPageIndex < 0 ? mSlideOutBottom : mSlideOutLeft);
    	}
    	else {
    		mOldPage = findViewById(mPages[mCurrentPage]);
    		mNewPage = findViewById(mPages[newPageIndex]);
    		mNewPage.setVisibility(View.VISIBLE);
    		
    		if (direction == 1) {
    			mSlideOutLeft.reset();
    			mSlideInBottom.reset();
    			
    			mSlideOutLeft.setAnimationListener(mSetInvisible);
    			
    			mOldPage.startAnimation(mSlideOutLeft);
    			mNewPage.startAnimation(mSlideInBottom);
    		}
    		
    		else {
    			mSlideInLeft.reset();
    			mSlideOutBottom.reset();    			
    			
    			mSlideOutBottom.setAnimationListener(mSetInvisible);
    			
    			mOldPage.startAnimation(mSlideOutBottom);
    			mNewPage.startAnimation(mSlideInLeft);
    		}
    		
        	mCurrentPage = newPageIndex;    		
    	}
    }
    
    private void exitTutorial(Animation anim) {
    	anim.reset();
    	anim.setAnimationListener(mCloseTutorial);
    	findViewById(R.id.tutorial_frame).startAnimation(anim);
    }
}