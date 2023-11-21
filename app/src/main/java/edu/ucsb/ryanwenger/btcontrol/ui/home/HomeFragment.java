package edu.ucsb.ryanwenger.btcontrol.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import edu.ucsb.ryanwenger.btcontrol.BTControl;
import edu.ucsb.ryanwenger.btcontrol.R;
import edu.ucsb.ryanwenger.btcontrol.SendCommandsActivity;
import edu.ucsb.ryanwenger.btcontrol.bluetooth.BTDevice;
import edu.ucsb.ryanwenger.btcontrol.databinding.FragmentHomeBinding;

public class HomeFragment extends ListFragment {
    private FragmentHomeBinding mBinding;

    private ArrayAdapter<BTDevice> mListAdapter;
    private HomeViewModel mViewModel;

    private ArrayList<BTDevice> mDevList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        mViewModel.refreshDevices();
        mViewModel.getDevs().observe(getViewLifecycleOwner(), this::updateDeviceList);
        mDevList = mViewModel.getDevs().getValue();

//        mBinding.getRoot().appBarDeviceListing.addDevice.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                mDevList);
        setListAdapter(mListAdapter);

        /* set up the 'add device' button */
        FloatingActionButton refreshDevBut = requireView().findViewById(R.id.refresh_devices);
        refreshDevBut.setOnClickListener(view1 -> {
            mViewModel.refreshDevices();
        });
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        BTDevice clicked = mDevList.get(position);
        Intent toCmdLine = new Intent(BTControl.getContext(), SendCommandsActivity.class);
        toCmdLine.putExtra(SendCommandsActivity.BTDEV_EXTRA_KEY, clicked.get());
        startActivity(toCmdLine);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    // observer callback for whenever the device list changes
    private void updateDeviceList(ArrayList<BTDevice> deviceList) {
        mDevList = deviceList;
        mListAdapter.clear();
        mListAdapter.addAll(deviceList);
        mListAdapter.notifyDataSetChanged();
    }
}