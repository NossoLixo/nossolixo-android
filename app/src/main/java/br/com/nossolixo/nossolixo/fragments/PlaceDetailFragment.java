package br.com.nossolixo.nossolixo.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.nossolixo.nossolixo.R;
import br.com.nossolixo.nossolixo.adapters.PlaceCategoryListAdapter;
import br.com.nossolixo.nossolixo.helpers.ProgressDialogHelper;
import br.com.nossolixo.nossolixo.models.Category;
import br.com.nossolixo.nossolixo.models.Place;
import br.com.nossolixo.nossolixo.services.PlaceService;
import br.com.nossolixo.nossolixo.services.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "place_id";

    private Place place;
    private TextView placeDescription;
    private TextView placeAddress;
    private TextView placeSite;
    private TextView placePhoneNumber;
    private TextView placeEmail;
    private ListView placeCategories;

    public PlaceDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.place_detail, container, false);
        placeDescription = (TextView) rootView.findViewById(R.id.place_description);
        placeAddress = (TextView) rootView.findViewById(R.id.place_address);
        placePhoneNumber = (TextView) rootView.findViewById(R.id.place_phone_number);
        placeEmail = (TextView) rootView.findViewById(R.id.place_email);
        placeSite = (TextView) rootView.findViewById(R.id.place_site);
        placeCategories = (ListView) rootView.findViewById(R.id.place_categories);

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

                        if (place.getDescription() == null || place.getDescription().isEmpty()) {
                            placeDescription.setVisibility(View.GONE);
                        } else {
                            placeDescription.setVisibility(View.VISIBLE);
                            placeDescription.setText(place.getDescription());
                        }

                        if (place.getAddress() == null || place.getAddress().isEmpty()) {
                            placeAddress.setVisibility(View.GONE);
                        } else {
                            placeAddress.setVisibility(View.VISIBLE);
                            placeAddress.setText(place.getAddress());
                        }

                        if (place.getPhoneNumber() == null || place.getPhoneNumber().isEmpty()) {
                            placePhoneNumber.setVisibility(View.GONE);
                        } else {
                            placePhoneNumber.setVisibility(View.VISIBLE);
                            String phoneNumber = place.getPhoneNumber();
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                phoneNumber = PhoneNumberUtils.formatNumber(place.getPhoneNumber(), "BR");
                            }
                            placePhoneNumber.setText(phoneNumber);
                        }

                        if (place.getEmail() == null || place.getEmail().isEmpty()) {
                            placeEmail.setVisibility(View.GONE);
                        } else {
                            placeEmail.setVisibility(View.VISIBLE);
                            placeEmail.setText(place.getEmail());
                        }

                        if (place.getSite() == null || place.getSite().isEmpty()) {
                            placeSite.setVisibility(View.GONE);
                        } else {
                            placeSite.setVisibility(View.VISIBLE);
                            placeSite.setText(place.getSite());
                        }

                        if (!place.getCategories().isEmpty()) {
                            ArrayList<String> categoriesNames = new ArrayList<>();
                            for(Category category : place.getCategories()) {
                                categoriesNames.add(category.getName());
                            }
                            placeCategories.setAdapter(new PlaceCategoryListAdapter(activity,
                                    categoriesNames));
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

        return rootView;
    }
}
