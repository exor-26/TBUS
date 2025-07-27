package com.example.tbus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class FirstPageFragment extends Fragment {

    private String selectedDate = ""; // Holds the selected date
    private EditText fromField, toField;
    private ImageView calendarIcon, trajectoryIcon;
    private TextView dayText, dateText, monthText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.viewpage1, container, false);

        // Initialize views
        fromField = view.findViewById(R.id.from_field);
        toField = view.findViewById(R.id.to_field);
        trajectoryIcon = view.findViewById(R.id.trajectory_icon);
        calendarIcon = view.findViewById(R.id.calendar_icon);

        dayText = view.findViewById(R.id.day_text);
        dateText = view.findViewById(R.id.date_text);
        monthText = view.findViewById(R.id.month_text);

        view.findViewById(R.id.today_text).setOnClickListener(v -> setToday());
        view.findViewById(R.id.tomorrow_text).setOnClickListener(v -> setTomorrow());

        // Set swap button listener
        trajectoryIcon.setOnClickListener(v -> swapFields());

        view.findViewById(R.id.search_button).setOnClickListener(v -> {
            String from = fromField.getText().toString().trim();
            String to = toField.getText().toString().trim();

            if (selectedDate.isEmpty()) {
                setCurrentDate(); // Automatically set the current date if none is selected
                Toast.makeText(getActivity(), "No date selected. Using today's date: " + selectedDate, Toast.LENGTH_SHORT).show();
            }

            if (!from.isEmpty() && !to.isEmpty() && !selectedDate.isEmpty()) {
                Intent intent = new Intent(getActivity(), SearchRoutesActivity.class);
                intent.putExtra("fromField", from);
                intent.putExtra("toField", to);
                intent.putExtra("selectedDate", selectedDate); // Pass the selected date
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        setCurrentDate();

        calendarIcon.setOnClickListener(v -> openDatePicker());

        return view;
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;

                    updateCalendarDisplay(selectedYear, selectedMonth, selectedDay);
                    Toast.makeText(getActivity(), "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();
                },
                year,
                month,
                day
        );

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void updateCalendarDisplay(int year, int month, int day) {
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year, month, day);

        String dayName = selectedCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, getResources().getConfiguration().locale);
        String monthName = selectedCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, getResources().getConfiguration().locale);

        dayText.setText(dayName);
        dateText.setText(String.valueOf(day));
        monthText.setText(monthName.toUpperCase());
    }

    private void setToday() {
        Calendar calendar = Calendar.getInstance();
        selectedDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);

        updateCalendarDisplay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void setTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        selectedDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);

        updateCalendarDisplay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void setCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        selectedDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);

        updateCalendarDisplay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void swapFields() {
        String from = fromField.getText().toString();
        String to = toField.getText().toString();

        fromField.setText(to);
        toField.setText(from);

        Toast.makeText(getActivity(), "Swapped From and To", Toast.LENGTH_SHORT).show();
    }
}