package com.balloon.dl.customviews;

import com.balloon.dl.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

public class HUDForTimedGame extends HUD {
	private BorderedTextView mScore;
	private BorderedTextView mCombo;
	private BorderedTextView mHighScore;
	private BorderedTextView mTimer;

	public HUDForTimedGame(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.hud_timed, this);
		
		mScore = (BorderedTextView)findViewById(R.id.hud_score);
		mCombo = (BorderedTextView)findViewById(R.id.hud_combo);
		mHighScore = (BorderedTextView)findViewById(R.id.hud_highscore);
		mTimer = (BorderedTextView)findViewById(R.id.hud_timer);
		
		Log.d("HUD initialized!", "derp");
	}

	public void updateScore(int score) {
		mScore.setText("Score: " + score, TextView.BufferType.EDITABLE);
	}
	
	public void updateCombo(int combo) {
		mCombo.setText("Combo: " + combo, TextView.BufferType.EDITABLE);
	}
	
	public void updateHighScore(int highscore) {
		mHighScore.setText("Best: " + highscore, TextView.BufferType.EDITABLE);
	}
	
	public void updateTimer(double milliseconds) {
		int seconds = (int)Math.ceil(milliseconds/1000);
		int min = (int)(seconds/60);
		int sec = (int)(seconds % 60);
		
		mTimer.setText("" + min + ":" + (sec < 10 ? "0" : "") + sec, TextView.BufferType.EDITABLE);
	}
}
