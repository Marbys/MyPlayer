package bys.com.myplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordHolder> {

    ArrayList<RecordInfo> records;
    Context context;
    OnItemClickListener onItemClickListener;
    OnItemClickListener onItemClickListenerDelete;

    public RecordAdapter(Context context,ArrayList<RecordInfo> records) {
        this.context = context;
        this.records = records;
    }

    @NonNull
    @Override
    public RecordHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View myView = LayoutInflater.from(context).inflate(R.layout.song_item,viewGroup,false);
        return new RecordHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecordHolder recordHolder, final int i) {
        final RecordInfo recordInfo = records.get(i);
        recordHolder.number.setText(recordInfo.number);
        recordHolder.date.setText(recordInfo.date);
        recordHolder.duration.setText(recordInfo.duration);

        recordHolder.btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(recordHolder.btnAction,view,recordInfo,i);
                }
            }
        });

        recordHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListenerDelete != null){
                    onItemClickListenerDelete.onItemClick(recordHolder.btnDelete,view,recordInfo,i);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return records.size();
    }


    public interface OnItemClickListener {
        void onItemClick(Button b,View v,RecordInfo obj,int position);
    }

    public void setOnItemClickListener(final OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemClickListenerDelete(final OnItemClickListener onItemClickListener){
        this.onItemClickListenerDelete = onItemClickListener;
    }



    public class RecordHolder extends RecyclerView.ViewHolder {
        TextView number,date,duration;
        Button btnAction,btnDelete;
        public RecordHolder(@NonNull View itemView) {
            super(itemView);

            number = (TextView) itemView.findViewById(R.id.number);
            date = (TextView) itemView.findViewById(R.id.date);
            duration = (TextView) itemView.findViewById(R.id.duration);
            btnAction = (Button) itemView.findViewById(R.id.button);
            btnDelete = (Button) itemView.findViewById(R.id.button2);
        }
    }
}
