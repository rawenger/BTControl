package edu.ucsb.ryanwenger.btcontrol.ui.snippets;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import edu.ucsb.ryanwenger.btcontrol.SnippetStore;

public class SnippetsViewModel extends ViewModel {
    private final SnippetStore mSnipStore;

    private final MutableLiveData<ArrayList<String>> mSnips;

    public SnippetsViewModel() {
        mSnipStore = SnippetStore.getInstance();
        mSnips = new MutableLiveData<>(mSnipStore.loadSnippets());
    }

    public LiveData<ArrayList<String>> getSnips() {
        return mSnips;
    }

    // null return value indicates success here; non-null is error message
    public @Nullable String addSnip(String name) {
        ArrayList<String> val = mSnips.getValue();
        if (val == null)
            val = new ArrayList<>();
        else if (val.contains(name))
            return "name '" + name + "' is taken";

        val.add(name);
        mSnips.setValue(val);
        mSnipStore.addSnippet(name);

        return null;
    }

    public void deleteSnip(String name) {
        ArrayList<String> val = mSnips.getValue();
        if (val == null)
            return;

        val.remove(name);
        mSnipStore.removeSnippet(name);
        mSnips.setValue(val);
    }
}