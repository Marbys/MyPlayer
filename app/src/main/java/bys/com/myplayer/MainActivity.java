package bys.com.myplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    //TODO Piosenki się odświeżają w oncreate!
    private ArrayList<RecordInfo> records = new ArrayList<RecordInfo>();
    RecyclerView recyclerView;
    SeekBar seekBar;
    RecordAdapter recordAdapter;
    MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        recordAdapter = new RecordAdapter(this, records);
        recyclerView.setAdapter(recordAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recordAdapter.setOnItemClickListener(new RecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final Button b, View v, final RecordInfo obj, int position) {
                if (b.getText().equals("Stop")) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    b.setText("Play");
                } else {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDataSource(obj.songUrl);
                                mediaPlayer.prepareAsync();
                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mp.start();
                                        seekBar.setProgress(0);
                                        seekBar.setMax(mediaPlayer.getDuration());
                                    }
                                });
                                b.setText("Stop");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    };
                    handler.postDelayed(runnable, 100);
                }
            }
        });

        recordAdapter.setOnItemClickListenerDelete(new RecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Button b, View v, RecordInfo obj, int position) {
                //TODO dodać opcje dialogową 
                File file = new File((Uri.parse(obj.songUrl)).getPath());
                //File file = new File(obj.songUrl);
                if(file.exists()){
                    Toast.makeText(getApplicationContext(),"File Exists",Toast.LENGTH_SHORT).show();
                    boolean isSuccess = file.delete();
                    Toast.makeText(getApplicationContext(),Boolean.toString(isSuccess),Toast.LENGTH_SHORT).show();
                    refreshSystemMediaScanDataBase(getApplicationContext(),file.getPath());
                }

                Toast.makeText(getApplicationContext(),file.getPath(),Toast.LENGTH_SHORT).show();
            }
        });
        checkUserPermission();
        Thread t = new MyThread();
        t.start();
    }

    public class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mediaPlayer != null) {
                    seekBar.post(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    });
                    Log.d("Runwa", "run: " + mediaPlayer.getCurrentPosition());
                }
            }
        }
    }

    private void checkUserPermission() {
        Log.d("MainActivity", "checkUserPermission");
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                return;
            }
        }
        loadSongs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("MainActivity", "onRequestPermissionsResult");
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSongs();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    checkUserPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }

    private void loadSongs() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = getContentResolver().query(uri, null, MediaStore.Audio.Media.DATA + " like ? ", new String[]{"%MyRecords%"}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String number = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String date = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    RecordInfo recordInfo = new RecordInfo(number, date, duration, url);
                    records.add(recordInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
            recordAdapter = new RecordAdapter(MainActivity.this, records);

        }
    }
    public static void refreshSystemMediaScanDataBase(Context context, String docPath){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(docPath));
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
