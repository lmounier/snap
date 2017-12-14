package ai.deepar.example;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private MediaQuery mediaQuery;
    private List<VideoItem> videoItemList;
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        String path = Environment.getExternalStorageDirectory().toString() + "/snap/";
        File directory = new File(path);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".mp4")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        File[] files = directory.listFiles(filter);
        List<Pair<File, File>> allFiles = new ArrayList();
        for(int i=0; i<files.length; i++){
            System.out.println(files[i].getName());
            String name = files[i].getName();
            String nomCourt = name.substring(0,name.indexOf('.'));
            File image = new File(path + nomCourt + ".jpg");
            allFiles.add(Pair.create(files[i], image));
            System.out.println("image : " + image.getName());

        }
        final RecyclerView rv = (RecyclerView) findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new VideoAdapter(allFiles));
    }

    public void changeLayout(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
