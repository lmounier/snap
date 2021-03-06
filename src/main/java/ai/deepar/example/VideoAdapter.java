package ai.deepar.example;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.app.AlertDialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    /*private  List<Pair<String, String>> characters = Arrays.asList(
            Pair.create("Lyra Belacqua", "Brave, curious, and crafty, she has been prophesied by the witches to help the balance of life"),
            Pair.create("Pantalaimon", "Lyra's daemon, nicknamed Pan."),
            Pair.create("Roger Parslow", "Lyra's friends"),
            Pair.create("Lord Asriel", "Lyra's uncle"),
            Pair.create("Marisa Coulter", "Intelligent and beautiful, but extremely ruthless and callous."),
            Pair.create("Iorek Byrnison", "Armoured bear, Rightful king of the panserbjørne. Reduced to a slave of the human village Trollesund."),
            Pair.create("Serafina Pekkala", "Witch who closely follows Lyra on her travels."),
            Pair.create("Lee Scoresby", "Texan aeronaut who transports Lyra in his balloon. Good friend with Iorek Byrnison."),
            Pair.create("Ma Costa", "Gyptian woman whose son, Billy Costa is abducted by the \"Gobblers\"."),
            Pair.create("John Faa", "The King of all gyptian people.")
    );*/

    private List<Pair<File, File>> characters = null;

    public VideoAdapter(List<Pair<File, File>> list) {
        this.characters = list;
    }



    @Override
    public int getItemCount() {
        return characters.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.video_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Pair<File, File> pair = characters.get(position);
        holder.display(pair);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView name;

        private Pair<File, File> currentPair;

        public MyViewHolder(final View itemView) {
            super(itemView);

            image = ((ImageView) itemView.findViewById(R.id.image));
            name = ((TextView) itemView.findViewById(R.id.name));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle(currentPair.first.getName())
                            .setNeutralButton("Partager",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "test");
                                            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(currentPair.first));
                                            sharingIntent.setType("video/mp4");
                                            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            itemView.getContext().startActivity(Intent.createChooser(sharingIntent, "Share"));
                                        }
                                    })
                            .setPositiveButton("Lire",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Start NewActivity.class
                                            Intent myIntent = new Intent(itemView.getContext(),
                                                    VideoViewActivity.class);
                                            myIntent.putExtra("name_of_extra", currentPair.first.getPath());
                                            itemView.getContext().startActivity(myIntent);
                                        }
                                    })
                            .show();
                }
            });
        }

        public void display(Pair<File, File> pair) {
            currentPair = pair;
            name.setText(pair.first.getName());
            image.setImageURI(Uri.parse(pair.second.getPath()));
        }
    }

}