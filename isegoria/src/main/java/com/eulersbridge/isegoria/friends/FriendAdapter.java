package com.eulersbridge.isegoria.friends;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Contact;
import com.eulersbridge.isegoria.network.api.models.GenericUser;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<UserViewHolder> implements UserViewHolder.OnClickListener {

    interface Delegate {
        void getContactInstitution(long institutionId, WeakReference<UserViewHolder> weakViewHolder);
        void onContactClick(@NonNull Contact contact);
    }

    final private List<Contact> items = new ArrayList<>();
    final private Delegate delegate;

    FriendAdapter(Delegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onViewClick(@Nullable GenericUser user) {
        if (user != null && delegate != null)
            delegate.onContactClick((Contact) user);
    }

    @Override
    public void onActionClick(@Nullable GenericUser user) {
        onViewClick(user);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.friend_partial_list_item, parent, false);

        return new UserViewHolder(itemView, R.drawable.profileactive, this);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        Contact item = items.get(position);
        holder.setItem(item);

        if (delegate != null && item.institutionId != null) {
            WeakReference<UserViewHolder> weakViewHolder = new WeakReference<>(holder);
            delegate.getContactInstitution(item.institutionId, weakViewHolder);
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

    void setItems(@NonNull List<Contact> newItems) {
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
                Contact oldItem = items.get(oldItemPosition);
                Contact newItem = items.get(newItemPosition);
                return Strings.equal(oldItem.email, newItem.email);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Contact oldItem = items.get(oldItemPosition);
                Contact newItem = items.get(newItemPosition);
                return oldItem.equals(newItem);
            }
        });

        items.clear();
        items.addAll(newItems);

        result.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
