package edu.ucsb.ryanwenger.btcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

public class SnippetEditor extends AppCompatActivity {
    public static final String EXTRA_SNIPPET_NAME = "SnippetEditor.name";

    private @NonNull String mSnipName = "";
    private SnippetStore mSnippetStore;

    private EditText mTextEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_editor);

        Intent trigger = getIntent();
        String name = trigger.getStringExtra(EXTRA_SNIPPET_NAME);
        if (name == null || name.equals("")) {
            finish();
            return;
        }

        mSnipName = name;

        mSnippetStore = SnippetStore.getInstance();
        String snipContents = mSnippetStore.loadSnip(mSnipName);

        mTextEditor = findViewById(R.id.snip_editor);
        mTextEditor.setText(snipContents);
        mTextEditor.bringToFront();

        // TODO: create EditText
        //  create 'run', 'save', 'cancel' buttons

    }

    public void onSave(View view) {
        // save but dont exit the activity
        // TODO: why aren't snippets being saved at all??
        mSnippetStore.saveSnip(mSnipName, mTextEditor.getText().toString());
        Snackbar.make(view, "Saved", Snackbar.LENGTH_SHORT).show();
    }

    public void onRun(View view) {
        // note: run WITHOUT saving in case changes want to be made
    }

    public void onCancel(View view) {
        // TODO: prompt to save if contents have been edited!

        finish();
    }
}