package com.example.notes;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder> {

    private List<Event> mDataset;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public CalendarAdapter(List<Event> myDataset) {
        mDataset = myDataset;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;
        public TextView mContent;
        public TextView mDate;

        public MyViewHolder(View pItem) {
            super(pItem);
            mTitle = (TextView) pItem.findViewById(R.id.viewTitle);
            mContent = (TextView) pItem.findViewById(R.id.viewContent);
            mDate = (TextView) pItem.findViewById(R.id.viewDate);
        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public CalendarAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_view, parent, false);

        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String[] myData = mDataset.get(position).getData().toString().split(",");
        holder.mTitle.setText(myData[0].substring(1));
        holder.mContent.setText(myData[1]);

        Long tempDate = Long.parseLong(myData[2].substring(1, myData[2].length() - 4) + "000");
        Date date = new Date(tempDate);


        //Date date = new java.util.Date(Long.parseLong(tempDate));

        holder.mDate.setText(dateFormat.format(date));


    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}