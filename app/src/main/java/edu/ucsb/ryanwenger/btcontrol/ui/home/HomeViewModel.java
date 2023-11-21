package edu.ucsb.ryanwenger.btcontrol.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import edu.ucsb.ryanwenger.btcontrol.bluetooth.BTDevice;
import edu.ucsb.ryanwenger.btcontrol.bluetooth.BTDeviceManager;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<BTDevice>> mDevs;
//    private final DeviceStore mDevStore;

    private BTDeviceManager mDevMgr;

    public HomeViewModel() {
        /* initialize the device list from disk */
        mDevs = new MutableLiveData<>();
        mDevMgr = BTDeviceManager.getInstance();
    }

    public LiveData<ArrayList<BTDevice>> getDevs() {
        return mDevs;
    }

    public void refreshDevices() {
        mDevs.setValue(mDevMgr.getAllDevices());
    }
}