package br.com.nossolixo.nossolixo.models;

import android.provider.BaseColumns;

public final class Filters {
    private Filters() {}

    public static class Filter implements BaseColumns {
        public static final String TABLE_NAME = "filter";
        public static final String COLUMN_NAME_CATEGORY_ID = "category_id";
        public static final String COLUMN_NAME_CATEGORY_NAME = "category_name";
    }
}
