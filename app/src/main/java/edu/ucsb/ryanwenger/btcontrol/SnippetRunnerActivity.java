package edu.ucsb.ryanwenger.btcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import edu.ucsb.ryanwenger.btcontrol.bluetooth.BTDevice;
import edu.ucsb.ryanwenger.btcontrol.bluetooth.BTDeviceManager;
import edu.ucsb.ryanwenger.btcontrol.ui.CmdHistoryView;

public class SnippetRunnerActivity extends AppCompatActivity {
    public static final String EXTRA_SNIPPET_NAME = "SnippetRunner.name";
    public static final String EXTRA_SNIPPET_CONTENTS = "SnippetRunner.contents";
    private static final String BUNDLE_TARGET_DEVICE = "SnippetRunner.targetDevice";

    public static void launch(Activity fromActivity,
                              @NonNull String name,
                              @Nullable String contents)
    {
        Intent target = new Intent(BTControl.getContext(), SnippetRunnerActivity.class);
        target.putExtra(EXTRA_SNIPPET_NAME, name);
        target.putExtra(EXTRA_SNIPPET_CONTENTS, contents);
        fromActivity.startActivity(target);
    }

    private CmdHistoryView mHistory;

    private String[] mContents;

    private int mCurLine = 0;

    private BTDevice mDev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_runner);

        Intent trigger = getIntent();
        String name = trigger.getStringExtra(EXTRA_SNIPPET_NAME);
        String contents = trigger.getStringExtra(EXTRA_SNIPPET_CONTENTS);

        if (name == null)
            finish();

        if (contents == null)
            contents = SnippetStore.getInstance().loadSnip(name);

        if (contents.isEmpty())
            finish();

        mContents = contents.split("\\n+", 0);

        mHistory = findViewById(R.id.snippet_hist_view);

        mDev = getTargetDevice(savedInstanceState);
        if (mDev != null)
            start();
    }

//    @SuppressLint("NewApi")
    private BTDevice getTargetDevice(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            BluetoothDevice lastUsed =
                    savedInstanceState.getParcelable(BUNDLE_TARGET_DEVICE);

            if (lastUsed != null)
                return new BTDevice(lastUsed);
        }

        ArrayList<BTDevice> devices = BTDeviceManager.getInstance().getAllDevices();
        String[] deviceNames = new String[devices.size()];
        for (int i = 0; i < devices.size(); i++)
            deviceNames[i] = devices.get(i).toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Target Device")
                .setItems(deviceNames, (dialog, which) -> {
                    mDev = devices.get(which);
                    // want to schedule execution *after* returning from callback
                    new Thread(() ->
                                runOnUiThread(SnippetRunnerActivity.this::start))
                            .start();
                });

        builder.create().show();

        return null;
    }

    private void start() {
        mDev.buildCommandMaps();
        mCurLine = 0;
        runNextLine();
    }

    private void runNextLine() {
        if (mCurLine == mContents.length) {
            mHistory.printDone();
            return;
        }

        String line = mContents[mCurLine++];

        mHistory.printCommand(line);

        mDev.dispatchAsync(line, this,
                (output) -> {
                    mHistory.printResult(output);
                    runNextLine();
                });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mDev != null)
            outState.putParcelable(BUNDLE_TARGET_DEVICE, mDev.get());
    }

}