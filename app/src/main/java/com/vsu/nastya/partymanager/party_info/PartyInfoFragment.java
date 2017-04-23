package com.vsu.nastya.partymanager.party_info;

import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.logic.Notifications;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;
import com.vsu.nastya.partymanager.party_list.Party;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Окно для выбора места проведения вечеринки (карта google и поле ввода).
 */
public class PartyInfoFragment extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMapClickListener, TextWatcher,
        TextView.OnEditorActionListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String SECURITY_ERROR = "security_error";
    private static final String GEOCODER_ERROR = "geocoder_error";
    private static final String FIREBASE_ERROR = "firebase_error";
    private static final String PREDICTION_QUERY = "query";
    private static final float MAP_SCALE = 16;

    private Party currentParty;
    private boolean doNotCallListener = false;
    private Subscription subscription;

    // Google Map
    private String place;
    private Geocoder geocoder;
    private MapView mapView;
    private GoogleMap map;
    private Marker currentMarker;
    private GoogleApiClient googleApiClient;

    // Search line
    private AutoCompleteTextView autoTextView;
    private ArrayAdapter<String> adapter;
    private ToggleButton confirmButton;

    private PartyDetailsActivity activity;

    // Firebase
    private DatabaseReference partyReference;
    private DatabaseReference placeReference;
    private ValueEventListener placeListener;

    public PartyInfoFragment() {
    }

    public static PartyInfoFragment newInstance() {
        return new PartyInfoFragment();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            doNotCallListener = true;
            autoTextView.setText(savedInstanceState.getString("text"));
            doNotCallListener = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_party_info, container, false);
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

        mapView = (MapView) activity.findViewById(R.id.party_info_map_view);
        mapView.onCreate(savedInstanceState);

        if (arePermissionsGranted()) {
            mapView.getMapAsync(this);
        } else {
            requestPermissions();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        attachDatabaseReadListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("text", autoTextView.getText().toString());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        map.setOnMapClickListener(this);
        mapView.onResume();
        if (place == null) {
            setUpMap();
        }
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
        }
        autoTextView.setSelection(autoTextView.getText().length());
        autoTextView.setCursorVisible(true);
        confirmButton.setClickable(true);
        confirmButton.setChecked(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.party_info_text) {
            AutoCompleteTextView textView = (AutoCompleteTextView) v;
            textView.setCursorVisible(true);
        }
    }

    /**
     * По нажатию на confirmButton пользователь подтверждает место проведения вечеринки.
     * Карта перемещается в выбранное место, данные о месте проведения отправляются на сервер.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.party_info_confirm_button) {
            if (isChecked) {
                place = autoTextView.getText().toString();
                setMarker(place);
                hidePredictions();
                updatePartyInfo(place);
                buttonView.setClickable(false);
                autoTextView.setCursorVisible(false);
                Toast.makeText(activity, getResources().getString(R.string.place_is_set), Toast.LENGTH_SHORT).show();
            }
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
            if (!place.equals("")) {
                handled = true;
                hideKeyboard();
                ((AutoCompleteTextView)text).dismissDropDown();
                setMarker(text.getText().toString());
            }
        }
        return handled;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence query, int start, int before, int count) {
        if (!doNotCallListener) {
            Bundle bundle = new Bundle();
            bundle.putString(PREDICTION_QUERY, autoTextView.getText().toString());;

            if (!query.equals("")) {
                 subscription = getPredictions(query.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(Observable::from)
                        .map(prediction -> prediction.getFullText(null))
                        .map(CharSequence::toString)
                        .map(this::getAddress)
                        .toList()
                        .subscribe(predictions -> {
                            adapter.clear();
                            adapter.addAll(predictions);
                            adapter.notifyDataSetChanged();
                        });
            }

            confirmButton.setChecked(false);
            confirmButton.setClickable(true);
        } else {
            autoTextView.dismissDropDown();
        }
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
        } else {
            mapView.getMapAsync(this);
        }
    }

    private void init(View view) {
        //получаем информацию о вечеринке от родительской активити
        PartyDetailsActivity activity = (PartyDetailsActivity) getActivity();
        this.currentParty = activity.getCurrentParty();

        geocoder = new Geocoder(activity, Locale.getDefault());

        partyReference = FirebaseDatabase.getInstance().getReference().child("parties").child(currentParty.getKey());
        placeReference = partyReference.child("place");

        autoTextView = (AutoCompleteTextView) view.findViewById(R.id.party_info_text);
        autoTextView.addTextChangedListener(this);
        autoTextView.setOnEditorActionListener(this);
        autoTextView.setOnClickListener(this);
        autoTextView.setCursorVisible(false);

        confirmButton = (ToggleButton) view.findViewById(R.id.party_info_confirm_button);
        confirmButton.setClickable(false);
        confirmButton.setOnCheckedChangeListener(this);
    }

    /**
     *  Получение списка подсказок при вводе адреса пользователем.
     *  Используется Google Places Api.
     * @param query запрос пользователя.
     * @return Observable список.
     */
    private Observable<AutocompletePredictionBuffer> getPredictions(String query) {

        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(-0,0), new LatLng(0,0));
        PendingResult<AutocompletePredictionBuffer> result = Places.
                GeoDataApi.getAutocompletePredictions(googleApiClient, query, latLngBounds, new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build());
        return Observable.just(result)
                .map(res -> res.await(60, TimeUnit.SECONDS));
    }

    /**
     * Получение адреса из полной строки (подсказки) в формате: Улица, Город или
     * Улица, номер дома, Город.
     */
    private String getAddress (String prediction) {
        String [] str = prediction.split(",");
        if (str.length == 4) {
            return str[0] + ", " + str[1];
        } else if (str.length == 5) {
            return  str[0] + ", " + str[1] + ", " + str[2];
        } else {
            return prediction;
        }
    }

    private boolean arePermissionsGranted() {
        return (ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        this.requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_CODE);
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
                placeName = address.getAddressLine(0) + ", " + address.getAddressLine(1);
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
        partyReference.updateChildren(task);
    }

    /**
     * Настройка карты: просто приближаем карту к последнему известному местоположению пользователя или,
     * если не известно, то к дефолтному местоположению.
     */
    private void setUpMap() {
        LatLng latLng;
        if ((latLng = getLastKnownCoordinates()) == null) {
                latLng = getDefaultCoordinates();
        }
        if (map != null) {
            try {
                map.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Log.d(SECURITY_ERROR, "SecurityException: " + e);
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_SCALE));
        }
    }

    /**
     * Ставим маркер на карту.
     * @param text адрес, для которого ставится маркер.
     */
    private void setMarker(String text) {
        LatLng latLng = getCoordinates(text);
        if (latLng != null) {
            if (currentMarker != null) {
                currentMarker.remove();
            }
            currentMarker = map.addMarker(new MarkerOptions()
                    .position(latLng));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_SCALE));
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void hidePredictions() {
        adapter.clear();
        adapter.notifyDataSetChanged();
        autoTextView.dismissDropDown();
    }

    /**
     * Отслеживание изменения в базе места проведения вечеринки.
     */
    private void attachDatabaseReadListener() {
        if (placeListener == null) {

            placeListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    place = (String) dataSnapshot.getValue();
                    if (place != null) {
                        doNotCallListener = true;
                        autoTextView.setText(place);
                        doNotCallListener = false;
                        setMarker(place);
                    } else {
                        confirmButton.setChecked(false);
                        confirmButton.setClickable(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Обычно вызывается, когда нет прав на чтение данных из базы
                    Log.d(FIREBASE_ERROR, "onCancelled: " + databaseError);
                }
            };
            placeReference.addValueEventListener(placeListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (placeListener != null) {
            placeReference.removeEventListener(placeListener);
            placeListener = null;
        }
    }
}
