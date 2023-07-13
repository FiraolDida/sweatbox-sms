package com.example.sweatbox_sms_test;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.sweatbox_sms_test.Interfaces.MyAPIService;
import com.example.sweatbox_sms_test.Interfaces.RefreshableFragment;
import com.example.sweatbox_sms_test.Models.UserModel;
import com.example.sweatbox_sms_test.Utils.ListAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewlyRegisteredFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewlyRegisteredFragment extends Fragment implements RefreshableFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private static final String TAG = "NewlyRegisteredFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //    private static final String API_URL = "http://10.0.2.2:8000/api/"; // for dev
    private static final String API_URL = "https://sweatbox-backend-production.up.railway.app/api/";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NewlyRegisteredFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewlyRegisteredFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewlyRegisteredFragment newInstance(String param1, String param2) {
        NewlyRegisteredFragment fragment = new NewlyRegisteredFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_newly_registered, container, false);
        recyclerView = view.findViewById(R.id.newlyRegisteredRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void fetchUser() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(100, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        MyAPIService myAPIService = retrofit.create(MyAPIService.class);

        Call<List<UserModel>> call = myAPIService.getNewlyRegisteredUsers();
        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.code());
                }

                List<UserModel> userModelList = response.body();
                ListAdapter listAdapter = new ListAdapter(userModelList, getActivity());
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(listAdapter);
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}