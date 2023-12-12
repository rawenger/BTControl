package edu.ucsb.ryanwenger.btcontrol.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import edu.ucsb.ryanwenger.btcontrol.R;

/**
 * TODO: document your custom view class.
 */
public class CmdHistoryView extends HorizontalScrollView {

    private TextView mText;
    private ScrollView mVScroll;

    public CmdHistoryView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CmdHistoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CmdHistoryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public void printCommand(CharSequence cmd) {
        // sem dec
        mText.append("> " + cmd + "\n");
        scrollToEnd();
    }

    public void printResult(CharSequence cmdResult) {
        mText.append(cmdResult + "\n\n");
        scrollToEnd();
    }

    private void scrollToEnd() {
        mVScroll.fullScroll(View.FOCUS_DOWN);
//        mVScroll.smoothScrollTo(0, mVScroll.getBottom());
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.cmd_history_view, this);

        mText = findViewWithTag("cmd_history");
        mVScroll = findViewWithTag("vertical_scrollview");

//        bringChildToFront(mText);
/*
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CmdHistoryView, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.CmdHistoryView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.CmdHistoryView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.CmdHistoryView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.CmdHistoryView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.CmdHistoryView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

 */
    }
}