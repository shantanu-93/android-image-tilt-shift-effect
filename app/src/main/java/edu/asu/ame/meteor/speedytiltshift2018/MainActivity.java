package edu.asu.ame.meteor.speedytiltshift2018;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    float a0f, a1f, a2f,a3f, s0f, s1f;
    private ImageView imageView;
    private TextView textHolder;
    private Bitmap bmp;
    static float sigma_range = 5.0f;
    private int[] imgIds= new int[]{R.drawable.img1,R.drawable.img2,R.drawable.img3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.imageView);
        setupUI();
        setupImg(0);
        textHolder = (TextView)findViewById((R.id.textBox));
        setupText("Tilt shift filter");
    }


    public void javaPress(View view){
        long start = System.currentTimeMillis();
        Bitmap outbmp = SpeedyTiltShift.tiltshift_java(bmp,s0f*sigma_range,s1f*sigma_range,(int)(a0f*bmp.getHeight()),(int)(a1f*bmp.getHeight()),(int)(a2f*bmp.getHeight()),(int)(a3f*bmp.getHeight()));
        long elapsed = System.currentTimeMillis() - start;
        imageView.setImageBitmap(outbmp);
        setupText("java perf: "+elapsed+"ms");
    }
    public void cppPress(View view){
        long start = System.currentTimeMillis();
        Bitmap outbmp = SpeedyTiltShift.tiltshift_cpp(bmp,s0f*sigma_range,s1f*sigma_range,(int)(a0f*bmp.getHeight()),(int)(a1f*bmp.getHeight()),(int)(a2f*bmp.getHeight()),(int)(a3f*bmp.getHeight()));
        long elapsed = System.currentTimeMillis() - start;
        imageView.setImageBitmap(outbmp);
        setupText("cpp perf: "+elapsed+"ms");
    }
    public void neonPress(View view){
        long start = System.currentTimeMillis();
        Bitmap outbmp = SpeedyTiltShift.tiltshift_neon(bmp,s0f*sigma_range,s1f*sigma_range,(int)(a0f*bmp.getHeight()),(int)(a1f*bmp.getHeight()),(int)(a2f*bmp.getHeight()),(int)(a3f*bmp.getHeight()));
        long elapsed = System.currentTimeMillis() - start;
        imageView.setImageBitmap(outbmp);
        setupText("neon perf: "+elapsed+"ms");
    }

    private void setupImg(int imgId){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmp = BitmapFactory.decodeResource(this.getResources(), imgIds[imgId],options);
        imageView.setImageBitmap(bmp);
    }
    private void setupText(String txt){
        textHolder.setText(txt);
    }

    private void setupUI(){
        final SeekBar seek0 = (SeekBar)findViewById(R.id.rowSeek0);
        final SeekBar seek1 = (SeekBar)findViewById(R.id.rowSeek1);
        final SeekBar seek2 = (SeekBar)findViewById(R.id.rowSeek2);
        final SeekBar seek3 = (SeekBar)findViewById(R.id.rowSeek3);
        SeekBar seek4 = (SeekBar)findViewById(R.id.rowSeek4);
        SeekBar seek5 = (SeekBar)findViewById(R.id.rowSeek5);
        Button imgbtn1 = (Button)findViewById(R.id.imgbtn1);
        Button imgbtn2 = (Button)findViewById(R.id.imgbtn2);
        Button imgbtn3 = (Button)findViewById(R.id.imgbtn3);
        Button javabtn = (Button)findViewById(R.id.javabtn);
        Button cppbtn = (Button)findViewById(R.id.cppbtn);
        Button neonbtn = (Button)findViewById(R.id.neonbtn);
        javabtn.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                javaPress(view);
            }
        });
        cppbtn.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                cppPress(view);
            }
        });
        neonbtn.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                neonPress(view);
            }
        });
        imgbtn1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupImg(0);
                setupText("Choose method");
            }
        });
        imgbtn2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupImg(1);
                setupText("Choose method");
            }
        });
        imgbtn3.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupImg(2);
                setupText("Choose method");
            }
        });
        seek0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress<seek1.getProgress()){
                    seek1.setProgress(progress);
                }
                a0f=1-progress/100f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress>seek0.getProgress()){
                    seek0.setProgress(progress);
                }
                if (progress<seek2.getProgress()){
                    seek2.setProgress(progress);
                }
                a1f=1-progress/100f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress>seek1.getProgress()){
                    seek1.setProgress(progress);
                }
                if (progress<seek3.getProgress()){
                    seek3.setProgress(progress);
                }
                a2f=1-progress/100f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        seek3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress>seek2.getProgress()){
                    seek2.setProgress(progress);
                }
                a3f=1-progress/100f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        seek4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                s0f=progress/100f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        seek5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                s1f=progress/100f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
    }

}
