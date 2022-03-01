package com.longseong.logcenter.log;

import static com.longseong.logcenter.Utils.Now;
import static com.longseong.logcenter.Utils.FLAG_DATE;
import static com.longseong.logcenter.Utils.FLAG_HOUR;
import static com.longseong.logcenter.Utils.FLAG_MILLIS;
import static com.longseong.logcenter.Utils.FLAG_MINUTE;
import static com.longseong.logcenter.Utils.FLAG_MONTH;
import static com.longseong.logcenter.Utils.FLAG_SECOND;
import static com.longseong.logcenter.Utils.enhancedFormatDate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.longseong.logcenter.R;

import java.util.Calendar;
import java.util.LinkedList;

public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.ViewHolder> {

    private LogActivity mActivity;
    private LinkedList<LogCenter.Log> mLogDataList;

    public LogListAdapter(LogActivity logActivity) {
        mActivity = logActivity;
        setData();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.logcenter_list_log_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LogCenter.Log logData = mLogDataList.get(position);

            long time = logData.getMilliSecond();
            String stackTracedString = logData.getLogString();

            Calendar calendar = Now();
            calendar.setTimeInMillis(time);

            String timeText = enhancedFormatDate(holder.itemView.getContext(), calendar, FLAG_MONTH | FLAG_DATE | FLAG_HOUR | FLAG_MINUTE | FLAG_SECOND | FLAG_MILLIS) + " (" + time + ")";
            holder.time.setText(timeText);
            holder.stackTrace.setText(stackTracedString);

    }

    @Override
    public int getItemCount() {
        return mLogDataList.size();
    }

    public void setData() {
        mLogDataList = LogCenter.getLogSet(mActivity);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{

        TextView time;
        TextView stackTrace;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.tv_time);
            stackTrace = itemView.findViewById(R.id.tv_stack_trace);
        }
    }
}
