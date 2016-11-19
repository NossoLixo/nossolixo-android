package br.com.nossolixo.nossolixo.services;

import java.util.List;

import br.com.nossolixo.nossolixo.models.Place;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlaceService {
    @GET("places")
    Call<List<Place>> listPlaces();

    @GET("places")
    Call<List<Place>> listPlacesByCategory(@Query("category") String category);
}
