package com.vsu.nastya.partymanager.party_details;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.text.TextDirectionHeuristicCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;
import com.vsu.nastya.partymanager.party_list.Party;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Окно для просмотра и изменения основной информации о вечеринке (название, место, дата, время)
 */
public class PartyInfoFragment extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMapClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String SECURITY_ERROR = "security_error";
    private static final String GEOCODER_ERROR = "geocoder_error";

    private Party currentParty;

    private String place;
    private Geocoder geocoder;
    private MapView mapView;
    private GoogleMap map;
    private Marker currentMarker;

    private TextView textView;
    private PartyDetailsActivity activity;

    private DatabaseReference partiesReference;

    public PartyInfoFragment() {
    }

    public static PartyInfoFragment newInstance() {
        return new PartyInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_party_info, container, false);
        textView = (TextView) view.findViewById(R.id.party_info_text);
        mapView = (MapView) view.findViewById(R.id.party_info_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //получаем информацию о вечеринке от родительской активити
        PartyDetailsActivity activity = (PartyDetailsActivity) getActivity();
        this.currentParty = activity.getCurrentParty();

        geocoder = new Geocoder(activity, Locale.getDefault());

        partiesReference = FirebaseDatabase.getInstance().getReference().child("parties");

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (PartyDetailsActivity) getActivity();
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
            textView.setText(place);
            updatePartyInfo(place);
        }
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
        try {
            Address address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
            return address.getAddressLine(0);
        } catch (IOException e) {
            Log.d(GEOCODER_ERROR, "Geocoder Error: " + e);
        }
        return null;
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
     * если если не известно, то к дефолтному местоположению.
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
                textView.setText(getPlaceName(latLng));
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
}
