package inmethod.gitnotetaking;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public  class CustomPreferenceFragment extends PreferenceFragmentCompat {

    public static final String FRAGMENT_TAG = "my_preference_fragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settingspreferences, rootKey);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        EditTextPreference GitAuthorName = (EditTextPreference) findPreference("GitAuthorName");
        GitAuthorName.setSummary(sharedPreferences.getString("GitAuthorName", ""));
        GitAuthorName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String yourString = o.toString();
                sharedPreferences.edit().putString("GitAuthorName", yourString).apply();
                GitAuthorName.setSummary(yourString);

                return true;
            }
        });

        EditTextPreference GitAuthorEmail = (EditTextPreference) findPreference("GitAuthorEmail");
        GitAuthorEmail.setSummary(sharedPreferences.getString("GitAuthorEmail", ""));
        GitAuthorEmail.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String yourString = o.toString();
                sharedPreferences.edit().putString("GitAuthorEmail", yourString).apply();
                GitAuthorEmail.setSummary(yourString);

                return true;
            }
        });

        EditTextPreference GitEditTextSize = (EditTextPreference) findPreference("GitEditTextSize");
        GitEditTextSize.setSummary(sharedPreferences.getString("GitEditTextSize", ""));
        GitEditTextSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String yourString = o.toString();
                sharedPreferences.edit().putString("GitEditTextSize", yourString).apply();
                GitEditTextSize.setSummary(yourString);

                return true;
            }
        });

    }


}
