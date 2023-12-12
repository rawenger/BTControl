package edu.ucsb.ryanwenger.btcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import edu.ucsb.ryanwenger.btcontrol.bluetooth.BTDevice;
import edu.ucsb.ryanwenger.btcontrol.ui.CmdHistoryView;

public class SendCommandsActivity extends AppCompatActivity {
    public static final String BTDEV_EXTRA_KEY = "BTDeviceHandle";

    private AutoCompleteTextView mCmdEntryView;
    private CmdHistoryView mHistoryView;
    private BTDevice mTargetDevice;
    private ArrayAdapter<String> mAutocompleteAdapter;

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

        mCmdEntryView = findViewById(R.id.bt_command_text_view);
        assert mCmdEntryView != null;
        mAutocompleteAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, mTargetDevice.getAutocomplete());
        mCmdEntryView.setThreshold(0);
        mCmdEntryView.setAdapter(mAutocompleteAdapter);

        mHistoryView = findViewById(R.id.send_cmds_history);

        mCmdEntryView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEND)
                return false;


            CharSequence cmd = v.getText();
            mHistoryView.printCommand(cmd);
            v.setText("");

            mTargetDevice.dispatchAsync(cmd, SendCommandsActivity.this,
                    (String output) ->  {

                        mHistoryView.printResult(output);

                        mAutocompleteAdapter.clear();
                        mAutocompleteAdapter.addAll(mTargetDevice.getAutocomplete());
                        mAutocompleteAdapter.notifyDataSetChanged();
                    });

            return true;
        });

    }


}