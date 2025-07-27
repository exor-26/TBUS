package com.example.tbus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnPage1, btnPage2, btnPage3;
    private ImageView homeIcon, historyIcon, profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        viewPager = findViewById(R.id.viewPager);
        btnPage1 = findViewById(R.id.btnPage1);
        btnPage2 = findViewById(R.id.btnPage2);
        btnPage3 = findViewById(R.id.btnPage3);
        homeIcon = findViewById(R.id.homeIcon);
        historyIcon = findViewById(R.id.history_icon);
        profileIcon = findViewById(R.id.profileIcon);

        // Set up ViewPager2 Adapter
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new FirstPageFragment());
        fragments.add(new HistoryFragment());
        fragments.add(new ThirdFragment());
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragments);
        viewPager.setAdapter(adapter);

        Intent intent = getIntent();
        int fragmentIndex = intent.getIntExtra("fragment", 0); // Default to 0 (FirstPageFragment)
        viewPager.setCurrentItem(fragmentIndex, false); // Navigate to the specified fragment

        // Button Navigation
        btnPage1.setOnClickListener(v -> viewPager.setCurrentItem(0));
        btnPage2.setOnClickListener(v -> viewPager.setCurrentItem(1));
        btnPage3.setOnClickListener(v -> viewPager.setCurrentItem(2));

        // Bottom Icon Navigation
        homeIcon.setOnClickListener(v -> viewPager.setCurrentItem(0));
        historyIcon.setOnClickListener(v -> viewPager.setCurrentItem(1));
        profileIcon.setOnClickListener(v -> viewPager.setCurrentItem(2));

        // Page Change Listener for Syncing Button and Icon State
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                highlightButton(position);
            }
        });
    }

    // Highlight active button/icon
    private void highlightButton(int position) {
        // Disable buttons and adjust alpha for icons
        btnPage1.setEnabled(position != 0);
        btnPage2.setEnabled(position != 1);
        btnPage3.setEnabled(position != 2);

        // Set icons to the correct state based on the selected page
        homeIcon.setAlpha(position == 0 ? 1.0f : 0.5f);
        historyIcon.setAlpha(position == 1 ? 1.0f : 0.5f);
        profileIcon.setAlpha(position == 2 ? 1.0f : 0.5f);

        // Switch the icon to "uc_" versions when active
        if (position == 0) {
            homeIcon.setImageResource(R.drawable.uc_home);
            historyIcon.setImageResource(R.drawable.ic_bookmark); // Keep default for others
            profileIcon.setImageResource(R.drawable.ic_profile); // Keep default for others
        } else if (position == 1) {
            homeIcon.setImageResource(R.drawable.ic_home); // Default for others
            historyIcon.setImageResource(R.drawable.uc_bookmark);
            profileIcon.setImageResource(R.drawable.ic_profile); // Keep default for others
        } else if (position == 2) {
            homeIcon.setImageResource(R.drawable.ic_home); // Default for others
            historyIcon.setImageResource(R.drawable.ic_bookmark); // Default for others
            profileIcon.setImageResource(R.drawable.uc_profile);
        }
    }
}