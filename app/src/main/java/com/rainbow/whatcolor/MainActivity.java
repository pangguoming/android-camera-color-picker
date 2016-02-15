package com.rainbow.whatcolor;

import java.util.List;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Toast;

public class MainActivity extends Activity {
    private ResizableCameraPreview mPreview;
    private RelativeLayout mLayout;
    private int mCameraId = 0;
	private TextView tv_color_name;
	private TextView tv_color_RGB;
	private TextView tv_color_HEX;
	private long exitTime = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mLayout = (RelativeLayout) findViewById(R.id.layout);
		tv_color_name = (TextView) findViewById(R.id.tv_color_name);
		tv_color_RGB = (TextView) findViewById(R.id.tv_color_RGB);
		tv_color_HEX = (TextView) findViewById(R.id.tv_color_HEX);
		Button captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});
	}

	private void createCameraPreview() {
		// Set the second argument by your choice.
		// Usually, 0 for back-facing camera, 1 for front-facing camera.
		// If the OS is pre-gingerbreak, this does not have any effect.
		mPreview = new ResizableCameraPreview(this, mCameraId, CameraPreview.LayoutMode.FitToParent, false);
		mLayout.addView(mPreview);
	}

	public void setTv_color_HEX(String text){
		tv_color_HEX.setText(text);
	}
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        mLayout.removeView(mPreview);
        mPreview = null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        createCameraPreview();
    }
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(getApplicationContext(), "Back again to exit.",
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
		}
	}
}
