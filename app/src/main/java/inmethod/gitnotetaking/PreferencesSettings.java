package inmethod.gitnotetaking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
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
import android.view.View;

import java.util.Objects;


public  class PreferencesSettings extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view =  findViewById(android.R.id.content);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CustomPreferenceFragment.FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new CustomPreferenceFragment();
        }
        getSupportFragmentManager() .beginTransaction().replace(android.R.id.content,  fragment ,CustomPreferenceFragment.FRAGMENT_TAG).commit();

        WindowCompat.enableEdgeToEdge(this.getWindow());
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {

            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;

        });
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

