package com.balloon.dl;

import android.app.Activity;
import android.content.Intent;
import android.view.animation.Animation;

public abstract class MenuActivity extends Activity {

	protected abstract void transitionIntoActivity(Intent i);
	protected abstract void toggleAllButtons(boolean mode);
	
	public void onResume() {
		super.onResume();
	}
	
	protected class StartActivityAfterAnimation implements Animation.AnimationListener {
        private Intent mIntent;
        
        StartActivityAfterAnimation(Intent intent) {
            mIntent = intent;
        }
            
        public void onAnimationEnd(Animation animation) {
        	if (mIntent != null)
        		startActivity(mIntent);
        	else
        		finish();
        	//Performs no animation during the transition between two activities (no "right to left" animation)
            overridePendingTransition(0, 0);
        }

        public void onAnimationRepeat(Animation animation) {
            // TODO Auto-generated method stub
            
        }

        public void onAnimationStart(Animation animation) {
            // TODO Auto-generated method stub
            
        }
        
    }
}
