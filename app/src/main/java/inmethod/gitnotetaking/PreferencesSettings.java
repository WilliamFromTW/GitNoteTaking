package inmethod.gitnotetaking;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;


public class PreferencesSettings extends FragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new CustomPreferenceFragment()).commit();

    }
}

class CustomPreferenceFragment extends PreferenceFragmentCompat {

    static String sPassword = "";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settingspreferences);
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        final EditTextPreference a = (EditTextPreference) findPreference("Password");
        sPassword = a.getText();

        a.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(!newValue.toString().equals("**********")) {
                    sPassword = newValue.toString();
                    a.setText(sPassword);
                    Log.d("gitnote", "new password = " + sPassword);
                    return true;
                }return false;
            }
        });

        a.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
              /*
                     @Override
                     public boolean onPreferenceClick(Preference preference, Object o) {
                         Log.e("", "New value is: " + o.toString());
                         // True to update the state of the Preference with the new value.
                         return true;
                     }*/

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        a.setText("**********");
                        Log.d("gitnote","current password = "+sPassword);
                        return true;
                    }

                });
    }

}
