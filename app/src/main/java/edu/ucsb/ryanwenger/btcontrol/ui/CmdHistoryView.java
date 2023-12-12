package edu.ucsb.ryanwenger.btcontrol.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
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

    public void printDone() {
        mText.append("\n===========DONE===========\n");
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
    }
}