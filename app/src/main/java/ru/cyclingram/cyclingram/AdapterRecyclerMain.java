package ru.cyclingram.cyclingram;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterRecyclerMain extends RecyclerView.Adapter<AdapterRecyclerMain.ViewHolder> {
    private ArrayList<Block> blocks;
    private Context context;
    public AdapterListener onClickListener;

    public AdapterRecyclerMain (Context context, ArrayList<Block> blocks, AdapterListener listener) {
        this.context = context;
        this.blocks = blocks;
        this.onClickListener = listener;
    }

    @Override
    public AdapterRecyclerMain.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.block_main, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        viewHolder.block_head.setText(blocks.get(i).getHeader());
        viewHolder.block_text.setText(blocks.get(i).getText());
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView block_head, block_text;
        Button block_btn;

        public ViewHolder(View view) {
            super(view);
            block_head = view.findViewById(R.id.block_head);
            block_text = view.findViewById(R.id.block_text);
            block_btn = view.findViewById(R.id.block_btn);

            block_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.btnOnClick(v,getAdapterPosition());
                }
            });
        }
    }

    public interface AdapterListener {
        void btnOnClick (View v, int position);
    }
}
