package br.com.nossolixo.nossolixo.fragments;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import br.com.nossolixo.nossolixo.R;
import br.com.nossolixo.nossolixo.helpers.ProgressDialogHelper;
import br.com.nossolixo.nossolixo.models.Place;
import br.com.nossolixo.nossolixo.services.PlaceService;
import br.com.nossolixo.nossolixo.services.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "place_id";

    private Place place;

    public PlaceDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            final Activity activity = this.getActivity();

            final ProgressDialogHelper progressDialog = new ProgressDialogHelper(activity,
                    getResources().getString(R.string.loading));
            progressDialog.show();

            final CollapsingToolbarLayout appBarLayout =
                    (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

            PlaceService client = ServiceGenerator.createService(PlaceService.class);
            Call<Place> call = client.find(getArguments().getString(ARG_ITEM_ID));
            call.enqueue(new Callback<Place>() {
                @Override
                public void onResponse(Call<Place> call, Response<Place> response) {
                    if (response.isSuccessful()) {
                        place = response.body();
                        if (appBarLayout != null) {
                            appBarLayout.setTitle(place.getName());
                        }
                    } else {
                        Log.d("Error", String.valueOf(response.raw()));
                    }
                    if (progressDialog.isShowing()) {
                        progressDialog.hide();
                    }
                }

                @Override
                public void onFailure(Call<Place> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    if (progressDialog.isShowing()) {
                        progressDialog.hide();
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.place_detail, container, false);
        if (place != null) {
            ((TextView) rootView.findViewById(R.id.place_detail)).setText(place.getName());
        }
        return rootView;
    }
}
