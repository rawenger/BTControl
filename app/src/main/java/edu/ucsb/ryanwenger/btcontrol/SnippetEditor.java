package edu.ucsb.ryanwenger.btcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

public class SnippetEditor extends AppCompatActivity {
    public static final String EXTRA_SNIPPET_NAME = "SnippetEditor.name";

    private static final String BUNDLE_SNIPPET_NAME = EXTRA_SNIPPET_NAME;
    private static final String BUNDLE_SNIPPET_CONTENTS = "SnippetEditor.contents";

    private @NonNull String mSnipName = "";
    private SnippetStore mSnippetStore;

    private EditText mTextEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_editor);

        mSnippetStore = SnippetStore.getInstance();

        Intent trigger = getIntent();
        String name = trigger.getStringExtra(EXTRA_SNIPPET_NAME);
        CharSequence contents;

        // We could be launched from an intent or could be returning from 'Run' action
        // *without having saved the snippet's contents*
        if (name == null) {
            if (savedInstanceState == null)
                finish();

            String prevName = savedInstanceState.getString(BUNDLE_SNIPPET_NAME);
            if (prevName == null) {
                // error
                finish();
            }

            // returning from run action
            assert prevName != null;
            mSnipName = prevName;
            contents = savedInstanceState
                    .getCharSequence(BUNDLE_SNIPPET_CONTENTS);

        } else if (name.equals("")) {
            finish();
            return;
        } else {
            // launched from main activity
            mSnipName = name;
            contents = mSnippetStore.loadSnip(mSnipName);
        }

        mTextEditor = findViewById(R.id.snip_editor);
        mTextEditor.setText(contents);
        mTextEditor.bringToFront();
    }

    public void onSave(View view) {
        // save but dont exit the activity
        mSnippetStore.saveSnip(mSnipName, mTextEditor.getText().toString());
        Snackbar.make(view, "Saved", Snackbar.LENGTH_SHORT).show();
    }

    public void onRun(View view) {
        SnippetRunnerActivity.launch(this, mSnipName,
                mTextEditor.getText().toString());
    }

    public void onCancel(View view) {
        // prompt to save if contents have been edited!

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Save changes to '" + mSnipName +"' before exiting?");
        builder.setPositiveButton("Save",
                        (dialog, which) -> onSave(view))
                .setNegativeButton("Don't save",
                        (dialog, which) -> dialog.dismiss())
                .setOnDismissListener(dialog -> finish());

        builder.show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_SNIPPET_NAME, mSnipName);
        outState.putCharSequence(BUNDLE_SNIPPET_CONTENTS, mTextEditor.getText());
    }
}