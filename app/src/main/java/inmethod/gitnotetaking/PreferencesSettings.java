package inmethod.gitnotetaking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;


public  class PreferencesSettings extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CustomPreferenceFragment.FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new CustomPreferenceFragment();
        }
        getSupportFragmentManager() .beginTransaction().replace(android.R.id.content,  fragment ,CustomPreferenceFragment.FRAGMENT_TAG).commit();
    }

}

