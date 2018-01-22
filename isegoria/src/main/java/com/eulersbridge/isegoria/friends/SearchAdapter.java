package com.eulersbridge.isegoria.friends;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.GenericUser;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class SearchAdapter extends RecyclerView.Adapter<UserViewHolder> implements UserViewHolder.OnClickListener {

    interface UserDelegate {
        void getSearchedUserInstitution(long institutionId, WeakReference<UserViewHolder> weakViewHolder);
        void onSearchedUserClick(@Nullable User user);
        void onSearchedUserActionClick(@Nullable User user);
    }

    final private List<User> items = new ArrayList<>();
    final private UserDelegate delegate;

    SearchAdapter(UserDelegate delegate) {
        this.delegate = delegate;
    }

    void clearItems() {
        int oldItemCount = items.size();
        items.clear();
        notifyItemRangeRemoved(0, oldItemCount);
    }

    void setItems(@NonNull List<User> newItems) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                User oldItem = items.get(oldItemPosition);
                User newItem = items.get(newItemPosition);
                return Strings.equal(oldItem.email, newItem.email);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                User oldItem = items.get(oldItemPosition);
                User newItem = items.get(newItemPosition);
                return oldItem.equals(newItem);
            }
        });

        items.clear();
        items.addAll(newItems);

        result.dispatchUpdatesTo(this);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.friend_partial_list_item, parent, false);

        return new UserViewHolder(itemView, R.drawable.addedinactive, this);
    }

    @Override
    public void onBindViewHolder(UserViewHolder viewHolder, int position) {
        GenericUser item = items.get(position);
        viewHolder.setItem(item);

        if (delegate != null && item.institutionId != null) {
            WeakReference<UserViewHolder> weakViewHolder = new WeakReference<>(viewHolder);
            delegate.getSearchedUserInstitution(item.institutionId, weakViewHolder);
        }
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, position);
    }

    void setInstitution(@Nullable Institution institution, WeakReference<UserViewHolder> weakViewHolder) {
        UserViewHolder viewHolder = weakViewHolder.get();

        if (viewHolder != null && institution != null)
            viewHolder.setInstitution(institution);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onViewClick(@Nullable GenericUser user) {
        if (user != null && delegate != null)
            delegate.onSearchedUserClick((User) user);
    }

    @Override
    public void onActionClick(@Nullable GenericUser user) {
        if (user != null && delegate != null)
            delegate.onSearchedUserActionClick((User) user);
    }
}
