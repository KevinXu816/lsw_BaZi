package lsw.liuyao.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import lsw.liuyao.HexagramAnalyzerActivity;
import lsw.liuyao.R;
import lsw.liuyao.common.IntentKeys;
import lsw.liuyao.model.HexagramRow;

import com.fortysevendeg.swipelistview.SwipeListView;

/**
 * Created by swli on 8/18/2015.
 */
public class HexagramListAdapter extends BaseAdapter {

    ArrayList<HexagramRow> rows;
    Context context;

    public HexagramListAdapter(ArrayList<HexagramRow> rows,  Context context)
    {
        this.rows = rows;
        this.context = context;
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Object getItem(int i) {
        return rows.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final HexagramRow item = rows.get(i);
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.hexagram_list_item, null);
            holder = new ViewHolder();
            holder.tvDate = (TextView) view.findViewById(R.id.tvDate);
            holder.tvOriginalName = (TextView) view.findViewById(R.id.tvOriginalName);
            holder.tvChangedName = (TextView) view.findViewById(R.id.tvChangedName);

            holder.btnAnalyze = (Button) view.findViewById(R.id.btnAnalyze);
            holder.btnEdit = (Button) view.findViewById(R.id.btnEdit);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        ((SwipeListView)viewGroup).recycle(view, i);

        holder.tvDate.setText(item.getDate().substring(0, 10));
        holder.tvOriginalName.setText(item.getOriginalName());
        holder.tvChangedName.setText(item.getChangedName());

        holder.btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent mIntent = new Intent(context,HexagramAnalyzerActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString(IntentKeys.FormatDate, item.getDate());
                mBundle.putString(IntentKeys.OriginalName,item.getOriginalName());
                mBundle.putString(IntentKeys.ChangedName,item.getChangedName());
                mIntent.putExtras(mBundle);

                context.startActivity(mIntent);
            }
        });

        return view;
    }

    static class ViewHolder {
        TextView tvDate;
        TextView tvOriginalName;
        TextView tvChangedName;
        Button btnAnalyze;
        Button btnEdit;
    }
}