package ru.cyclingram.cyclingram;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterRecyclerMain extends RecyclerView.Adapter<AdapterRecyclerMain.ViewHolder> {
    private ArrayList<Block> blocks;
    private Context context;

    public AdapterRecyclerMain (Context context, ArrayList<Block> blocks) {
        this.context = context;
        this.blocks = blocks;
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
        public ViewHolder(View view) {
            super(view);
            block_head = view.findViewById(R.id.block_head);
            block_text = view.findViewById(R.id.block_text);

        }
    }
}
