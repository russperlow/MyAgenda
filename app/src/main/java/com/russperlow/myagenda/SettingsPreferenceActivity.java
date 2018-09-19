package com.russperlow.myagenda;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;

import java.util.Map;

public class SettingsPreferenceActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.settings_activity);

        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsPreferenceFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(final Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            initPreferences();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(findPreference(key), key);
        }

        private void initPreferences(){
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            Map<String, ?> preferences = sharedPreferences.getAll();
            for(String key : preferences.keySet()){
                try{
                    findPreference(key).setSummary(sharedPreferences.getString(key, "Default"));
                }
                catch (Exception e){
                    Log.d("SETTINGS ACTIVITY", e.getMessage());
                }
            }
        }

        private void updatePreference(Preference preference, String key){
            if(preference == null)
                return;
            try{
                SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
                preference.setSummary(sharedPreferences.getString(key, "Default"));
            }
            catch (Exception e){
                Log.d("SETTINGS ACTIVITY", e.getMessage());
            }
        }
    }
}
