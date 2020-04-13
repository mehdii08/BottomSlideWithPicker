package com.test.bottomslidewithpicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SlidingUpPanelLayout slidingUpPanelLayout;
    EasyPickerView myPicker;
    int selectedIndex;
    AppCompatButton clickMe, submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slidingUpPanelLayout = findViewById(R.id.side_up_panel);
        myPicker = findViewById(R.id.picker);
        clickMe = findViewById(R.id.click_me);
        submit = findViewById(R.id.submit);
        selectedIndex = 0;

        final ArrayList<String> list = new ArrayList<>();
        list.add("item 1");
        list.add("item 2");
        list.add("item 3");
        list.add("item 4");
        list.add("item 5");
        list.add("item 6");
        list.add("item 7");
        list.add("item 8");

        myPicker.setDataList(list);

        myPicker.setOnScrollChangedListener(new EasyPickerView.OnScrollChangedListener() {
            public void onScrollChanged(int curIndex) {
            }

            public void onScrollFinished(int curIndex) {
                selectedIndex = curIndex;
            }
        });

        clickMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                Toast.makeText(MainActivity.this, "selected item : " + list.get(selectedIndex), Toast.LENGTH_SHORT).show();

            }
        });

    }
}
