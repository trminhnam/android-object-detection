package com.example.objectdetectionaivn;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button captureImageButton;
    private ImageView inputImageView;
    private TextView tvPlaceHolder;

    private ImageView sampleImage1;
    private ImageView sampleImage2;

    static  final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Data initialization
        captureImageButton = findViewById(R.id.captureImageButton);
        inputImageView = findViewById(R.id.imageViewID);
        tvPlaceHolder = findViewById(R.id.welcomeMessageTVID);

        sampleImage1 = findViewById(R.id.sampleImage1);
        sampleImage2 = findViewById(R.id.sampleImage2);

        // Click on sample image 1
        sampleImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    setViewAndDetect(getSampleImage(R.drawable.image_test));
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

        sampleImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    setViewAndDetect(getSampleImage(R.drawable.kite));
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

        // Init button when being clicked
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Taking a photo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setViewAndDetect(Bitmap bitmap) throws IOException {
        // Display captured image
        inputImageView.setImageBitmap(bitmap);
        tvPlaceHolder.setVisibility(View.INVISIBLE);

        // Run object detection and display result
        // running this process in background thread
        // also delay to show the labeled image
        inputImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    runObjectDetection(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    private void runObjectDetection(Bitmap bitmap) throws IOException {
        // Step 1: convert bitmap image to tensor image
        TensorImage image = TensorImage.fromBitmap(bitmap);

        // Step 2: initialize the detector object
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(5)
                .setScoreThreshold(0.5f)
                .build();

        ObjectDetector detector = ObjectDetector.createFromFileAndOptions(
                this,
                "android.tflite",
                options);

        List<Detection> results = detector.detect(image);

        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(outputBitmap);
        Paint pen = new Paint();
        pen.setTextAlign(Paint.Align.LEFT);

        for (Detection result: results){
            pen.setColor(Color.RED);
            pen.setStrokeWidth(8F);
            pen.setStyle(Paint.Style.STROKE);
            RectF box = result.getBoundingBox();
            canvas.drawRect(box, pen);

            Rect tagSize = new Rect(0, 0, 0, 0);

            pen.setStyle(Paint.Style.FILL_AND_STROKE);
            pen.setColor(Color.YELLOW);
            pen.setStrokeWidth(2F);

            Category category = result.getCategories().get(0);
            String text = category.getLabel() + " "  + Math.round(category.getScore()*100) + "%";

            pen.setTextSize(96F);
            pen.getTextBounds(text, 0, text.length(), tagSize);

            canvas.drawText(
                    text, box.left,
                    box.top, pen
            );

        }

        inputImageView.setImageBitmap(outputBitmap);

    }

    private Bitmap getSampleImage(int resID) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;

        return BitmapFactory.decodeResource(getResources(), resID, options);
    }




}