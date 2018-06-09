package com.ssig.smartcap.common;


import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;


public class CountDownAnimation {

    private TextView mTextView;
    private Animation mAnimation;
    private int mStartCount;
    private int mCurrentCount;
    private CountDownListener mListener;

    private ToneGenerator toneGenerator;
    private Vibrator vibrator;
    private boolean hasSound;
    private boolean hasVibration;

    private Handler mHandler = new Handler();

    private final Runnable mCountDown = new Runnable() {
        public void run() {
            if (mCurrentCount > 0) {
                if (hasVibration)
                    vibrator.vibrate(100);
                if (hasSound)
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 800);
                mTextView.setText(String.valueOf(mCurrentCount));
                mTextView.startAnimation(mAnimation);
                mCurrentCount--;
            } else {
                if (hasVibration)
                    vibrator.vibrate(1000);
                if (hasSound)
                    toneGenerator.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1000);
                mTextView.setVisibility(View.GONE);
                if (mListener != null)
                    mListener.onCountDownEnd(CountDownAnimation.this);

            }
        }
    };


    public CountDownAnimation(Context context, TextView textView, int startCount, boolean hasSound, boolean hasVibration) {

        this.mTextView = textView;
        this.mStartCount = startCount;
        this.toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 500);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        this.hasSound = hasSound;
        this.hasVibration = hasVibration;

        this.mAnimation = new AlphaAnimation(1.0f, 0.0f);
        this.mAnimation.setDuration(1000);
    }


    public void start() {
        mHandler.removeCallbacks(mCountDown);

        mTextView.setText(String.valueOf(mStartCount));
        mTextView.setVisibility(View.VISIBLE);

        mCurrentCount = mStartCount;

        mHandler.post(mCountDown);
        for (int i = 1; i <= mStartCount; i++) {
            mHandler.postDelayed(mCountDown, i * 1000);
        }
    }

//    public void cancel() {
//        mHandler.removeCallbacks(mCountDown);
//
//        mTextView.setText("");
//        mTextView.setVisibility(View.GONE);
//    }


    public void setCountDownListener(CountDownListener listener) {
        mListener = listener;
    }


    public interface CountDownListener {
        void onCountDownEnd(CountDownAnimation animation);
    }
}
