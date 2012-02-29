package com.balloon.dl;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
/*
 * This activity is for the main menu. It lets the user to pick between the options and the game. 
 */
public class MainMenuActivity extends MenuActivity {
	AlertDialog.Builder exitConfirm;
	
	View mLogo;
	View mBottomElements;
	
	Animation mTopElementSlideInAnim;
	Animation mBottomElementSlideInAnim;
	Animation mSlideOutAnim;
	
	ImageView mStartButton;
	ImageView mTutorialButton;
	
	CheckBox mOptionSound, mOptionColourWheel, mOptionVibrate;
	
	// The button listener for the start button
	private View.OnClickListener startButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			toggleAllButtons(false);
			//Create an intent to call the game menu
			Intent i = new Intent(MainMenuActivity.this, GameMenuActivity.class);
			transitionIntoActivity(i);				
		}
	};
	
	// The button listener for the option button
	private View.OnClickListener tutorialButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			toggleAllButtons(false);
			// Create an intent to call the game menu
			Intent i = new Intent(MainMenuActivity.this, TutorialActivity.class);
			transitionIntoActivity(i);
		}
	};
   
	// The button listener for the option button
	private DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			finish();
		}
	};
   
	//The button listener for the option button
	private DialogInterface.OnClickListener keepPlayingListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		setContentView(R.layout.main);
		
		PreferencesManager.getInstance(this.getBaseContext());
		
		mStartButton = (ImageView) findViewById(R.id.start_button);
		mStartButton.setOnClickListener(startButtonListener);
		
		mTutorialButton = (ImageView) findViewById(R.id.tutorial_button);
		mTutorialButton.setOnClickListener(tutorialButtonListener);
		
		exitConfirm = new AlertDialog.Builder(this);
		exitConfirm.setMessage("Are you sure you wish to exit Balloon Seasons?");
		exitConfirm.setPositiveButton("Exit", exitListener);
		exitConfirm.setNegativeButton("Keep Playing", keepPlayingListener);
		
		mLogo = findViewById(R.id.logo);
		mBottomElements = findViewById(R.id.bottom_elements);
		
		mTopElementSlideInAnim = AnimationUtils.loadAnimation(this, R.anim.top_slide_in);
		mBottomElementSlideInAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_slide_in);
		mSlideOutAnim = AnimationUtils.loadAnimation(this, R.anim.top_slide_out);
		
		mOptionSound = (CheckBox) findViewById(R.id.option_button_sound);
		mOptionColourWheel = (CheckBox) findViewById(R.id.option_button_colour_wheel);
		mOptionVibrate = (CheckBox) findViewById(R.id.option_button_vibrate);
		
		// Initialize all the bitmaps at the very beginning
		/*
		BalloonManager b = BalloonManager.getInstance();
		b.setAttributes(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, getBaseContext(), true);

		CloudManager c = CloudManager.getInstance();
		c.setAttributes(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, getBaseContext(), false);
		*/
	}
	
	public void onPause() {
		super.onPause();
		
		mLogo.setVisibility(View.INVISIBLE);
		mBottomElements.setVisibility(View.INVISIBLE);
		Utilities.updateOptions(mOptionSound, mOptionColourWheel, mOptionVibrate);
	}
	
	public void onResume() {
		super.onResume();
		mLogo.clearAnimation();
		mBottomElements.clearAnimation();
		
		mLogo.setVisibility(View.VISIBLE);
		mBottomElements.setVisibility(View.VISIBLE);
		
		mTopElementSlideInAnim.reset();
		mBottomElementSlideInAnim.reset();
		mLogo.startAnimation(mTopElementSlideInAnim);
		mBottomElements.startAnimation(mBottomElementSlideInAnim);
		
		toggleAllButtons(true);
		
		Utilities.resetOptionButtons(mOptionSound, mOptionColourWheel, mOptionVibrate);
	}

	public void onStop() {
		super.onStop();
	}
	
	public void onRestart() {
		super.onRestart();
		Utilities.resetOptionButtons(mOptionSound, mOptionColourWheel, mOptionVibrate);
	}

    /*
     * Captures the back button press to pause the game instead of killing the activity
     * */
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exitConfirm.show();
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

	@Override
	protected void transitionIntoActivity(Intent i) {
 	   	mLogo.clearAnimation();
		mBottomElements.clearAnimation();
		
		mSlideOutAnim.reset();
		mSlideOutAnim.setAnimationListener(new StartActivityAfterAnimation(i));
		mLogo.startAnimation(mSlideOutAnim);
		mBottomElements.startAnimation(mSlideOutAnim);  
	}
	
	protected void toggleAllButtons(boolean mode) {
		mStartButton.setClickable(mode);
		mTutorialButton.setClickable(mode);
	}
}