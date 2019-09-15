package inmethod.gitnotetaking;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public  class CustomPreferenceFragment extends PreferenceFragmentCompat {

    private static String sPassword = "";
    public static final String FRAGMENT_TAG = "my_preference_fragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settingspreferences, rootKey);
    }

}
