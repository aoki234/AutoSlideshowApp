package jp.techacademy.jun.aoki.autoslideshowapp;

import android.Manifest;
import android.os.Handler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Timer mTimer;
    TextView mText;
    ImageView imageView;
    Button mNextbtn;
    Button mBackbtn;
    Button mAutobtn;

    ArrayList<Uri> imgArray = new ArrayList<>();
    int mCountNum = 0;

    Handler mHandler = new Handler();

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mNextbtn = (Button) findViewById(R.id.next_button);
        mBackbtn = (Button) findViewById(R.id.back_button);
        mAutobtn = (Button) findViewById(R.id.auto_button);

        imageView = (ImageView) findViewById(R.id.imageview);
        mText = (TextView)findViewById(R.id.text);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        mAutobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgArray.size() > 0) {
                    if (mTimer == null) {
                        mAutobtn.setText("停止");
                        mNextbtn.setEnabled(false);
                        mBackbtn.setEnabled(false);

                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mCountNum += 1;


                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int num = mCountNum % imgArray.size();
                                        imageView.setImageURI(imgArray.get(num));
                                    }
                                });
                            }
                        }, 2000, 2000);
                    } else {
                        mTimer.cancel();
                        mTimer = null;
                        mAutobtn.setText("再生");
                        mNextbtn.setEnabled(true);
                        mBackbtn.setEnabled(true);
                    }
                }
            }
        });


        mNextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imgArray.size() > 0) {
                    mCountNum += 1;
                    int num = mCountNum % imgArray.size();
                    imageView.setImageURI(imgArray.get(num));

                } else {
                    mText.setText(String.format("写真へのアクセスを許可し、画像を追加してください"));
                }
            }
        });

        mBackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imgArray.size() > 0) {

                        if(mCountNum > 0){
                            mCountNum -= 1;
                            //Log.d("javatest",String.valueOf(mCountNum));
                        }else{

                            mCountNum = imgArray.size()-1;
                            //Log.d("javatest",String.valueOf(mCountNum));

                        }
                        int num = mCountNum % imgArray.size();

                    imageView.setImageURI(imgArray.get(num));
                    //Log.d("javatest",String.valueOf(num));

                } else {
                    mText.setText(String.format("写真へのアクセスを許可し、画像を追加してください"));
                }
            }
        });


    }


        @Override
        public void onRequestPermissionsResult ( int requestCode, String[] permissions,
        int[] grantResults){
            switch (requestCode) {
                case PERMISSIONS_REQUEST_CODE:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getContentsInfo();
                    }
                    break;
                default:
                    mText.setText(String.format("写真へのアクセスを許可してください"));
                    break;
            }
        }

        private void getContentsInfo () {

            // 画像の情報を取得する
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                    null, // 項目(null = 全項目)
                    null, // フィルタ条件(null = フィルタなし)
                    null, // フィルタ用パラメータ
                    null // ソート (null ソートなし)
            );

            if (cursor.moveToFirst()) {
                do {
                    // indexからIDを取得し、そのIDから画像のURIを取得する
                    int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    Long id = cursor.getLong(fieldIndex);
                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    Log.d("ANDROID", "URI : " + imageUri.toString());
                    imgArray.add(imageUri);

                    //imageView.setImageURI(imageUri);
                } while (cursor.moveToNext());
            }
            cursor.close();
            imageView.setImageURI(imgArray.get(0));
        }

}

