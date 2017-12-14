package ai.deepar.example;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

public class MediaQuery {
    private Context context;
    private int count = 0;
    private Cursor cursor;
    List<VideoItem> videoItems;
    public MediaQuery(Context context){
        this.context=context;
    }
    public File[] getAllVideo() {
        System.out.println(Uri.parse("content:/" + Environment.getExternalStorageDirectory().toString() + File.separator + "snap" + File.separator));
        System.out.println(Uri.parse(MediaStore.Video.Media.INTERNAL_CONTENT_URI.toString() + "/0/snap"));
        String path = Environment.getExternalStorageDirectory().toString() + "/snap/";
        File directory = new File(path);
        File[] files = directory.listFiles();

        return files;
    }
    /*public int getVideoCount(){
        int count=0;
        count=(getAllVideo()).size();
        return count;
    }*/
}
