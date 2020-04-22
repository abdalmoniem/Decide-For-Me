package com.hifnawy.decideForMe;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hifnawy.spinningwheellib.SpinningWheelView;
import com.hifnawy.spinningwheellib.model.MarkerPosition;
import com.hifnawy.spinningwheellib.model.WheelBitmapSection;
import com.hifnawy.spinningwheellib.model.WheelColorSection;
import com.hifnawy.spinningwheellib.model.WheelDrawableSection;
import com.hifnawy.spinningwheellib.model.WheelSection;
import com.hifnawy.spinningwheellib.model.WheelTextSection;
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
import androidx.core.graphics.drawable.DrawableCompat;

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
    ListView optionsLV;
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

        optionsLV = findViewById(R.id.optionsLV);
        itemsAdapter = new CustomListViewAdapter(this, new ArrayList<WheelSection>());

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

        wheelView.reInit();

        optionsLV.setAdapter(itemsAdapter);

        if (savedWheelSections == null) {
            wheelSections.clear();
            itemsAdapter.clear();
            for (int i = 0; i < 4; i++) {
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
//            wheelView.setWheelSections(savedWheelTextSections);

            itemsAdapter.clear();

            for (WheelSection ws : savedWheelTextSections) {
                itemsAdapter.add((WheelTextSection) ws);
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
            textSizeSB.setProgress(wheelView.getGlobalTextSize());
        } else {
            textSizeTV.setText(savedTextSize + " px");
            textSizeSB.setProgress(savedTextSize);

            wheelView.setGlobalTextSize(savedTextSize);
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
            optionRepeatTV.setText((savedOptionRepeat + 1) + " times");
            optionRepeatSB.setProgress(savedOptionRepeat);
        }
        optionRepeatSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                optionRepeatTV.setText((progress + 1) + " times");

                List<WheelSection> newWheelSections = new ArrayList<>();

                if (itemsAdapter.getItems().size() < 1) {
                    for (WheelSection ws : wheelSections) {
                        newWheelSections.add(ws);
                    }
                } else {
                    for (WheelSection ws : itemsAdapter.getItems()) {
                        newWheelSections.add(ws);
                    }
                }

                int preferredSize = newWheelSections.size() * (progress + 1);

                int difference = preferredSize - newWheelSections.size();

                for (int i = 0; i < difference; i++) {
                    newWheelSections.add(newWheelSections.get(i % newWheelSections.size()));
                }

                wheelView.setWheelSections(newWheelSections);
                wheelView.generateWheel();
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
                wheelView.setInitialFlingDampening(1);
                wheelView.setFlingVelocityDampening(1);
                wheelView.flingWheel(1000 + (1000 * (int) Math.pow(2, speed)), (new Random().nextFloat() > 0.5));
            }
        });

        optionsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                editOption(position);
            }
        });

        wheelView.generateWheel();
    }

    @Override
    protected void onPause() {
        wheelView.stopWheel();

        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putInt("savedTextSize", textSizeSB.getProgress());
        editor.putInt("savedOptionRepeat", optionRepeatSB.getProgress());
        editor.putInt("savedSpinTime", spinTimeSB.getProgress());

        String wheelSectionsJson = "";

//        if (itemsAdapter.getCount() < 1) {
//            wheelSectionsJson = new Gson().toJson(wheelSections);
//        } else {
        wheelSectionsJson = new Gson().toJson(itemsAdapter.getItems());
//        }

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

                previewTextView.setText(optionEditText.getText());            }

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
                dialog.dismiss();

                itemsAdapter.remove(position);

                List<WheelSection> newWheelSections = new ArrayList<>();

                for (WheelSection ws : itemsAdapter.getItems()) {
                    newWheelSections.add(ws);
                }

                wheelView.setWheelSections(newWheelSections);
                wheelView.generateWheel();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

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

            }
        });

        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
    }

    public void shuffleWheel(View view) {
        List<WheelSection> newWheelSections = new ArrayList<>();

        if (wheelSections.size() > 0) {
            wheelSections.clear();
            itemsAdapter.clear();

            for (int i = 0; i < 4; i++) {
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

            for (WheelSection ws : wheelSections) {
                newWheelSections.add(ws);
            }

            int preferredSize = newWheelSections.size() * (optionRepeatSB.getProgress() + 1);

            int difference = preferredSize - newWheelSections.size();

            for (int i = 0; i < difference; i++) {
                newWheelSections.add(newWheelSections.get(i % newWheelSections.size()));
            }
        } else {
            for (int i = 0; i < itemsAdapter.getCount(); i++) {
                WheelTextSection item = (WheelTextSection) itemsAdapter.getItem(i);

                int bR = new Random().nextInt(255);
                int bG = new Random().nextInt(255);
                int bB = new Random().nextInt(255);

                int fR = ~bR;
                int fG = ~bG;
                int fB = ~bB;

                itemsAdapter.updateItem(i, new WheelTextSection(item.getText())
                        .setSectionForegroundColor(Color.rgb(fR, fG, fB))
                        .setSectionBackgroundColor(Color.rgb(bR, bG, bB)));
            }

            for (WheelSection ws : itemsAdapter.getItems()) {
                newWheelSections.add(ws);
            }

            int preferredSize = newWheelSections.size() * (optionRepeatSB.getProgress() + 1);

            int difference = preferredSize - newWheelSections.size();

            for (int i = 0; i < difference; i++) {
                newWheelSections.add(newWheelSections.get(i % newWheelSections.size()));
            }
        }

        wheelView.setWheelSections(newWheelSections);

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
                dialog.hide();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                String s = previewTextView.getText().toString();

//                itemsAdapter.add(new WheelTextSection("Item #" + new Random().nextInt(200))
//                        .setSectionForegroundColor(Color.rgb(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255)))
//                        .setSectionBackgroundColor(Color.rgb(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255))));

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

//        final Drawable buttonDrawable = DrawableCompat.wrap(view.getBackground());
//        DrawableCompat.setTintMode(buttonDrawable, PorterDuff.Mode.SRC_ATOP);
//        DrawableCompat.setTint(buttonDrawable, 0x55000000);

        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.button_fade_out);
        fadeOut.setDuration(100);
        view.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                view.setBackground(buttonDrawable);

                Animation fadeIn = AnimationUtils.loadAnimation(WheelSettingsActivity.this, R.anim.button_fade_in);
                fadeIn.setDuration(100);
                view.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

//        if (wheelView.getFlingDirection() == FlingDirection.STOPPED) {

        wheelView.stopWheel();

        Bundle extra = new Bundle();
//        if (itemsAdapter.getItems().size() < 1) {
//            extra.putSerializable("sections", (Serializable) wheelSections);
//        } else {
        extra.putSerializable("sections", (Serializable) itemsAdapter.getItems());
//        }

        extra.putInt("textSize", textSizeSB.getProgress());
        extra.putInt("optionRepeat", (optionRepeatSB.getProgress() + 1));
        extra.putInt("spinTime", spinTimeSB.getProgress());

        Intent intent = new Intent(WheelSettingsActivity.this, MainActivity.class);
        intent.putExtra("extra", extra);
        startActivity(intent);
//        }
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
