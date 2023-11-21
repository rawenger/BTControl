package edu.ucsb.ryanwenger.btcontrol;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class SnippetStore {
    private static final String PREFS_FILE = "SnippetsList";
    private static final String NUM_SNIPS_LABEL = "numSnippets";
    private static final String SNIP_LABEL_PREFIX = "SNIP#";

    private ArrayList<String> snippets = null;

    private static SnippetStore mInstance;

    public static SnippetStore getInstance() {
        if (mInstance == null)
            mInstance = new SnippetStore();
        return mInstance;
    }

    private SnippetStore() {
        if (snippets != null)
            return;

        reload();
    }

    public void reload() {
        snippets = new ArrayList<>();
        SharedPreferences prefs = getPrefs();
        int numSnips = prefs.getInt(NUM_SNIPS_LABEL, 0);

        for (int i = 0; i < numSnips; i++) {
            String key = SNIP_LABEL_PREFIX + i;
            snippets.add(prefs.getString(key, ""));
        }
    }

    public ArrayList<String> loadSnippets() {
        assert snippets != null;
        return snippets;
    }

    public void storeSnippets(ArrayList<String> snippetNames) {
        snippets = snippetNames;
        storeSnippets();
    }

    public void addSnippet(String snippetName) {
        if (snippets.contains(snippetName))
            return;

        snippets.add(snippetName);
        int numSnips = snippets.size();
        String newKey = SNIP_LABEL_PREFIX + (numSnips - 1);

        getPrefs().edit()
                .putInt(NUM_SNIPS_LABEL, numSnips)
                .putString(newKey, snippetName)
                .apply();

        saveSnip(snippetName, "");
    }

    public void removeSnippet(String snippetName) {
        snippets.remove(snippetName);
        storeSnippets();

        getPrefs(snippetName).edit()
                .clear()
                .apply();

        // TODO: remove the deleted snippet file from disk
    }

    private void storeSnippets() {
        SharedPreferences.Editor prefs = getPrefs().edit();

        int numSnips = snippets.size();
        prefs.putInt(NUM_SNIPS_LABEL, numSnips);

        for (int i = 0; i < numSnips; i++) {
            String key = SNIP_LABEL_PREFIX + i;
            prefs.putString(key, snippets.get(i));
        }

        prefs.apply();
    }

    private static final String SNIPPET_DATA_KEY = "snipContents";

    public void saveSnip(String name, String contents) {
        getPrefs(name).edit()
                .putString(SNIPPET_DATA_KEY, contents)
                .apply();
    }

    public String loadSnip(String name) {
        return getPrefs(name).getString(SNIPPET_DATA_KEY, "");
    }

    private SharedPreferences getPrefs() {
        return getPrefs(PREFS_FILE);
    }

    private SharedPreferences getPrefs(String name) {
        return BTControl.getContext()
                .getSharedPreferences(name, Context.MODE_PRIVATE);
    }
}
