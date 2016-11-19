package br.com.nossolixo.nossolixo.services;

import java.util.List;

import br.com.nossolixo.nossolixo.models.Category;
import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryService {
    @GET("categories")
    Call<List<Category>> listCategories();
}
