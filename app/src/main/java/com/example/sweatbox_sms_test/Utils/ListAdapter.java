package com.example.sweatbox_sms_test.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sweatbox_sms_test.Models.UserModel;
import com.example.sweatbox_sms_test.R;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    List<UserModel> userModelList;
    Context context;

    public ListAdapter(List<UserModel> userModelList, Context context) {
        this.userModelList = userModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        UserModel userModel = userModelList.get(position);
        holder.id.setText(userModel.getColumn());
        holder.phoneNumber.setText(userModel.getPhone_number());
    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        TextView id, phoneNumber;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            id = itemView.findViewById(R.id.textId);
            phoneNumber = itemView.findViewById(R.id.textPhoneNumber);
        }
    }
}
