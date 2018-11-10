package ifreecomm.nettyserver.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ifreecomm.nettyserver.bean.ClientChanel;

/**
 * Created by  IT小蔡 on 2018-11-10.
 */

public class CustomSpinnerAdapter extends BaseAdapter {

    private int layoutId;
    private Context mContext;
    private List<ClientChanel> mData;
    private final LayoutInflater mInflater;

    public CustomSpinnerAdapter(@NonNull Context context, List<ClientChanel> list) {
        this.mContext = context;
        this.mData = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public ClientChanel getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        final TextView text;

        if (convertView == null) {
            view = mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        } else {
            view = convertView;
        }

        text = (TextView) view;
        ClientChanel item = getItem(position);
        text.setText(item.getClientIp());

        return view;
    }
}
