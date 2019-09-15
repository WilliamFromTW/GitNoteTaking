package inmethod.gitnotetaking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;


public  class PreferencesSettings extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CustomPreferenceFragment.FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new CustomPreferenceFragment();
        }
        getSupportFragmentManager() .beginTransaction().replace(android.R.id.content,  fragment ,CustomPreferenceFragment.FRAGMENT_TAG).commit();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id==android.R.id.home){
            onBackPressed();
            return true;
        }
        return true;
    }
}

