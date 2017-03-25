package com.vsu.nastya.partymanager.party_info;

import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.support.v4.content.Loader;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;
import com.vsu.nastya.partymanager.party_list.Party;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Окно для выбора места проведения вечеринки (карта google и поле ввода).
 */
public class PartyInfoFragment extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMapClickListener, TextWatcher,
        TextView.OnEditorActionListener, View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String SECURITY_ERROR = "security_error";
    private static final String GEOCODER_ERROR = "geocoder_error";
    private static final String PREDICTION_QUERY = "query";

    private Party currentParty;

    private String place;
    private Geocoder geocoder;
    private MapView mapView;
    private GoogleMap map;
    private Marker currentMarker;

    private GoogleApiClient googleApiClient;
    private AutoCompleteTextView autoTextView;
    private ArrayAdapter<String> adapter;

    private PartyDetailsActivity activity;

    private DatabaseReference partiesReference;

    public PartyInfoFragment() {
    }

    public static PartyInfoFragment newInstance() {
        return new PartyInfoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_party_info, container, false);
        mapView = (MapView) view.findViewById(R.id.party_info_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        init(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (PartyDetailsActivity) getActivity();

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient
                    .Builder(activity)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .build();
        }
        googleApiClient.connect();
        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, new ArrayList<String>());
        autoTextView.setAdapter(adapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Проверка прав
        if (ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        this.map = googleMap;
        setUpMarker();
        map.setOnMapClickListener(this);
        mapView.onResume();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = map.addMarker(new MarkerOptions()
                .position(latLng));
        place = getPlaceName(latLng);
        if (place != null) {
            autoTextView.setText(place);
            updatePartyInfo(place);
        }
        autoTextView.setSelection(autoTextView.getText().length());
        autoTextView.setCursorVisible(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.party_info_text) {
            AutoCompleteTextView textView = (AutoCompleteTextView) v;
            textView.setSelection(textView.getText().length());
            textView.setCursorVisible(true);
        }
    }

    /**
     * Метод, обрабатывающий нажатие кнопки поиска на клавиатуре.
     * Скрывает клавиатуру, перемещает камеру к введенному местоположению, ставит на это место маркер.
     */
    @Override
    public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            place = text.getText().toString();
            updatePartyInfo(place);

            if (!place.equals("")) {
                handled = true;
                updatePartyInfo(place);
                hideKeyboard();
                text.setCursorVisible(false);
                ((AutoCompleteTextView) text).dismissDropDown();
                LatLng latLng = getCoordinates(text.getText().toString());
                if (latLng != null) {
                    if (currentMarker != null) {
                        currentMarker.remove();
                    }
                    currentMarker = map.addMarker(new MarkerOptions()
                            .position(latLng));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                }
            }
        }
        return handled;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Bundle bundle = new Bundle();
        bundle.putString(PREDICTION_QUERY, autoTextView.getText().toString());
        getLoaderManager().restartLoader(0, bundle, callbacks);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length == 0 ||
                grantResults[0] == PackageManager.PERMISSION_DENIED &&
                        grantResults[1] == PackageManager.PERMISSION_DENIED) {

            Toast.makeText(activity, "Permission denied!", Toast.LENGTH_LONG).show();
        }
    }

    private void init(View view) {
        //получаем информацию о вечеринке от родительской активити
        PartyDetailsActivity activity = (PartyDetailsActivity) getActivity();
        this.currentParty = activity.getCurrentParty();

        geocoder = new Geocoder(activity, Locale.getDefault());
        
        partiesReference = FirebaseDatabase.getInstance().getReference().child("parties");
        autoTextView = (AutoCompleteTextView) view.findViewById(R.id.party_info_text);
        autoTextView.addTextChangedListener(this);
        autoTextView.setOnEditorActionListener(this);
        autoTextView.setOnClickListener(this);
        autoTextView.setCursorVisible(false);
    }

    /**
     * Получение координат последнего известного местополжения пользователя.
     * @return LatLng координаты. Если не известно - null.
     */
    private LatLng getLastKnownCoordinates() {
        try {
            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);

            Location location = null;
            for (int i = 0; i < providers.size(); i++) {
                location = locationManager.getLastKnownLocation(providers.get(i));
                if (location != null)
                    break;
            }

            if (location == null) {
                return null;
            } else {
                return new LatLng(location.getLatitude(), location.getLongitude());
            }

        } catch (SecurityException e) {
            Log.d(SECURITY_ERROR, "SecurityException: " + e);
        }
        return null;
    }

    private LatLng getDefaultCoordinates() {
        return new LatLng(-33.867, 151.206); // Sydney coordinates
    }

    /**
     * Получение адреса по координатам.
     */
    private String getPlaceName(LatLng latLng) {
        String placeName = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() != 0) {
                Address address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                placeName = address.getAddressLine(0);
            }
        } catch (IOException e) {
            Log.d(GEOCODER_ERROR, "Geocoder Error: " + e);
        }
        return placeName;
    }

    /**
     * Получения координат по названию места.
     */
    private LatLng getCoordinates(String placeName) {
        LatLng latLng = null;
        try {
            List<Address> addresses = geocoder.getFromLocationName(placeName, 1);
            if (addresses != null && addresses.size() != 0 ) {
                double lat = addresses.get(0).getLatitude();
                double lon = addresses.get(0).getLongitude();
                latLng = new LatLng(lat, lon);
            }
        } catch (IOException e) {
            Log.d(GEOCODER_ERROR, "Geocoder Error: " + e);
        }
        return latLng;
    }

    /**
     * Помещение информации о месте проведения вечеринки в базу данных на сервере.
     * @param place адрес
     */
    private void updatePartyInfo(String place) {
        currentParty.setPlace(place);
        HashMap<String, Object> task = new HashMap<>();
        task.put("place", currentParty.getPlace());
        partiesReference.child(currentParty.getKey()).updateChildren(task);
    }

    /**
     * Устанавливаем маркер на карту, если для текущей вечеринки было выбрано место проведения.
     * Если не выбрано, просто приближаем карту к последнему известному местоположению пользователя или,
     * если не известно, то к дефолтному местоположению.
     */
    private void setUpMarker() {
        LatLng latLng;
        if ((place = currentParty.getPlace()) == null) {
            if ((latLng = getLastKnownCoordinates()) == null) {
                latLng = getDefaultCoordinates();
            }
        } else {
            try {
                double lat = geocoder.getFromLocationName(place, 1).get(0).getLatitude();
                double lon = geocoder.getFromLocationName(place, 1).get(0).getLongitude();
                latLng = new LatLng(lat, lon);
                currentMarker = map.addMarker(new MarkerOptions()
                        .position(latLng));
                autoTextView.setText(getPlaceName(latLng));
            } catch (IOException e) {
                latLng = getDefaultCoordinates();
                Log.d(GEOCODER_ERROR, "Geocoder Error: " + e);
            }
        }

        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.d(SECURITY_ERROR, "SecurityException: " + e);
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Callbacks к PredictionLoader.
     */
    private LoaderManager.LoaderCallbacks<List<String>> callbacks = new LoaderManager.LoaderCallbacks<List<String>>() {
        @Override
        public Loader<List<String>> onCreateLoader(int id, Bundle args) {
            return new PredictionsLoader(activity, googleApiClient, args.getString(PREDICTION_QUERY));
        }

        /**
         * Полученныe от серевера подсказки мест добавляются в адаптер AutocompleteTextView.
         * @param data список подсказок.
         */
        @Override
        public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
            adapter.clear();
            adapter.addAll(data);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<String>> loader) {

        }
    };

}
