package com.russperlow.myagenda;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, MainFragment.newInstance());
        transaction.commit();
    }

    // Display calendar for user to choose due date from
    public void showDatePicker(View view){
        DialogFragment fragment = new MainFragment.DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "date picker");
    }

    // Display the picker for user to choose the due time from
    public void showTimePicker(View view){
        DialogFragment fragment = new MainFragment.TimePickerFragment();
        fragment.show(getSupportFragmentManager(), "time picker");
    }

}
