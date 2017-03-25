package com.vsu.nastya.partymanager.party_info;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Загрузка подсказок адресов для autoCompleteTextView.
 * Используется Google Places Api.
 */
public class PredictionsLoader extends AsyncTaskLoader<List<String>> {

    private List<String> data;
    private String query;
    private GoogleApiClient googleApiClient;

    public PredictionsLoader(Context context, GoogleApiClient googleApiClient, String query) {
        super(context);
        this.googleApiClient = googleApiClient;
        this.query = query;
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    public List<String> loadInBackground() {
        data = new ArrayList<>();
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(-0,0), new LatLng(0,0));
        PendingResult<AutocompletePredictionBuffer> result = Places.
                GeoDataApi.getAutocompletePredictions(googleApiClient, query, latLngBounds, new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build());
        AutocompletePredictionBuffer autocompletePredictions = result.await(60, TimeUnit.SECONDS);

        for (AutocompletePrediction prediction : autocompletePredictions) {
            // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
            data.add(prediction.getFullText(null).toString());
        }
        return data;
    }
}
