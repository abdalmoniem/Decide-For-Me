package com.hifnawy.decideForMe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.hifnawy.spinningWheelLib.DimensionUtil;
import com.hifnawy.spinningWheelLib.SpinningWheelView;
import com.hifnawy.spinningWheelLib.model.MarkerPosition;
import com.hifnawy.spinningWheelLib.model.WheelBitmapSection;
import com.hifnawy.spinningWheelLib.model.WheelColorSection;
import com.hifnawy.spinningWheelLib.model.WheelDrawableSection;
import com.hifnawy.spinningWheelLib.model.WheelSection;
import com.hifnawy.spinningWheelLib.model.WheelTextSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    SpinningWheelView wheelView;
    List<WheelSection> wheelSections;

    TextView textView;
    ImageView homeImage;

    float textViewScaleX;
    float textViewScaleY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wheelView = findViewById(R.id.home_spinning_wheel_view);

        textView = findViewById(R.id.textView);
        homeImage = findViewById(R.id.home_image);

        textViewScaleX = textView.getScaleX();
        textViewScaleY = textView.getScaleY();

        wheelSections = (List<WheelSection>) getIntent().getBundleExtra("extra").getSerializable("sections");

        int textSize = getIntent().getBundleExtra("extra").getInt("textSize");
        int optionRepeat = getIntent().getBundleExtra("extra").getInt("optionRepeat");
        int spinTime = getIntent().getBundleExtra("extra").getInt("spinTime");

        if (wheelSections != null) {
            if (optionRepeat > 1) {
                List<WheelSection> newWheelSections = new ArrayList<>();

                for (WheelSection ws : wheelSections) {
                    newWheelSections.add(ws);
                }

                int preferredSize = wheelSections.size() * optionRepeat;

                int difference = preferredSize - wheelSections.size();


                for (int i = 0; i < difference; i++) {
                    newWheelSections.add(wheelSections.get(i % wheelSections.size()));
                }

                wheelView.setWheelSections(newWheelSections);
            } else {
                wheelView.setWheelSections(wheelSections);
            }

            wheelView.setMarkerPosition(MarkerPosition.TOP);

            wheelView.setWheelBorderLineColor(R.color.white);
            wheelView.setWheelBorderLineThickness(10);

            wheelView.setWheelSeparatorLineColor(R.color.white);
            wheelView.setWheelSeparatorLineThickness(1);

            wheelView.setTickVibrations(true);

            wheelView.setGlobalTextSize(textSize);

            wheelView.setWheelEventsListener(new WheelEventsListener());

            wheelView.generateWheel();

            wheelView.setInitialFlingDampening(1f);
            wheelView.setFlingVelocityDampening(1.01f);

            wheelView.flingWheel((spinTime + 1) * 1000, 1000 + (1000 * (int) Math.pow(2, spinTime)), (new Random().nextFloat() > 0.5));
        }
    }

    private class WheelEventsListener implements com.hifnawy.spinningWheelLib.WheelEventsListener {

        @Override
        public void onWheelStopped() {
        }

        @Override
        public void onWheelFlung() {
            ObjectAnimator animatorX = ObjectAnimator.ofFloat(textView, "scaleX", textViewScaleX);
            ObjectAnimator animatorY = ObjectAnimator.ofFloat(textView, "scaleY", textViewScaleY);

            animatorX.setDuration(500);
            animatorY.setDuration(500);

            animatorX.start();
            animatorY.start();
        }

        @Override
        public void onWheelSectionChanged(int sectionIndex, double angle) {
            WheelSection section = wheelView.getWheelSections().get(sectionIndex);
            switch (section.getType()) {
                case TEXT:
                    textView.setText(((WheelTextSection) section).getText());
                    break;
                default:
                    textView.setText("Selected " + sectionIndex);
                    break;
            }
        }

        @Override
        public void onWheelSettled(final int sectionIndex, double angle) {
            if (sectionIndex >= 0) {
                final float scaleUpFactor = 6f;
                final float scaleDownFactor = 2f;

                WheelSection section = wheelView.getWheelSections().get(sectionIndex);
                ObjectAnimator animatorX = ObjectAnimator.ofFloat(textView, "scaleX", textView.getScaleX() + DimensionUtil.convertPixelsToDp(scaleUpFactor));
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(textView, "scaleY", textView.getScaleY() + DimensionUtil.convertPixelsToDp(scaleUpFactor));
                animatorX.setDuration(300);
                animatorY.setDuration(300);

                animatorX.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ObjectAnimator animatorXX = ObjectAnimator.ofFloat(textView, "scaleX",
                                textView.getScaleX() - DimensionUtil.convertPixelsToDp(scaleDownFactor));
                        animatorXX.setDuration(300);
                        animatorXX.start();
                    }
                });

                animatorY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        ObjectAnimator animatorYY = ObjectAnimator.ofFloat(textView, "scaleY",
                                textView.getScaleY() - DimensionUtil.convertPixelsToDp(scaleDownFactor));
                        animatorYY.setDuration(300);
                        animatorYY.start();
                    }
                });

                animatorX.start();
                animatorY.start();

                switch (section.getType()) {
                    case TEXT:
                        textView.setText(((WheelTextSection) section).getText());
                        Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);

                        textView.startAnimation(fadeOut);

                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                WheelSection section = wheelView.getWheelSections().get(sectionIndex);
                                switch (section.getType()) {
                                    case TEXT:
                                        textView.setTextColor(((WheelTextSection) section).getForegroundColor());
                                        break;

                                }

                                Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                                textView.startAnimation(fadeIn);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        break;
                    default:
                        textView.setText("Selected " + sectionIndex);
                        break;
                }

                Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                homeImage.startAnimation(fadeOut);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        WheelSection section = wheelView.getWheelSections().get(sectionIndex);
                        switch (section.getType()) {
                            case TEXT:
                                homeImage.setImageDrawable(null);
                                homeImage.setBackgroundColor(((WheelTextSection) section).getBackgroundColor());
                                break;
                            case BITMAP:
                                homeImage.setImageBitmap(((WheelBitmapSection) section).getBitmap());
                                break;
                            case DRAWABLE:
                                homeImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), ((WheelDrawableSection) section).getDrawableRes()));
                                break;
                            case COLOR:
                                homeImage.setImageDrawable(null);
                                homeImage.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), ((WheelColorSection) section).getColor()));
                                break;

                        }

                        Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                        homeImage.startAnimation(fadeIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        }
    }
}