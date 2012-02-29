package com.balloon.dl.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import com.balloon.dl.R;

public class BorderedTextView extends TextView {

	protected float mBorderSize;
	protected int mBorderColor;
	
	private Paint mBorderPaint = new Paint();
	private Paint mTextPaint = new Paint();
	
	private Context mContext;
	private AttributeSet mAttrs;
	
	private String mText;
	
	private int mAscent;
	
	public BorderedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mAttrs = attrs;

		TypedArray a = mContext.obtainStyledAttributes(mAttrs, R.styleable.BorderedTextView);
		
		// SET THE BORDER SIZE BEFORE YOU SET THE BORDER COLOUR!
		setBorderSize(a.getDimension(R.styleable.BorderedTextView_borderSize, (float)1.0));
		setBorderColor(a.getColor(R.styleable.BorderedTextView_borderColor, 0x000000));
		a.recycle();
		
		setTextColor(getCurrentTextColor());
		setText(getText(), TextView.BufferType.EDITABLE);
	}
	
	public void setBorderSize(float size) {
		if (size >= 1.0) {
			mBorderSize = size;
		}
			
        requestLayout();
        invalidate();
	}
	
	public void setBorderColor(int color) {
		mBorderColor = color;
		
		mBorderPaint.setColor(mBorderColor);
	    mBorderPaint.setTextSize(getTextSize());
	    mBorderPaint.setTypeface(Typeface.DEFAULT_BOLD);
	    mBorderPaint.setStyle(Paint.Style.STROKE);
	    mBorderPaint.setStrokeWidth(mBorderSize);
		
        requestLayout();
        invalidate();
	}
	
	@Override
	public void setTextColor(int color) {
		super.setTextColor(color);
		
	    mTextPaint.setColor(color);
	    mTextPaint.setTextSize(getTextSize());
	    mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
	    
        requestLayout();
        invalidate();
	}
	
	@Override
	public void setText(CharSequence str, TextView.BufferType buf) {
		super.setText(str, buf);
		mText = str.toString();
		
        requestLayout();
        invalidate();
	}

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) mBorderPaint.measureText(getText().toString()) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        
        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) mBorderPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + mBorderPaint.descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
	
	@Override
	public void onDraw(Canvas canvas) {
	    canvas.drawText(mText, getPaddingLeft(), getPaddingTop() - mAscent, mBorderPaint);
	    canvas.drawText(mText, getPaddingLeft(), getPaddingTop() - mAscent, mTextPaint);
	}
}
