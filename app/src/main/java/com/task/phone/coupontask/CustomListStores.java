package com.task.phone.coupontask;

import android.app.Activity;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by adhiraj on 8/9/15.
 */
public class CustomListStores extends ArrayAdapter<String> {
    private JSONArray jsonArray;
    LayoutInflater inflater;
    public CustomListStores(Activity context, JSONArray jsonArray) {
        super(context, R.layout.list_row);
        this.jsonArray = jsonArray;
        this.inflater = context.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return this.jsonArray.length();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        view = inflater.inflate(R.layout.list_row, null);

        TextView txtStoreName = (TextView) view.findViewById(R.id.txtStoreName);
        ImageView logoStore = (ImageView) view.findViewById(R.id.logoStore);
        TextView distanceStore = (TextView) view.findViewById(R.id.distanceStore);
        TextView nearbyLocation = (TextView) view.findViewById(R.id.nearbyLocation);

                try {

                    Object str = jsonArray.get(position);
                    JSONObject jobj = new JSONObject(str.toString());

                    String brandName = jobj.getString("BrandName");
                    Double distance = jobj.getDouble("distance");
                    String s = String.format("%.2f", distance);
                    String NeighbourhoodName = jobj.getString("NeighbourhoodName");
                    String logoURL = jobj.getString("LogoURL");
                    Picasso.with(getContext())
                            .load(logoURL)
                            .placeholder(R.mipmap.ic_launcher) // optional
                            .error(R.mipmap.ic_launcher)
                            .into(logoStore);


                    txtStoreName.setText(brandName);
                    distanceStore.setText(s+" Km");
                    nearbyLocation.setText(NeighbourhoodName);

                } catch (JSONException e) {
                    e.printStackTrace();

                }


        return view;
    }
}


