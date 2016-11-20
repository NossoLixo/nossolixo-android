package br.com.nossolixo.nossolixo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.nossolixo.nossolixo.R;

public class PlaceCategoryListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;

    public PlaceCategoryListAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.place_categories, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.place_categories, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.category_name);
        textView.setText(values.get(position));

        return rowView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
