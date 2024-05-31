package inmethod.gitnotetaking;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public  class CustomPreferenceFragment extends PreferenceFragmentCompat {

    public static final String FRAGMENT_TAG = "my_preference_fragment";
    public static final String TAG =MainActivity.TAG;

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

        ListPreference aSort = (ListPreference) findPreference("Sort");
        //aSort.setValue(sharedPreferences.getString("Sort", "11"));
        aSort.setDefaultValue(sharedPreferences.getString("Sort", "11"));
        aSort.setSummary( aSort.getEntries()[aSort.findIndexOfValue(sharedPreferences.getString("Sort", "11"))]);
        aSort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                int index = +((ListPreference)preference).findIndexOfValue(o.toString());
                aSort.setSummary(((ListPreference)preference).getEntries()[index]);
//                Log.d(TAG,"((ListPreference)preference).getValue()="+((ListPreference)preference).getValue());
  //              Log.d(TAG,"((ListPreference)preference).getEntry()="+((ListPreference)preference).getEntry());
                String yourString = o.toString();
                sharedPreferences.edit().putString("Sort", yourString).apply();
              //  aSort.setSummary( ((ListPreference)preference).getEntry());

                return true;
            }
        });

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
