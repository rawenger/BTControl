package edu.ucsb.ryanwenger.btcontrol.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import edu.ucsb.ryanwenger.btcontrol.BTControl;

@SuppressLint("MissingPermission")
public class BTDevice {
    public void buildCommandMaps() {
        if (mCmds != null)
            return;

        mCmds = new HashMap<>();

        mCmds.put("exit", (ss) -> {
            return "TODO";
        });

        mCmds.put("help", (ss) -> getHelp());

        mCmds.put("proto-headset", unused -> openHeadsetMenu());
        mCmds.put("proto-player", unused -> openA2dpMenu());
        mCmds.put("proto-HID", unused -> openHIDMenu());
    }

    private Map<String, Function<String[], String>> mCmds = null;

    private BluetoothDevice mDev;

    private BluetoothProfile mProtoHandle = null;
    private HIDConnectionMgr mHidMgr = null;

    public BTDevice(@NonNull BluetoothDevice dev) {
        mDev = dev;
    }

    @NonNull
    public String toString() {
        if (ActivityCompat.checkSelfPermission(
                BTControl.getContext(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            return "";
        }

        Function<Integer, String> f = i -> "" + i;
        return mDev.getName();
    }

    public BluetoothDevice get() {
        return mDev;
    }

    public ArrayList<String> getAutocomplete() {
        return new ArrayList<>(mCmds.keySet());
    }

    public void dispatchAsync(CharSequence cmd,
                              @NonNull Activity callbackOwner,
                              Consumer<String> callback) {
        dispatchAsync(cmd.toString(), callbackOwner, callback);
    }

    public void dispatchAsync(String cmd,
                              @NonNull Activity callbackOwner,
                              @NonNull Consumer<String> callback) {
        new Thread(() -> {
            String output = dispatch(cmd);
            callbackOwner.runOnUiThread(() ->
                    callback.accept(output));
        }).start();
    }

    public String dispatch(CharSequence cmd) {
        return dispatch(cmd.toString());
    }

    public String dispatch(String cmd) {
        String[] argv = cmd.split("[\\s\\t]+", 2);
        String commandName = argv[0];
        String[] args = null;
        if (argv.length > 1)
            args = argv[1].split("[\\s\\t]+", 0);

        Function<String[], String> action = mCmds.get(commandName);
        if (action == null)
            return "Invalid command '" + cmd + "'";

        return action.apply(args);
    }

    private String getHelp() {
        if (mCmds == null)
            return "";

        StringBuilder res = new StringBuilder("Avaliable commands:\n");
        for (String k : mCmds.keySet())
            res.append("\t").append(k).append("\n");

        return res.toString();
    }

    private String backToStart(String... unused) {
        mCmds = null;
        buildCommandMaps();
        BTDeviceManager.getInstance().closeProxy(mProtoHandle);
        return "";
    }

    /** PROTOCOL ROUTINES */
    private String openA2dpMenu() {
        BluetoothA2dp a2dp = (BluetoothA2dp) BTDeviceManager.getInstance()
                .connectProfile(this, BluetoothProfile.A2DP);

        if (a2dp == null || a2dp.getConnectionState(mDev) != BluetoothProfile.STATE_CONNECTED)
            return "unable to select audio player protocol";

        mProtoHandle = a2dp;
        mCmds = A2dpCmds();

        return "success";

    }

    private String openHeadsetMenu() {
        BluetoothHeadset headset = (BluetoothHeadset) BTDeviceManager.getInstance()
                .connectProfile(this, BluetoothProfile.HEADSET);

        if (headset.getConnectionState(mDev) != BluetoothProfile.STATE_CONNECTED)
            return "unable to select headset protocol";

        mProtoHandle = headset;
        mCmds = headsetCmds();

        return "success";
    }

    private Map<String, Function<String[], String>>
    headsetCmds() {
        Map<String, Function<String[], String>> cmds = new HashMap<>();

        Function<Boolean, String> voiceRec = shouldStart -> {
            BluetoothHeadset headset = (BluetoothHeadset) mProtoHandle;
            if (!headset.isVoiceRecognitionSupported(mDev))
                return "Voice recognition is not supported by this device.";

            if (ActivityCompat.checkSelfPermission(BTControl.getContext(),
                    Manifest.permission.MODIFY_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            {
                return "Missing required system permission 'MODIFY_PHONE_STATE'";
            }

            if (shouldStart) {
                headset.startVoiceRecognition(mDev);
                return "Started voice recognition";
            } else {
                headset.stopVoiceRecognition(mDev);
                return "Stopped voice recognition";
            }
        };

        cmds.put("start-voice-recognition", unused -> voiceRec.apply(true));

        cmds.put("stop-voice-recognition", unused -> voiceRec.apply(false));

        cmds.put("back", this::backToStart);

        cmds.put("help", unused -> getHelp());

        return cmds;
    }

    private Map<String, Function<String[], String>>
    A2dpCmds() {
        Map<String, Function<String[], String>> cmds = new HashMap<>();

        cmds.put("back", this::backToStart);

        cmds.put("help", unused -> getHelp());

        return cmds;
    }

    public void finalize() {
        if (mProtoHandle != null)
            BTDeviceManager.getInstance().closeProxy(mProtoHandle);
    }

    private String openHIDMenu() {
        BluetoothHidDevice hid = (BluetoothHidDevice) BTDeviceManager.getInstance()
                .connectProfile(this, BluetoothProfile.HID_DEVICE);

        if (ActivityCompat.checkSelfPermission(BTControl.getContext(),
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            return "Insufficient permission";
        }

        if (hid == null)
            return "unable to connect to host device";

        mHidMgr = new HIDConnectionMgr(hid);

        new Thread(() -> {
            hid.registerApp(HIDConnectionMgr.sdpRecord,
                    null, HIDConnectionMgr.qosOut,
                    command -> new Thread(command).start(),
                    mHidMgr);
        }).start();

        long timeout = System.currentTimeMillis() / 1000;
        while (hid.getConnectedDevices().size() == 0) {
            if ((System.currentTimeMillis() / 1000) - timeout > 20) {
                return "unable to select HID protocol: connection timed out";
            }
        }

        mProtoHandle = hid;

        mCmds = hidCmds();
        return "success";
    }

    private Map<String, Function<String[], String>>
    hidCmds() {
        Map<String, Function<String[], String>> cmds = new HashMap<>();

        cmds.put("back", unused -> {
            mHidMgr = null;
            return backToStart();
        });
        cmds.put("help", unused -> getHelp());

        cmds.put("setDx", dxStr -> {
            if (dxStr == null || dxStr.length != 1)
                return "usage: setDx <[0, 255]>";

            byte dx = Byte.parseByte(dxStr[0]);
            mHidMgr.sendReport(new HIDMouseReport().setDx(dx));
            return "sent";
        });

        cmds.put("setDy", dyStr -> {
            if (dyStr == null || dyStr.length != 1)
                return "usage: setDy <[255, 0]> (note inverted axis)";

            byte dy = Byte.parseByte(dyStr[0]);
            mHidMgr.sendReport(new HIDMouseReport().setDx(dy));
            return "sent";
        });

        cmds.put("setScroll", scrollStr -> {
            if (scrollStr == null || scrollStr.length != 1)
                return "usage: setScroll <[-128, 127]> (note inverted axis)";

            byte dy = Byte.parseByte(scrollStr[0]);
            mHidMgr.sendReport(new HIDMouseReport().setDx(dy));
            return "sent";
        });

        cmds.put("click", durationStr -> {
            String usage = "usage: click <left|right|middle> [durationMs]";
            if (durationStr == null || durationStr.length == 0 || durationStr.length > 2)
                return usage;

            byte buttons = 0;
            switch (durationStr[0]) {
                case "left":
                    buttons |= HIDMouseReport.LEFT_BUTTON_MASK;
                    break;
                case "right":
                    buttons |= HIDMouseReport.RIGHT_BUTTON_MASK;
                    break;
                case "middle":
                    buttons |= HIDMouseReport.MIDDLE_BUTTON_MASK;
                    break;
                default:
                    return usage;
            }

            long duration = durationStr.length > 1
                    ? Long.parseLong(durationStr[1])
                    : 0;

            if (duration < 0)
                return usage;

            HIDMouseReport rep = new HIDMouseReport()
                    .clickButton(true, buttons);

            mHidMgr.sendReport(rep);
            Log.d("RYANRYAN", "clicking");

            if (duration == 0)
                return "sent";

            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                return "error occurred before release of mouse button";
            }

            rep.clickButton(false, buttons);
            mHidMgr.sendReport(rep);

            return "sent";
        });

        return cmds;
    }

    private class HIDConnectionMgr extends BluetoothHidDevice.Callback {
        static final BluetoothHidDeviceAppSdpSettings sdpRecord
                = new BluetoothHidDeviceAppSdpSettings(
                        "BTControl",
                        "Bluetooth command console",
                        "edu.ucsb.ryanwenger",
                        BluetoothHidDevice.SUBCLASS1_COMBO,
                        HIDDescriptors.INSTANCE.getMOUSE_KEYBOARD_COMBO());

        static final BluetoothHidDeviceAppQosSettings qosOut
                = new BluetoothHidDeviceAppQosSettings(
                        BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
                        800, 9, 0, 11250,
                BluetoothHidDeviceAppQosSettings.MAX);

        static final byte REPORT_ID_FEATURE = 6;

        BluetoothHidDevice hid;

        private boolean mCleanup = false;

        public HIDReport mReport;

        public HIDConnectionMgr(@NonNull BluetoothHidDevice hid) {
            super();
            this.hid = hid;
        }

        public void sendReport(HIDReport report) {
            hid.sendReport(BTDevice.this.mDev, report.getID(), report.getReport());
        }

        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            super.onAppStatusChanged(pluggedDevice, registered);
            if (registered) {
                if (pluggedDevice != null) {
                    BTDevice.this.mDev = pluggedDevice;
                    Log.d("RYANRYAN", "plugged " + pluggedDevice.getName());
                }
                hid.connect(BTDevice.this.mDev);
            } else {
                hid.disconnect(BTDevice.this.mDev);
            }
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            super.onConnectionStateChanged(device, state);
            if (state == BluetoothHidDevice.STATE_DISCONNECTING
                || state == BluetoothHidDevice.STATE_DISCONNECTED)
            {
                if (mCleanup)
                    hid.unregisterApp();
            }
        }

        @Override
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            super.onGetReport(device, type, id, bufferSize);

            switch (type) {
                case BluetoothHidDevice.REPORT_TYPE_FEATURE:
                    hid.replyReport(device, type, REPORT_ID_FEATURE, new byte[]{0b101});
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            super.onSetReport(device, type, id, data);
        }

        @Override
        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            super.onSetProtocol(device, protocol);
        }

        @Override
        public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) {
            super.onInterruptData(device, reportId, data);
        }

        @Override
        public void onVirtualCableUnplug(BluetoothDevice device) {
            super.onVirtualCableUnplug(device);
        }

        public void finalize() {
            mCleanup = true;
            hid.disconnect(mDev);
        }
    }
}
