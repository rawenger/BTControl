package edu.ucsb.ryanwenger.btcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicReference;

import edu.ucsb.ryanwenger.btcontrol.bluetooth.BTDevice;

public class SendCommandsActivity extends AppCompatActivity {
    public static final String BTDEV_EXTRA_KEY = "BTDeviceHandle";

    AutoCompleteTextView mCmdEntryView;
    TextView mHistoryView;

    BTDevice mTargetDevice;

    ArrayAdapter<String> mAutocompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_commands);

        BluetoothDevice target = getIntent().getExtras()
                                            .getParcelable(BTDEV_EXTRA_KEY);
        if (target == null)
            finish();

        mTargetDevice = new BTDevice(target);
        mTargetDevice.buildCommandMaps();
//        BTDeviceManager.getInstance().connectDevice(mTargetDevice);

        mCmdEntryView = findViewById(R.id.bt_command_text_view);
//        mCmdEntryView.set;
        mHistoryView = findViewById(R.id.cmd_history);
        assert mCmdEntryView != null;
        mAutocompleteAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, mTargetDevice.getAutocomplete());
        mCmdEntryView.setThreshold(0);
        mCmdEntryView.setAdapter(mAutocompleteAdapter);

        mCmdEntryView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            AtomicReference<String> cmdOutput = new AtomicReference<>("");
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_ACTION_SEND)
                    return false;

                ScrollView sv = findViewById(R.id.history_scrollview);

                CharSequence cmd = v.getText();
                mHistoryView.append("> " + cmd + "\n");
                v.setText("");
                sv.smoothScrollTo(0, sv.getBottom());

                mTargetDevice.dispatchAsync(cmd, SendCommandsActivity.this,
                        (String output) ->  {
                            mHistoryView.append(output + "\n\n");
                            sv.smoothScrollTo(0, sv.getBottom());

                            mAutocompleteAdapter.clear();
                            mAutocompleteAdapter.addAll(mTargetDevice.getAutocomplete());
                            mAutocompleteAdapter.notifyDataSetChanged();
                        });

                return true;
            }
        });
//        mHistoryView.setMovementMethod(new ScrollingMovementMethod());

    }


}