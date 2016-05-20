package com.loften.clipimagesample;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loften.clipimagesample.clipimage.ClipImageActivity;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;//请求加载系统照相机
    private ImageView mImageView;
    private Button button1,button2;
    private String mOutputPath;//输出路径
    private File mTmpFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化截图后地址
        mOutputPath = new File(getExternalCacheDir(), "chosen.jpg").getPath();

        mImageView = (ImageView) findViewById(R.id.image);

        //选择图片
        button1 = (Button) findViewById(R.id.bt_select);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhoto();
            }
        });
        //拍照
        button2 = (Button) findViewById(R.id.bt_take);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCameraAction();
            }
        });

    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(this.getPackageManager()) != null){
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            try {
                mTmpFile = FileUtils.createTmpFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(mTmpFile != null && mTmpFile.exists()) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }else{
                Toast.makeText(this, "图片错误", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "No system camera found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhoto(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE).setType(
                        "image/*");
        startActivityForResult(intent, ClipImageActivity.REQUEST_CAPTURE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK
                && data != null
                && (requestCode == ClipImageActivity.REQUEST_CLIP_IMAGE)) {
            String path = data.getStringExtra(ClipImageActivity.EXTRA_OUTPUT);
            Log.e("dd",path);
            if (path != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                mImageView.setImageBitmap(bitmap);
            }
            return;
        }

        //选择图片回调
        if(resultCode == Activity.RESULT_OK && requestCode == ClipImageActivity.REQUEST_CAPTURE_IMAGE){
            try {
                final Cursor cr = getContentResolver().query(data.getData(),
                        new String[]{MediaStore.Images.Media.DATA}, null,
                        null, null);
                if (cr.moveToFirst()) {
                    String localPath = cr.getString(cr
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    Log.e("aa",localPath);
                    Intent i = new Intent(this, ClipImageActivity.class);
                    i.putExtra(ClipImageActivity.EXTRA_INPUT,localPath);
                    i.putExtra(ClipImageActivity.EXTRA_OUTPUT,mOutputPath);
                    startActivityForResult(i,ClipImageActivity.REQUEST_CLIP_IMAGE);
                }
                cr.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //拍照回调
        if(requestCode == REQUEST_CAMERA){
            if(resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mTmpFile)));
                    Intent i = new Intent(this, ClipImageActivity.class);
                    i.putExtra(ClipImageActivity.EXTRA_INPUT,mTmpFile.getAbsolutePath().toString());
                    i.putExtra(ClipImageActivity.EXTRA_OUTPUT,mOutputPath);
                    startActivityForResult(i, ClipImageActivity.REQUEST_CLIP_IMAGE);
                }
            }else{
                while (mTmpFile != null && mTmpFile.exists()){
                    boolean success = mTmpFile.delete();
                    if(success){
                        mTmpFile = null;
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
