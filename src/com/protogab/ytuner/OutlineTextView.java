package com.protogab.ytuner;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/*
 * This is a custom TextView to be able to have a text outline(border) effect which is a lacking feature of android.
 * NOTE:  the textview element on the layout should have a property declared android:shadowRadius="3.0" (3.0 or some value) 
 * //http://stackoverflow.com/questions/3182393/android-textview-outline-text
 */

public class OutlineTextView extends TextView {

    // Constructors
	 public OutlineTextView(Context context) {
	super(context);
	// TODO Auto-generated constructor stub
	 }

	public OutlineTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
    public OutlineTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void draw(Canvas canvas) {
        for (int i = 0; i < 5; i++) {
            super.draw(canvas);
        }
    }

}
