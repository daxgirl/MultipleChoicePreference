package com.wubydax.multiplechoicepreference;

import android.os.Bundle;

/**
 * Created by Anna Berkovitch on 03/03/2016.
 */
public class PreferenceFragment extends android.preference.PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
