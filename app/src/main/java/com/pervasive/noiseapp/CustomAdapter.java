package com.pervasive.noiseapp;

/**
 * Created by Federico Boarelli on 08/05/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomAdapter extends BaseAdapter{

    private String [] result;
    private String [] pos;
    private Context context;
    private int [] imageId;

    private static LayoutInflater inflater=null;
    public CustomAdapter(Stats stats, String[] prgmNameList, String[] prgmPosList, int[] prgmImages) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        pos=prgmPosList;
        context=stats;
        imageId=prgmImages;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        TextView tv2;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.customlayout, null);
        holder.tv=(TextView) rowView.findViewById(R.id.textView_name);
        holder.tv2=(TextView) rowView.findViewById(R.id.textView_description);
        holder.img=(ImageView) rowView.findViewById(R.id.imageView);
        holder.tv.setText(result[position]);
        holder.tv2.setText(pos[position]);
        holder.img.setImageResource(imageId[position]);
        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_LONG).show();
                Intent intent = new Intent(v.getContext(), SensorStats.class);
                intent.putExtra("sensorName", result[position]);
                context.startActivity(intent);
            }
        });
        return rowView;
    }

}


