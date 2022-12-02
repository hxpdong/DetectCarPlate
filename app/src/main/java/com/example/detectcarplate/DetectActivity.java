package com.example.detectcarplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.detectcarplate.model.CharacterDetect;
import com.example.detectcarplate.tflite.Classifier;
import com.example.detectcarplate.tflite.YOLOv5Classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetectActivity extends AppCompatActivity {
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.15f;
    public static final int TF_OD_API_INPUT_SIZE = 416;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "carplate5s.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/classes.txt";
    int imgSize = TF_OD_API_INPUT_SIZE;

    ImageView imghinh;
    TextView txtresult, txtnumline, txtafter, txtbefore;
    Button btncamera, btngallery;
    LinearLayout lnlresult;
    ProgressDialog noti;
    private Classifier detector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        imghinh = (ImageView) findViewById(R.id.imgHinh);
        txtresult = (TextView) findViewById(R.id.result);
        btncamera = (Button) findViewById(R.id.btnCamera);
        lnlresult = (LinearLayout) findViewById(R.id.lnlResult);
        txtnumline = (TextView) findViewById(R.id.txtNumLine);
        txtafter = (TextView) findViewById(R.id.txtAfter);
        txtbefore = (TextView) findViewById(R.id.txtBefore);

        btncamera.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) { openCamera(); }
        });

        noti = new ProgressDialog(this);
        noti.setTitle("Đang nhận diện");
        noti.setMessage("Vui lòng đợi...");
        noti.setCancelable(false);

        txtresult.setText("");
        txtnumline.setText("");
        txtbefore.setText("");
        txtafter.setText("");
        //connect to model to detect
        try {
            detector =
                    YOLOv5Classifier.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED,
                            TF_OD_API_INPUT_SIZE);
        } catch (final IOException e) {
            e.printStackTrace();
            //LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }
    public void openCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 2);


    }

    public void swap(CharacterDetect char1, CharacterDetect char2) {
        CharacterDetect temp;
        temp = new CharacterDetect(char1);

        char1.setX(char2.getX());
        char1.setY(char2.getY());
        char1.setCharacter(char2.getCharacter());
        char1.setLocation(char2.getLocation());
        char1.setConf(char2.getConf());

        char2.setX(temp.getX());
        char2.setY(temp.getY());
        char2.setCharacter(temp.getCharacter());
        char2.setLocation(temp.getLocation());
        char2.setConf(temp.getConf());
    }



    public void classifyImage(Bitmap img){
        final List<Classifier.Recognition> results = detector.recognizeImage(img);
        final Canvas canvas = new Canvas(img);
        final Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5.0f);

        List<Classifier.Recognition> ArrNumCarPlate = results;
        for(int i = 0; i < results.size(); i++) {
            System.out.println("ID: " + results.get(i).getId() +
                    "- Title: " + results.get(i).getTitle() +
                    "- RectF: " + results.get(i).getLocation() +
                    "- Confidence: " + results.get(i).getConfidence() + "\n");

        }
        //lưu trữ tọa độ (x,y) của các đối tượng từ kết quả
        ArrayList<CharacterDetect> pos = new ArrayList<CharacterDetect>();
        for (int i=0; i<ArrNumCarPlate.size(); i++){
            int xpos = 0;
            int ypos = 0;
            String character = "";
            RectF location;
            Float conf;
            xpos = (int) ((ArrNumCarPlate.get(i).getLocation().left + ArrNumCarPlate.get(i).getLocation().right)/2);
            ypos = (int) ((ArrNumCarPlate.get(i).getLocation().top + ArrNumCarPlate.get(i).getLocation().bottom)/2);
            character = ArrNumCarPlate.get(i).getTitle();
            location = ArrNumCarPlate.get(i).getLocation();
            conf = ArrNumCarPlate.get(i).getConfidence();
            pos.add(new CharacterDetect(xpos, ypos, character, location, conf));
        }
        ///// check 2 đối tượng có tâm tọa độ đụng độ nhau (2 đối tượng chồng lên nhau => chọn đối tượng tỷ lệ cao hơn)
        for(int i=0;i<pos.size(); i++){
            for (int j=0;j<pos.size(); j++){
                if (i==j) continue;
                else if(
                        pos.get(i).getY() >= pos.get(j).getLocation().top &&
                                pos.get(i).getY() <= pos.get(j).getLocation().bottom &&
                                pos.get(i).getX() >= pos.get(j).getLocation().left &&
                                pos.get(i).getX() <= pos.get(j).getLocation().right
                ){
                    if(pos.get(i).getConf() > pos.get(j).getConf()){
                        pos.remove(pos.get(j));
                    } else pos.remove(pos.get(i));
                }
            }
        }
        /////tìm trung điểm của trục dọc để xác định biển số có 1 hay 2 hàng
        for (int i=0; i<pos.size();i++){
            System.out.println("Xpos: " + pos.get(i).x + " Ypos: "+pos.get(i).y + " Char: " + pos.get(i).character + " Location RectF: " + pos.get(i).getLocation());
        }
        String plateorigin ="";
        for (int i=0; i<pos.size();i++){
            plateorigin = plateorigin + pos.get(i).getCharacter();
            canvas.drawRect(pos.get(i).getLocation(), paint);
        }
        int ymin = 0, ymax = 0;
        float yavg = 0.0f;
        for (int i=0; i<pos.size();i++){
            if(i==0) {ymin = pos.get(i).getY(); ymax = pos.get(i).getY();}
            else {
                if (pos.get(i).getY() < ymin){
                    ymin = pos.get(i).getY();
                }
                if (pos.get(i).getY() > ymax){
                    ymax = pos.get(i).getY();
                }
            }
        }
        yavg = (ymin+ymax)/2;
        System.out.println("Ymin: "+ymin + " Ymax: " + ymax + " YAvg: " + yavg);



        int numline = 1;
        for(int i=0; i<pos.size(); i++){
            //nếu có bất kì số nào có tọa độ dọc (y) lớn hơn cạnh dưới của số có tọa độ cao nhất (ymin) thì sẽ có 2 hàng
            if(pos.get(i).getY() == ymin){
                for (int j=0; j<pos.size(); j++){
                    if (pos.get(j).getY() > pos.get(i).getLocation().bottom){
                        numline = 2; break;
                    }
                }
            }
        }
        System.out.println("Numline is: " + numline);
        //chia mảng ban đầu thành 1 hoặc 2 mảng dựa trên số hàng đã xác định
        ArrayList<CharacterDetect> line1 = new ArrayList<CharacterDetect>();
        ArrayList<CharacterDetect> line2 = new ArrayList<CharacterDetect>();
        switch(numline){
            case 2:
                for (int i=0;i<pos.size(); i++){
                    if(pos.get(i).getY() < yavg){
                        line1.add(new CharacterDetect(pos.get(i).getX(), pos.get(i).getY(), pos.get(i).character, pos.get(i).location, pos.get(i).conf));
                    }
                    else line2.add(new CharacterDetect(pos.get(i).getX(), pos.get(i).getY(), pos.get(i).character, pos.get(i).location, pos.get(i).conf));
                }
                break;
            case 1:
                for (int i=0;i<pos.size(); i++){
                    line1.add(new CharacterDetect(pos.get(i).getX(), pos.get(i).getY(), pos.get(i).character, pos.get(i).location, pos.get(i).conf));
                }
                break;
        }


        System.out.println("Line 1 : ");
        for (int i=0; i<line1.size();i++){
            System.out.println("Xpos: " + line1.get(i).getX() + " Ypos: "+line1.get(i).getY() + " Char: " + line1.get(i).getCharacter()+ " Location RectF: " + line1.get(i).getLocation());
        }
        System.out.println("Line 2 : ");
        for (int i=0; i<line2.size();i++){
            System.out.println("Xpos: " + line2.get(i).getX() + " Ypos: "+line2.get(i).getY() + " Char: " + line2.get(i).getCharacter()+ " Location RectF: " + line2.get(i).getLocation());
        }
        //sắp xếp các mảng vừa được chia theo trục ngang tọa độ (x) tăng dần từ nhỏ tới lớn (tức các số từ trái sang phải)

        for(int i=0;i<line1.size()-1;i++){
            for(int j=i+1;j<line1.size(); j++){
                if(line1.get(i).getX() > line1.get(j).getX()){
                    swap(line1.get(i), line1.get(j));
                }

            }
        }


        System.out.println("Line 1 after sorted : ");
        for (int i=0; i<line1.size();i++){
            System.out.println("Xpos: " + line1.get(i).getX() + " Ypos: "+line1.get(i).getY() + " Char: " + line1.get(i).getCharacter() + " RectF: " + line1.get(i).getLocation());
        }

        for(int i=0;i<line2.size()-1;i++){
            for(int j=i+1;j<line2.size(); j++){
                if(line2.get(i).getX() > line2.get(j).getX()){
                    swap(line2.get(i), line2.get(j));
                }

            }
        }
        System.out.println("Line 2 after sorted : ");
        for (int i=0; i<line2.size();i++){
            System.out.println("Xpos: " + line2.get(i).getX() + " Ypos: "+line2.get(i).getY() + " Char: " + line2.get(i).getCharacter());
        }
        //gộp 2 mảng sau khi được sắp xếp hoàn chỉnh lại thành biển số, line1 sẽ là hàng trên và line2 là hàng dưới
        ArrayList<CharacterDetect> numplate = new ArrayList<CharacterDetect>();
        for(int i=0;i<line1.size();i++){
            numplate.add(line1.get(i));
        }
        for(int i=0;i<line2.size();i++){
            numplate.add(line2.get(i));
        }

        String plate ="";
        for (int i=0; i<numplate.size();i++){
            plate = plate + numplate.get(i).getCharacter();
        }
        System.out.println("The numberplate is : " + plate);
        txtresult.setVisibility(View.VISIBLE);
        txtresult.setText(plate);
        txtnumline.setText("Lines: " + numline);
        txtbefore.setText("Before sorting: " + plateorigin);
        txtafter.setText("After sorting: " + plate);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Bitmap image = null;
        if(resultCode == RESULT_OK){
            if (requestCode == 2){
                image = (Bitmap) data.getExtras().get("data");
                //int dimension = Math.min(image.getWidth(), image.getHeight());
                //image = ThumbnailUtils.extractThumbnail(image, dimension,dimension);
                image = Bitmap.createScaledBitmap(image, imgSize, imgSize, false);
            }
        }
        if (image != null){
            Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
            imghinh.setImageBitmap(mutableBitmap);
            txtresult.setText("");
            txtnumline.setText("");
            txtbefore.setText("");
            txtafter.setText("");

            noti.show();
            new CountDownTimer(1000, 1000) {
                public void onFinish() {
                    classifyImage(mutableBitmap);
                    noti.dismiss();
                }
                public void onTick(long millisUntilFinished) {

                }
            }.start();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
