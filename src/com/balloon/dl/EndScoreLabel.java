package com.balloon.dl;

import com.balloon.dl.R;
import com.balloon.dl.customviews.BorderedTextView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EndScoreLabel extends LinearLayout {
	BorderedTextView mHighScoreLabel;
	BorderedTextView mScore;
	
	public EndScoreLabel(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.end_score_label, this);

		mScore = (BorderedTextView) findViewById(R.id.final_score);
		mHighScoreLabel = (BorderedTextView) findViewById(R.id.high_score_label);
	}
	
	public void drawScore(int score) {
		drawScore(score, false);
	}
	
	public void drawScore(int score, boolean highScore) {
		mScore.setText("" + score, TextView.BufferType.EDITABLE);
		mHighScoreLabel.setVisibility(highScore ? View.VISIBLE : View.INVISIBLE);
	}
}
