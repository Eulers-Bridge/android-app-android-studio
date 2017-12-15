package com.eulersbridge.isegoria.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ClickableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

    public interface ClickListener {
        void onItemClick(RecyclerView.ViewHolder viewHolder, int position);
    }

    private @NonNull final ClickListener onClickListener;

    protected ClickableViewHolder(View view, @NonNull ClickListener onClickListener) {
        super(view);

        this.onClickListener = onClickListener;
    }

    @Override
    public void onClick(View view) {
        onClickListener.onItemClick(this, getAdapterPosition());
    }
}
