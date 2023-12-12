package edu.ucsb.ryanwenger.btcontrol.ui.snippets;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import edu.ucsb.ryanwenger.btcontrol.BTControl;
import edu.ucsb.ryanwenger.btcontrol.R;
import edu.ucsb.ryanwenger.btcontrol.SnippetEditor;
import edu.ucsb.ryanwenger.btcontrol.SnippetRunnerActivity;
import edu.ucsb.ryanwenger.btcontrol.databinding.FragmentSnippetsBinding;

public class SnippetsFragment extends ListFragment {

    private FragmentSnippetsBinding binding;

    private SnippetsViewModel mViewModel;

    private ArrayAdapter<String> mListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        mViewModel = new ViewModelProvider(this).get(SnippetsViewModel.class);

        binding = FragmentSnippetsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mViewModel.getSnips().observe(getViewLifecycleOwner(), this::updateSnippetList);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,  mViewModel.getSnips().getValue());

        setListAdapter(mListAdapter);

        registerForContextMenu(getListView());

        /* set up the 'create snippet' button */
        FloatingActionButton createSnip = requireView().findViewById(R.id.new_snippet);
        createSnip.setOnClickListener(this::onCreateSnipClick);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String clicked = mListAdapter.getItem(position);
        if (clicked == null)
            return;

        PopupMenu menu = new PopupMenu (getContext(), v);
        menu.setOnMenuItemClickListener (item -> {
            int id1 = item.getItemId();

            if (id1 == R.id.item_run) {

                SnippetRunnerActivity
                        .launch(SnippetsFragment.this.requireActivity(),
                                clicked, null);

            } else if (id1 == R.id.item_edit) {

                editSnip(clicked);

            } else if (id1 == R.id.item_rename) {

                // TODO

            } else if (id1 == R.id.item_delete) {

                AlertDialog.Builder builder = new AlertDialog.Builder(l.getContext());
                builder.setTitle("Delete snippet '" + clicked + "'?")
                        .setMessage("This action cannot be undone!");

                builder.setPositiveButton("Delete",
                        (dialog, which) -> {
                            mViewModel.deleteSnip(clicked);
                            mListAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel",
                                (dialog, which) -> dialog.cancel());

                builder.show();

            } else {
                return false;
            }

            return true;
        });
        menu.inflate(R.menu.snippet_popup);
        menu.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateSnippetList(ArrayList<String> snips) {
        mListAdapter.notifyDataSetChanged();
    }

    private void editSnip(String name) {
        Intent toSnipEditor = new Intent(BTControl.getContext(), SnippetEditor.class);
        toSnipEditor.putExtra(SnippetEditor.EXTRA_SNIPPET_NAME, name);
        SnippetsFragment.this.startActivity(toSnipEditor);
    }

    private void onCreateSnipClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Create Snippet");

        // Set up the input
        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        input.setText("Snippet");
        builder.setView(input);

        final String[] newSnipName = new String[1];

        // Set up the buttons
        builder.setPositiveButton("OK",
                (dialog, which) -> {
                    newSnipName[0] = input.getText().toString();
                    String failure = mViewModel.addSnip(newSnipName[0]);

                    if (failure != null) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                        builder1.setTitle("Unable to create new snippet: " + failure);
                        builder1.setPositiveButton("OK",
                                (dialog1, which1) -> dialog1.dismiss());

                        builder1.show();
                        return;
                    }

                    editSnip(newSnipName[0]);
                });

        builder.setNegativeButton("Cancel",
                (dialog, which) -> dialog.cancel());

        builder.show();
    }
}