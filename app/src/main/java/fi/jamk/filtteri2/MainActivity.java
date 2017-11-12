package fi.jamk.filtteri2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST = 1;
    private static final int RESULT_LOAD_IMAGE = 0;

    private final int TAKE_PICTURE = 1; //lisätty

    Button b_load, b_save, b_share, b_filter, b_filter2, b_take;

    ImageView imageView, imageFilter;
    String currentFileName = "";//lisätty

    Integer photoHeight = 0;
    Integer photoWidth = 0;

    String currentImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //storage oikeudet
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);

            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }

        }
        else {
            // ei mitään
        }

        imageView = (ImageView) findViewById(R.id.imageView);
        imageFilter = (ImageView) findViewById(R.id.imageFilter);

        b_load = (Button) findViewById(R.id.b_load);
        b_take = (Button) findViewById(R.id.b_take); //lisätty
        b_save = (Button) findViewById(R.id.b_save);
        b_filter = (Button) findViewById(R.id.b_filter);
        b_filter2 = (Button) findViewById(R.id.b_filter2);
        b_share = (Button) findViewById(R.id.b_share);

        b_filter.setEnabled(false);
        b_filter2.setEnabled(false);
        b_save.setEnabled(false);
        b_share.setEnabled(false);

        b_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });



        b_take.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View view){

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (intent.resolveActivity(getPackageManager()) != null) {

                    File file = new File(Environment.getExternalStorageDirectory(), fileName());
                    Uri imageUri = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                    startActivityForResult(intent, TAKE_PICTURE);
                }


            }
        });

        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View content = findViewById(R.id.lay);
                Bitmap bitmap = getScreenShot(content);
                currentImage = "image" + System.currentTimeMillis() + ".png";
                store(bitmap, currentImage);
                b_share.setEnabled(true);


            }
        });

        b_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Drawable unscaled = getResources().getDrawable(R.drawable.filter);
                Bitmap bm = ((BitmapDrawable)unscaled).getBitmap();

                Drawable scaled = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bm, photoWidth, photoHeight, true));

                imageFilter.setImageDrawable(scaled);

                b_save.setEnabled(true);

            }
        });

        b_filter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Drawable unscaled = getResources().getDrawable(R.drawable.filter2);
                Bitmap bm = ((BitmapDrawable)unscaled).getBitmap();

                Drawable scaled = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bm, photoWidth, photoHeight, true));

                imageFilter.setImageDrawable(scaled);

                b_save.setEnabled(true);

            }
        });

        b_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage(currentImage);

            }
        });
    }
    //get the filtered image
    private static Bitmap getScreenShot(View view){
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    //kuvan tallennus
    private void store(Bitmap bm, String fileName){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FILTTERIKUVAT";
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();

        }
        File file = new File(dirPath, fileName);
        try{
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // sharing the image
    private void shareImage(String fileName){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FILTTERIKUVAT";
        Uri uri = Uri.fromFile(new File(dirPath, fileName));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        try{
            startActivity(Intent.createChooser(intent, "Share via"));
        } catch (Exception e) {
            Toast.makeText(this, "No sharing app found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap kuva = BitmapFactory.decodeFile(picturePath);
            photoHeight = kuva.getHeight();
            photoWidth = kuva.getWidth();
            imageView.setImageBitmap(kuva);
            b_filter.setEnabled(true);
            b_filter2.setEnabled(true);
        }


        //lisättyä koodia ->
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            try {
                File file = new File(Environment.getExternalStorageDirectory(), currentFileName);

                //Bundle extras = data.getExtras();
                //Bitmap photo = (Bitmap) extras.get("data");
                Bitmap immutableBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));

                //changing immutablebitmap to mutable bitmap by making a copy of it
                Bitmap photo = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);

                photoHeight = photo.getHeight();
                photoWidth = photo.getWidth();

                //valokuvasta tulee vanmanen kun sitä skaalaa :(
                //photo.setHeight(1280);
                //photo.setWidth(720);
                imageView.setImageBitmap(photo);




                b_filter.setEnabled(true);
                b_filter2.setEnabled(true);



            } catch (FileNotFoundException e) {
                Toast.makeText(this,"Captured image not found! ",Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this,"Captured image not found! ",Toast.LENGTH_LONG).show();
            }
        }
        //loppuu,
    }

    public String fileName(){ //tämä luo uniikin tiedostonnimen ajan mukaan...
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        currentFileName = imageFileName;
        return imageFileName;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case MY_PERMISSION_REQUEST:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        //do nothing
                    }
                    else {
                        Toast.makeText(this, "No permission granted!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

            }
        }
    }
}
