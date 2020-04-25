package com.hifnawy.decideForMe;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hifnawy.spinningWheelLib.SpinningWheelView;
import com.hifnawy.spinningWheelLib.model.MarkerPosition;
import com.hifnawy.spinningWheelLib.model.WheelSection;
import com.hifnawy.spinningWheelLib.model.WheelTextSection;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;
import com.skydoves.colorpickerview.sliders.AlphaSlideBar;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WheelSettingsActivity extends AppCompatActivity {
    final int MAXIMUM_OPTION_CHAR_LENGTH = 20;

    final List<WheelSection> wheelSections = new ArrayList<>();
    SpinningWheelView wheelView;
    TextView textSizeTV;
    SeekBar textSizeSB;
    TextView optionRepeatTV;
    SeekBar optionRepeatSB;
    TextView spinTimeTV;
    SeekBar spinTimeSB;
    RecyclerView optionsRV;
    CustomListViewAdapter itemsAdapter;
    int speed = -1;

    SharedPreferences sharedpreferences;

    int foregroundColor = -1;
    int backgroundColor = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel_settings);

        textSizeTV = findViewById(R.id.textSizeTV);
        textSizeSB = findViewById(R.id.textSizeSB);

        optionRepeatTV = findViewById(R.id.optionRepeatTV);
        optionRepeatSB = findViewById(R.id.optionRepeatSB);

        spinTimeTV = findViewById(R.id.spinTimeTV);
        spinTimeSB = findViewById(R.id.spinTimeSB);

        wheelView = findViewById(R.id.previewPrizeWheel);

        optionsRV = findViewById(R.id.optionsLV);
        itemsAdapter = new CustomListViewAdapter(this);

        itemsAdapter.setSizeChangedListener(new ListViewAdapterSizeChangedListner() {
            @Override
            public void onSizeChanged(int size) {
                if (size >= 8) {
                    optionRepeatSB.setProgress(0);
                    optionRepeatSB.setEnabled(false);
                    Toast.makeText(WheelSettingsActivity.this,
                            "number of items is more than 8\noption repeat will be disabled ", Toast.LENGTH_LONG).show();
                } else {
                    optionRepeatSB.setEnabled(true);
                }
            }
        });

        itemsAdapter.setItemClickListener(new RecyclerViewItemClickListner() {
            @Override
            public void onItemClicked(int position) {
                editOption(position);
            }

            @Override
            public void onItemLongClicked(int position) {
                showSnackbar(((WheelTextSection) itemsAdapter.getItem(position)).getText(),
                        ((WheelTextSection) itemsAdapter.getItem(position)).getForegroundColor(),
                        ((WheelTextSection) itemsAdapter.getItem(position)).getBackgroundColor());

                itemsAdapter.remove(position);

                List<WheelSection> newWheelSections = new ArrayList<>();

                for (WheelSection ws : itemsAdapter.getItems()) {
                    newWheelSections.add(ws);
                }

                int preferredSize = newWheelSections.size() * (optionRepeatSB.getProgress() + 1);

                int difference = preferredSize - newWheelSections.size();

                for (int i = 0; i < difference; i++) {
                    newWheelSections.add(newWheelSections.get(i % newWheelSections.size()));
                }

                wheelView.setWheelSections(newWheelSections);
                wheelView.generateWheel();
            }
        });

        sharedpreferences = getSharedPreferences("ActivityState", Context.MODE_PRIVATE);

        sharedpreferences.edit().clear().apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        int savedTextSize = sharedpreferences.getInt("savedTextSize", -1);
        int savedOptionRepeat = sharedpreferences.getInt("savedOptionRepeat", -1);
        int savedSpinTime = sharedpreferences.getInt("savedSpinTime", -1);

        String savedWheelSections = sharedpreferences.getString("savedWheelSections", null);

        if (savedWheelSections == null) {
            wheelSections.clear();
            itemsAdapter.clear();
            for (int i = 0; i < 6; i++) {
                int bR = new Random().nextInt(255);
                int bG = new Random().nextInt(255);
                int bB = new Random().nextInt(255);

                int fR = ~bR;
                int fG = ~bG;
                int fB = ~bB;
                WheelTextSection wts = new WheelTextSection("item #" + (i + 1))
                        .setSectionForegroundColor(Color.rgb(fR, fG, fB))
                        .setSectionBackgroundColor(Color.rgb(bR, bG, bB));
                wheelSections.add(wts);
                itemsAdapter.add(wts);

            }
            wheelView.setWheelSections(wheelSections);
        } else {
            List<WheelTextSection> savedWheelTextSections = new Gson().fromJson(savedWheelSections, new TypeToken<ArrayList<WheelTextSection>>() {
            }.getType());

            itemsAdapter.clear();

            for (WheelSection ws : savedWheelTextSections) {
                itemsAdapter.add(ws);
            }
        }

        //Init wheelView and set parameters
        wheelView.setIsPreview(true);

        wheelView.setTickVibrations(true);

        wheelView.setMarkerPosition(MarkerPosition.TOP);

        wheelView.setWheelBorderLineColor(R.color.white);
        wheelView.setWheelBorderLineThickness(10);

        wheelView.setWheelSeparatorLineColor(R.color.white);
        wheelView.setWheelSeparatorLineThickness(1);

        if (savedTextSize == -1) {
            textSizeTV.setText(wheelView.getGlobalTextSize() + " px");
            textSizeSB.setProgress((wheelView.getGlobalTextSize() - 1));
        } else {
            textSizeTV.setText((savedTextSize + 1) + " px");
            textSizeSB.setProgress(savedTextSize);

            wheelView.setGlobalTextSize((savedTextSize + 1));
        }
        textSizeSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textSizeTV.setText((progress + 1) + " px");
                wheelView.setGlobalTextSize(progress + 1);
                wheelView.generateWheel();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (savedOptionRepeat == -1) {
            optionRepeatTV.setText("1 time");
            optionRepeatSB.setProgress(0);
        } else {
            optionRepeatTV.setText((savedOptionRepeat + 1) + ((savedOptionRepeat == 0) ? " time" : " times"));
            optionRepeatSB.setProgress(savedOptionRepeat);
        }
        optionRepeatSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                optionRepeatTV.setText((progress + 1) + ((progress == 0) ? " time" : " times"));

                List<WheelSection> newWheelSections = new ArrayList<>();

                for (WheelSection ws : itemsAdapter.getItems()) {
                    newWheelSections.add(ws);
                }

                if (newWheelSections.size() < 8) {
                    int preferredSize = newWheelSections.size() * (progress + 1);

                    int difference = preferredSize - newWheelSections.size();

                    for (int i = 0; i < difference; i++) {
                        newWheelSections.add(newWheelSections.get(i % newWheelSections.size()));
                    }

                    wheelView.setWheelSections(newWheelSections);
                    wheelView.generateWheel();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (savedSpinTime == -1) {
            spinTimeTV.setText(getResources().getStringArray(R.array.spin_time_values)[2]);
            spinTimeSB.setProgress(2);
        } else {
            spinTimeTV.setText(getResources().getStringArray(R.array.spin_time_values)[savedSpinTime]);
            spinTimeSB.setProgress(savedSpinTime);
        }
        spinTimeSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                spinTimeTV.setText(getResources().getStringArray(R.array.spin_time_values)[progress]);
                speed = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                wheelView.setFlingVelocityDampening(1.01f);
                wheelView.flingWheel((speed + 1) * 1000, 1000 + (1000 * (int) Math.pow(2, speed)), (new Random().nextFloat() > 0.5));
            }
        });

        // use a linear layout manager
        optionsRV.setLayoutManager(new LinearLayoutManager(this));

        optionsRV.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(WheelSettingsActivity.this, R.anim.layout_animation_fall_down));

        optionsRV.setAdapter(itemsAdapter);

        wheelView.generateWheel();
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putInt("savedTextSize", textSizeSB.getProgress());
        editor.putInt("savedOptionRepeat", optionRepeatSB.getProgress());
        editor.putInt("savedSpinTime", spinTimeSB.getProgress());

        String wheelSectionsJson = "";

        wheelSectionsJson = new Gson().toJson(itemsAdapter.getItems());

        editor.putString("savedWheelSections", wheelSectionsJson);

        editor.apply();

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.aboutMenuItem:
                Dialog dialog = new Dialog(this);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.about_dialog);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.setCanceledOnTouchOutside(true);

                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void editOption(int index) {
        final int position = index;
        final Dialog dialog = new Dialog(WheelSettingsActivity.this);
        dialog.setContentView(R.layout.add_option_dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView dialogTitleTextView = dialog.findViewById(R.id.dialogTitleTextView);

        dialogTitleTextView.setText("Edit Option");

        Button okButton = dialog.findViewById(R.id.okBtn);
        Button deleteButton = dialog.findViewById(R.id.deleteBtn);
        Button cancelButton = dialog.findViewById(R.id.cancelBtn);

        final EditText optionEditText = dialog.findViewById(R.id.optionEditText);

        final TextView previewTextView = dialog.findViewById(R.id.previewTextView);

        final ColorPickerView foregroundColorPickerView = dialog.findViewById((R.id.foregroundColorPickerView));
        AlphaSlideBar foregroundAlphaSlideBar = dialog.findViewById(R.id.foregroundAlphaSlideBar);
        BrightnessSlideBar foregroundBrightnessSlideBar = dialog.findViewById(R.id.foregroundBrightnessSlideBar);

        foregroundColorPickerView.attachAlphaSlider(foregroundAlphaSlideBar);
        foregroundColorPickerView.attachBrightnessSlider(foregroundBrightnessSlideBar);

        final ColorPickerView backgroundColorPickerView = dialog.findViewById((R.id.backgroundColorPickerView));
        AlphaSlideBar backgroundAlphaSlideBar = dialog.findViewById(R.id.backgroundAlphaSlideBar);
        BrightnessSlideBar backgroundBrightnessSlideBar = dialog.findViewById(R.id.backgroundBrightnessSlideBar);

        backgroundColorPickerView.attachAlphaSlider(backgroundAlphaSlideBar);
        backgroundColorPickerView.attachBrightnessSlider(backgroundBrightnessSlideBar);

        optionEditText.setHint(((WheelTextSection) itemsAdapter.getItem(position)).getText());

        previewTextView.setText(((WheelTextSection) itemsAdapter.getItem(position)).getText());
        previewTextView.setTextColor(((WheelTextSection) itemsAdapter.getItem(position)).getForegroundColor());
        previewTextView.setBackgroundColor(((WheelTextSection) itemsAdapter.getItem(position)).getBackgroundColor());


        foregroundColorPickerView.post(new Runnable() {
            @Override
            public void run() {
                int radius = foregroundColorPickerView.getWidth() / 2;
                Point point = color2Point(((WheelTextSection) itemsAdapter.getItem(position)).getForegroundColor(), radius);
                foregroundColorPickerView.setSelectorPoint(point.x, point.y);
            }
        });

        backgroundColorPickerView.post(new Runnable() {
            @Override
            public void run() {
                int radius = backgroundColorPickerView.getHeight() / 2;
                Point point = color2Point(((WheelTextSection) itemsAdapter.getItem(position)).getBackgroundColor(), radius);
                backgroundColorPickerView.setSelectorPoint(point.x, point.y);
            }
        });


        foregroundColorPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(int color, boolean fromUser) {
                if (fromUser) {
                    previewTextView.setTextColor(color);
                }
            }
        });

        backgroundColorPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(int color, boolean fromUser) {
                if (fromUser) {
                    previewTextView.setBackgroundColor(color);
                }
            }
        });

        optionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (optionEditText.getText().length() > MAXIMUM_OPTION_CHAR_LENGTH) {
                    optionEditText.setText(optionEditText.getText().toString().substring(0, optionEditText.getText().toString().length() - 1));
                    optionEditText.setSelection(optionEditText.getText().length());
                }

                previewTextView.setText(optionEditText.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackbar(((WheelTextSection) itemsAdapter.getItem(position)).getText(),
                        ((WheelTextSection) itemsAdapter.getItem(position)).getForegroundColor(),
                        ((WheelTextSection) itemsAdapter.getItem(position)).getBackgroundColor());

                itemsAdapter.remove(position);

                List<WheelSection> newWheelSections = new ArrayList<>();

                for (WheelSection ws : itemsAdapter.getItems()) {
                    newWheelSections.add(ws);
                }

                int preferredSize = newWheelSections.size() * (optionRepeatSB.getProgress() + 1);

                int difference = preferredSize - newWheelSections.size();

                for (int i = 0; i < difference; i++) {
                    newWheelSections.add(newWheelSections.get(i % newWheelSections.size()));
                }

                wheelView.setWheelSections(newWheelSections);
                wheelView.generateWheel();

                dialog.dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = previewTextView.getText().toString();

                itemsAdapter.updateItem(position, new WheelTextSection(s)
                        .setSectionForegroundColor(foregroundColorPickerView.getColor())
                        .setSectionBackgroundColor(backgroundColorPickerView.getColor()));


                List<WheelSection> newWheelSections = new ArrayList<>();

                for (WheelSection ws : itemsAdapter.getItems()) {
                    newWheelSections.add(ws);
                }

                wheelView.setWheelSections(newWheelSections);
                wheelView.generateWheel();

                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
    }

    public void shuffleWheel(View view) {
        itemsAdapter.shuffle();

        wheelView.setWheelSections(itemsAdapter.getItems());

        wheelView.generateWheel();
    }

    public void addOption(View view) {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.add_option_dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView dialogTitleTextView = dialog.findViewById(R.id.dialogTitleTextView);

        dialogTitleTextView.setText("New Option");

        Button okButton = dialog.findViewById(R.id.okBtn);
        Button deleteButton = dialog.findViewById(R.id.deleteBtn);
        Button cancelButton = dialog.findViewById(R.id.cancelBtn);

        deleteButton.setVisibility(View.GONE);

        final EditText optionEditText = dialog.findViewById(R.id.optionEditText);

        final TextView previewTextView = dialog.findViewById(R.id.previewTextView);

        final ColorPickerView foregroundColorPickerView = dialog.findViewById((R.id.foregroundColorPickerView));
        final AlphaSlideBar foregroundAlphaSlideBar = dialog.findViewById(R.id.foregroundAlphaSlideBar);
        final BrightnessSlideBar foregroundBrightnessSlideBar = dialog.findViewById(R.id.foregroundBrightnessSlideBar);

        foregroundColorPickerView.attachAlphaSlider(foregroundAlphaSlideBar);
        foregroundColorPickerView.attachBrightnessSlider(foregroundBrightnessSlideBar);

        final ColorPickerView backgroundColorPickerView = dialog.findViewById((R.id.backgroundColorPickerView));
        final AlphaSlideBar backgroundAlphaSlideBar = dialog.findViewById(R.id.backgroundAlphaSlideBar);
        final BrightnessSlideBar backgroundBrightnessSlideBar = dialog.findViewById(R.id.backgroundBrightnessSlideBar);

        backgroundColorPickerView.attachAlphaSlider(backgroundAlphaSlideBar);
        backgroundColorPickerView.attachBrightnessSlider(backgroundBrightnessSlideBar);

        backgroundBrightnessSlideBar.post(new Runnable() {
            @Override
            public void run() {
                backgroundBrightnessSlideBar.setSelectorPosition(0.1f);
                backgroundColorPickerView.fireColorListener(0xff000000, true);
            }
        });

        previewTextView.setTextColor(ContextCompat.getColor(this, R.color.white));
        previewTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.black));

        foregroundColorPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(int color, boolean fromUser) {
                if (fromUser) {
                    foregroundColor = color;
                    previewTextView.setTextColor(color);
                }
            }
        });

        backgroundColorPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(int color, boolean fromUser) {
                Log.d("mn3m", "background color: " + color);
                if (fromUser) {
                    backgroundColor = color;
                    previewTextView.setBackgroundColor(color);
                }
            }
        });

        optionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (optionEditText.getText().length() > MAXIMUM_OPTION_CHAR_LENGTH) {
                    optionEditText.setText(optionEditText.getText().toString().substring(0, optionEditText.getText().toString().length() - 1));
                    optionEditText.setSelection(optionEditText.getText().length());
                }

                previewTextView.setText(optionEditText.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = previewTextView.getText().toString();

                itemsAdapter.add(new WheelTextSection(s)
                        .setSectionForegroundColor(foregroundColor)
                        .setSectionBackgroundColor(backgroundColor));

                List<WheelSection> newWheelSections = new ArrayList<>();

                for (WheelSection ws : itemsAdapter.getItems()) {
                    newWheelSections.add(ws);
                }

                int preferredSize = newWheelSections.size() * (optionRepeatSB.getProgress() + 1);

                int difference = preferredSize - newWheelSections.size();

                for (int i = 0; i < difference; i++) {
                    newWheelSections.add(newWheelSections.get(i % newWheelSections.size()));
                }

                wheelView.setWheelSections(newWheelSections);
                wheelView.generateWheel();

                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
    }

    public void clearOptions(View view) {
        wheelSections.clear();
        itemsAdapter.clear();

        wheelView.setWheelSections(itemsAdapter.getItems());
        wheelView.generateWheel();
    }

    public void spinWheel(final View view) {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.button_fade_out);
        fadeOut.setDuration(100);
        view.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation fadeIn = AnimationUtils.loadAnimation(WheelSettingsActivity.this, R.anim.button_fade_in);
                fadeIn.setDuration(100);
                view.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Bundle extra = new Bundle();

        extra.putSerializable("sections", (Serializable) itemsAdapter.getItems());

        extra.putInt("textSize", textSizeSB.getProgress());
        extra.putInt("optionRepeat", (optionRepeatSB.getProgress() + 1));
        extra.putInt("spinTime", spinTimeSB.getProgress());

        Intent intent = new Intent(WheelSettingsActivity.this, MainActivity.class);
        intent.putExtra("extra", extra);
        startActivity(intent);
    }

    private void showSnackbar(String text, int foregroundColor, int backgroundColor) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rootLayout), "", Snackbar.LENGTH_SHORT);

        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();

        ((Snackbar.SnackbarLayout) snackbar.getView()).removeAllViews();

        snackbar.getView().setBackgroundColor(backgroundColor);

        View snackbarView = WheelSettingsActivity.this.getLayoutInflater().inflate(R.layout.snackbar_view, null);

        View snackBarRootView = snackbarView.findViewById(R.id.snackbarRootView);

        TextView deletedItemNameTV = snackbarView.findViewById(R.id.itemNameTV);

        snackBarRootView.setBackgroundColor(backgroundColor);

        deletedItemNameTV.setTextColor(foregroundColor);

        deletedItemNameTV.setText(text);

        snackbarLayout.addView(snackbarView, 0);

        snackbar.show();
    }

    private Point color2Point(int color, float radius) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        double x = hsv[1] * Math.cos(Math.toRadians(hsv[0]));
        double y = hsv[1] * Math.sin(Math.toRadians(hsv[0]));

        int pointX = (int) ((x + 1) * radius);
        int pointY = (int) ((1 - y) * radius);
        return new Point(pointX, pointY);
    }
}