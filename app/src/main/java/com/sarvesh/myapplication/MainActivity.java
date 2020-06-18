package com.sarvesh.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    //////////////////////////////////////////////////
    public String HOST = "192.168.43.205";
    public int Port = 1234;
    public String message;
    public String name;
    public Drawable photo;
    public int SELECT_PHOTO = 1;
    public Uri uri;
    public ImageView photoImage;


    //////////////////////////////////////////////////
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

///////////////////////////////////////////////
        setContentView(R.layout.fragment_home);
        final EditText nameText = (EditText) findViewById(R.id.nameText);
        final Button sendButton = (Button) findViewById(R.id.sendButton);
        photoImage = (ImageView) findViewById(R.id.photoImage);
        sendButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                send sendcode = new send();
                name = nameText.getText().toString();
                sendcode.execute();
            }
         });

        final Button uploadPhotoButton = (Button) findViewById(R.id.uploadPhotoButton);
        uploadPhotoButton.setOnClickListener(new View.OnClickListener(){
              public void onClick(View v) {
                  Intent intent = new Intent (Intent.ACTION_PICK);
                  intent.setType("image/*");
                  startActivityForResult(intent,SELECT_PHOTO);
              }
        });
///////////////////////////////////////////////
       }
///////////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data.getData() != null){
            uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                photoImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

///////////////////////////////////////////////
//////////////////////////////////////////////////////////////////
        class send extends AsyncTask<Void, Void, Void> {
             Socket s;
             PrintWriter pw;

            @Override
            protected Void doInBackground(Void... params) {
                //send name
                sendName();
                //send photo
                try {
                    sendFileToServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            void sendName()
            {
                //prepration
                try{
                    s = new Socket(HOST,Port);
                    pw = new PrintWriter(s.getOutputStream());
                } catch (UnknownHostException e){
                    System.out.println("Fail");
                    e.printStackTrace();
                } catch (IOException e){
                    System.out.println("Fail");
                    e.printStackTrace();
                }
                //send NAME_LENGTH
                byte[] nameBytes = name.getBytes();
                int nameBytesLength = nameBytes.length;
                String nameBytesLengthString = Integer.toString(nameBytesLength);
                if (nameBytesLength <= 9)
                {System.out.println(nameBytesLength);
                    pw.write("0"+nameBytesLengthString);}
                else
                    pw.write(nameBytesLengthString);
                //send name
                pw.write(name);
                pw.flush();
                pw.close();
                //s.close();
            }
             void sendFileToServer () throws UnknownHostException, IOException {

                //File myFile = new File((uri.getPath()));
                File myFile = new File("app\\src\\main\\res\\drawable\\avatar.png");

                while (true) {
                    byte[] mybytearray = new byte[(int) myFile.length()];
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                    bis.read(mybytearray, 0, mybytearray.length);
                    //now file is available in mybytearray

                    //send photo size
                    pw = new PrintWriter(s.getOutputStream());
                    String photoImageLengthString = Integer.toString(mybytearray.length);
                    pw.write(photoImageLengthString);
                    pw.flush();
                    pw.close();
                    //now writing this mybytearray image to outputstream
                    /*OutputStream ost = s.getOutputStream();
                    ost.write(mybytearray, 0, mybytearray.length);
                    ost.flush();*/
                    //s.close();
                }
            }

            public void receiveFileFromServer () throws UnknownHostException, IOException {
                Socket sock = new Socket("192.168.1.10", 5555);
                byte[] mybytearray = new byte[1024];
                InputStream is = sock.getInputStream();
                FileOutputStream fos = new FileOutputStream("/home/files/file.jpeg");
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                int bytesRead = is.read(mybytearray, 0, mybytearray.length);
                bos.write(mybytearray, 0, bytesRead);
                bos.close();
                sock.close();
            }
        }
/////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}