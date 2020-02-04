package com.ait.dragrecycleritem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ait.dragrecycleritem.model.DistanceMatrixModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements OnStartDragListener, MapListener {
    RecyclerAdapter adapter;
    RecyclerView recyclerView;
    private ItemTouchHelper mItemTouchHelper;
    CardView emptyLayout;
    private AlertDialog globalalertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        emptyLayout = findViewById(R.id.layout_empty);
        Utils utils = new Utils();
        if (utils.isLocationEnabled(this)) {
            if (ActivityCompat.checkSelfPermission(this,
                    ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationSupportManager manager = new LocationSupportManager(this, this);
                manager.buildLocationRequest();
                manager.buildLocationCallBacks();
                manager.displayLocation();
                if (Constants.datalist.size() == 0) {
                    getDummyData();
                }
                if (Constants.datalist.size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                } else {
                    setAdapter();
                }
            } else {
                askLocationPermission();
            }
        } else {
            utils.showlocationAlert(this);
        }
    }

    private void setAdapter() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
        adapter = new RecyclerAdapter(Constants.datalist, this);
        ItemTouchHelper.Callback callback = new EditItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        mItemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.updateList(Constants.datalist);
        adapter.notifyDataSetChanged();
    }

    private void getDummyData() {
        Constants.datalist.add(new Model("1. Sree Bagavathi Gardens", new LatLng(11.054936, 76.933393)));//18.67
        Constants.datalist.add(new Model("2. Able Electronics", new LatLng(11.050640, 76.940630)));//904.76
        Constants.datalist.add(new Model("3. Gandhi Nagar", new LatLng(11.052930, 76.936961)));//430.66
        Constants.datalist.add(new Model("4. Balaji Gardens", new LatLng(11.053674, 76.934517)));//170.96
    }

    private void askLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) this, ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions((Activity) this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 100);
            } else {
                /*if (!notFirst) {
                    ActivityCompat.requestPermissions((Activity) getContext(), new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, Constants.getLocationRequestCode());


                    preferenceManager.storeBoolsData(Constants.getLocationPermission(), false);
                    notFirst = true;
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.enable_location), Toast.LENGTH_SHORT).show();

                }*/
            }
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void draw(View view) {
        Constants.datalist = adapter.getDataList();
        if(Constants.datalist.size()>1){
        startActivity(new Intent(this, MapActivity.class));}else {
            Toast.makeText(this, "Please add location", Toast.LENGTH_SHORT).show();
        }
    }

    public void add(View view) {
        startActivity(new Intent(this, AddLocationActivity.class));
    }

    public void optimze(View view) {
        showOptimizeSelectionTypeView();
    }


    private void showOptimizeSelectionTypeView() {

        View optimizeTypeView = getLayoutInflater().inflate(R.layout.optimize_type_selection_layout, null);
        TextView fromMyLocation, keepStart, keepStartEnd;
        Button close;

        close = (Button) optimizeTypeView.findViewById(R.id.close_optimize_type);


        fromMyLocation = (TextView) optimizeTypeView.findViewById(R.id.from_my_location);


        keepStart = (TextView) optimizeTypeView.findViewById(R.id.keep_start);


        keepStartEnd = (TextView) optimizeTypeView.findViewById(R.id.keep_start_and_end);


        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
        alertDialog.setView(optimizeTypeView);
        globalalertDialog = alertDialog.create();
        globalalertDialog.show();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globalalertDialog.dismiss();
            }
        });
        fromMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromCurrentLocation();
            }
        });

        keepStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keepStartLocation();
            }
        });

        keepStartEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keepStartEndLocation();
            }
        });
    }

    private void fromCurrentLocation() {
        if (Constants.currentLocation != null) {
            Location currentLocation = new Location("");
            currentLocation.setLatitude(Constants.currentLocation.latitude);
            currentLocation.setLongitude(Constants.currentLocation.longitude);
            int i = 0;
            for (Model model : Constants.datalist) {
                Location modelLocation = new Location("");
                LatLng modelLatLng = model.getLatLng();
                modelLocation.setLatitude(modelLatLng.latitude);
                modelLocation.setLongitude(modelLatLng.longitude);
                float dist = currentLocation.distanceTo(modelLocation);
                model.setDistance(dist);
                Constants.datalist.set(i, model);
                Log.d("DistanceMatrix Distance", "" + dist);
                getDistanceMatrixApi(currentLocation,modelLocation);
                i++;
            }

            Collections.sort(Constants.datalist, new Comparator<Model>() {
                @Override
                public int compare(Model t1, Model t2) {
                    return (int) (t1.getDistance() - t2.getDistance());
                }
            });

            for (Model model : Constants.datalist) {
                Log.d("After sort Distance", "" + model.getDistance());
            }
        }
        globalalertDialog.dismiss();
        adapter.updateList(Constants.datalist);
        adapter.notifyDataSetChanged();

    }

    private void keepStartLocation() {
        Location startLocation = new Location("start");
        startLocation.setLatitude(Constants.datalist.get(0).getLatLng().latitude);
        startLocation.setLongitude(Constants.datalist.get(0).getLatLng().longitude);
        int i = 0;
        for (Model model : Constants.datalist) {
            Location modelLocation = new Location("");
            LatLng modelLatLng = model.getLatLng();
            modelLocation.setLatitude(modelLatLng.latitude);
            modelLocation.setLongitude(modelLatLng.longitude);
            float dist = startLocation.distanceTo(modelLocation);
            if (i != 0) {
                model.setDistance(dist);
            } else {
                model.setDistance(0.0f);
            }
            Constants.datalist.set(i, model);
            Log.d("Distance", "" + dist);
            globalalertDialog.dismiss();
            adapter.updateList(Constants.datalist);
            adapter.notifyDataSetChanged();
            i++;
        }
        Collections.sort(Constants.datalist, new Comparator<Model>() {
            @Override
            public int compare(Model t1, Model t2) {
                return (int) (t1.getDistance() - t2.getDistance());
            }
        });

        for (Model model : Constants.datalist) {
            Log.d("After sort Distance", "" + model.getDistance());
        }
    }

    private void keepStartEndLocation() {
        Location startLocation = new Location("start");
        ArrayList<Model> list = Constants.datalist;
        startLocation.setLatitude(list.get(0).getLatLng().latitude);
        startLocation.setLongitude(list.get(0).getLatLng().longitude);

        Location endLocation = new Location("end");
        endLocation.setLatitude(list.get(list.size() - 1).getLatLng().latitude);
        endLocation.setLongitude(list.get(list.size() - 1).getLatLng().longitude);

        ArrayList<Model> finalList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {

            Location tempLocation = new Location("temp");
            tempLocation.setLatitude(list.get(i).getLatLng().latitude);
            tempLocation.setLongitude(list.get(i).getLatLng().longitude);
            float distance = startLocation.distanceTo(tempLocation);
            list.get(i).setDistance(distance);
            if (i == 0) {
                list.get(i).setDistance(0.0f);
                finalList.add(list.get(i));
            } else if (i == Constants.datalist.size() - 1) {
                Collections.sort(finalList, new Comparator<Model>() {
                    @Override
                    public int compare(Model t1, Model t2) {
                        return (int) (t1.getDistance() - t2.getDistance());
                    }
                });
                finalList.add(list.get(i));
            } else {
                finalList.add(list.get(i));
            }

        }
        Constants.datalist = (ArrayList<Model>) finalList.clone();
        globalalertDialog.dismiss();
        adapter.updateList(Constants.datalist);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void getLocation(Location location) {
        Constants.currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

    }

    @Override
    public void onRouteObtained(PolylineOptions routes) {

    }

    public void clear(View view) {
        Constants.datalist.clear();
        recyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void getDistanceMatrixApi(Location origin, Location destination){
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric");
        stringBuilder.append("&origins=");
        stringBuilder.append(String.valueOf(origin.getLatitude()));
        stringBuilder.append(",");
        stringBuilder.append(String.valueOf(origin.getLongitude()));
        stringBuilder.append("&destinations=");
        stringBuilder.append(String.valueOf(destination.getLatitude()));
        stringBuilder.append(",");
        stringBuilder.append(String.valueOf(destination.getLongitude()));
        stringBuilder.append("&key=");
        stringBuilder.append(getResources().getString(R.string.DistanceMatrixApi));

        ApiInterface mService = GoogleMapAPI.getClient("https://maps.googleapis.com").create(ApiInterface.class);
        Call<DistanceMatrixModel> call = mService.getDistanceMatrixApi(stringBuilder.toString());
        call.enqueue(new Callback<DistanceMatrixModel>() {
            @Override
            public void onResponse(Call<DistanceMatrixModel> call, Response<DistanceMatrixModel> response) {
                if(response.isSuccessful()){
                    try {

                        Log.d("","----------------DistanceMatrix------------------------");
                        Log.d("DistanceMatrix",response.body().getOriginAddresses().toString());
                        Log.d("DistanceMatrix",response.body().getDestinationAddresses().toString());
                        Log.d("DistanceMatrix",response.body().getRows().get(0).getElements().get(0).getDistance().getText());
                        Log.d("DistanceMatrix",response.body().getRows().get(0).getElements().get(0).getDuration().getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else {
                    Log.d("DistanceMatrix",response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<DistanceMatrixModel> call, Throwable t) {
                Log.d("DistanceMatrix",t.getMessage());
            }
        });
    }
}


class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private ArrayList<Model> dataList;
    OnStartDragListener mDragStartListener;

    public RecyclerAdapter(ArrayList<Model> dataList, OnStartDragListener dragListner) {
        this.dataList = dataList;
        mDragStartListener = dragListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.location.setText(dataList.get(position).getAddress());
        holder.textDist.setText("Distance: " + String.valueOf(dataList.get(position).getDistance()));
        holder.handle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        //Log.v("", "Log position" + fromPosition + " " + toPosition);
        if (fromPosition < dataList.size() && toPosition < dataList.size()) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(dataList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(dataList, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }
        return true;
    }


    @Override
    public void onItemDismiss(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    public ArrayList<Model> getDataList() {
        return dataList;
    }

    public void updateList(ArrayList<Model> list) {
        dataList = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        TextView location, textDist;
        ImageView handle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.text);
            handle = itemView.findViewById(R.id.handle);
            textDist = itemView.findViewById(R.id.text_dist);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

}