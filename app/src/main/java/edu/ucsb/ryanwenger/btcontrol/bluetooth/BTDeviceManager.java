package edu.ucsb.ryanwenger.btcontrol.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Semaphore;

import edu.ucsb.ryanwenger.btcontrol.BTControl;

public class BTDeviceManager {
    private static BTDeviceManager instance = null;

    public static @NonNull BTDeviceManager getInstance() {
        if (instance == null)
            instance = new BTDeviceManager();
        return instance;
    }

    BluetoothAdapter mBtAdapter;
    Set<BluetoothDevice> mPairedDevs;

    private BTDeviceManager() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        instance = this;
    }

    public void closeProxy(BluetoothProfile proxyHandle) {
        if (proxyHandle == null)
            return;

        int type;
        Class c = proxyHandle.getClass();
        if (c == BluetoothHeadset.class)
            type = BluetoothProfile.HEADSET;
        else if (c == BluetoothA2dp.class)
            type = BluetoothProfile.A2DP;
        else if (c == BluetoothHidDevice.class)
            type = BluetoothProfile.HID_DEVICE;
        else
            return;

        mBtAdapter.closeProfileProxy(type, proxyHandle);
    }

    public enum BTStatus {
        UNSUPPORTED,
        DISABLED,
        ENABLED,
    }

    public BTStatus getBTStatus() {
        if (mBtAdapter == null) {
            return BTStatus.UNSUPPORTED;
        }

        if (!mBtAdapter.isEnabled()) {
            return BTStatus.DISABLED;
        }

        return BTStatus.ENABLED;
    }

    public ArrayList<BTDevice> getAllDevices() {
        ArrayList<BTDevice> res = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(BTControl.getContext(),
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return res;
        }

        mPairedDevs = mBtAdapter.getBondedDevices();

        for (BluetoothDevice dev : mPairedDevs) {
            res.add(new BTDevice(dev));
        }
        return res;
    }

    public BluetoothProfile connectProfile(BTDevice dev, int type) {
        final BluetoothProfile[] proxyHandle = new BluetoothProfile[1];
        if (type != BluetoothProfile.HEADSET
                && type != BluetoothProfile.A2DP
                && type != BluetoothProfile.HID_DEVICE) {
            return null;
        }

        Semaphore sem = new Semaphore(0);

        new Thread(() -> {
            final BluetoothProfile.ServiceListener[] profileListener
                    = new BluetoothProfile.ServiceListener[1];
            profileListener[0] = new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.HEADSET) {
                        proxyHandle[0] = (BluetoothHeadset) proxy;
                        sem.release();
                    } else if (profile == BluetoothProfile.A2DP) {
                        proxyHandle[0] = (BluetoothA2dp) proxy;
                        sem.release();
                    } else if (profile == BluetoothProfile.HID_DEVICE) {
                        proxyHandle[0] = (BluetoothHidDevice) proxy;
                        sem.release();
                    }
                }

                public void onServiceDisconnected(int profile) {
                    proxyHandle[0] = null;
                }
            };


            mBtAdapter.getProfileProxy(BTControl.getContext(), profileListener[0], type);
        }).start();

        try {
            sem.acquire();
        } catch (InterruptedException e) {
            Log.d("RYANRYAN", "" + e.getLocalizedMessage());
        }

        return proxyHandle[0];
    }
}
