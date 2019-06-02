package ru.alexleru.a3l3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.DisposableSubscriber;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button openAndRead;
    Button stopButton;
    Uri uri;
    Flowable<Bitmap> flowable;
    static int INTENT_REQUEST_CODE = 870;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        imageView = findViewById(R.id.imageView);
        openAndRead = findViewById(R.id.open_and_read_convert_and_write);
        openAndRead.setOnClickListener(v -> openFile());
        stopButton = findViewById(R.id.stop);
        stopButton.setOnClickListener(v -> {});
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, INTENT_REQUEST_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        flowable = Flowable.create(emitter -> {
            if (requestCode == INTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    uri = data.getData();
                    Toast.makeText(MainActivity.this.getApplicationContext(),
                            ": " + uri.toString(),
                            Toast.LENGTH_LONG).show();
                    emitter.onNext(convertJPEGtoPNG());
                }
            }

        }, BackpressureStrategy.BUFFER);

        Disposable disposable = flowable.subscribeWith(new DisposableSubscriber<Bitmap>() {
            @Override
            public void onNext(Bitmap bitmap) {
                showPicture(bitmap);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
    }



    private Bitmap convertJPEGtoPNG(){
        FileOutputStream fileOutputStream;
        String nameFile = generatePath(this.uri, getApplicationContext());
        Bitmap bitmap = BitmapFactory.decodeFile(nameFile);
        File file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "fileSave.png");
        try {
            fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    private void showPicture(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    private static String generatePath(Uri uri,Context context){
        String filePath = null;
        if(DocumentsContract.isDocumentUri(context, uri)){
            String wholeID = DocumentsContract.getDocumentId(uri);

            String id = wholeID.split(":")[1];

            String[] column = { MediaStore.Images.Media.DATA };
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{ id }, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();
        }
        return filePath;
    }

}
