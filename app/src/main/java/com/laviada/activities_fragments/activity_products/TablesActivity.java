package com.laviada.activities_fragments.activity_products;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laviada.R;
import com.laviada.adapters.HomeAdapter;
import com.laviada.adapters.MarkAdapter;
import com.laviada.adapters.ProductAdapter;
import com.laviada.adapters.ProductDetialsAdapter;
import com.laviada.adapters.Table2Adapter;
import com.laviada.adapters.TableAdapter;
import com.laviada.databinding.ActivityProductsBinding;
import com.laviada.databinding.ActivityTablesBinding;
import com.laviada.language.Language;
import com.laviada.models.BrandDataModel;
import com.laviada.models.BrandModel;
import com.laviada.models.CategoryDataModel;
import com.laviada.models.CategoryModel;
import com.laviada.models.CreateOrderModel;
import com.laviada.models.ItemCartModel;
import com.laviada.models.ProductDataModel;
import com.laviada.models.ProductDetialsModel;
import com.laviada.models.ProductModel;
import com.laviada.models.SingleOrderDataModel;
import com.laviada.models.TableDataModel;
import com.laviada.models.TableModel;
import com.laviada.models.UserModel;
import com.laviada.preferences.Preferences;
import com.laviada.remote.Api;
import com.laviada.share.Common;
import com.laviada.tags.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TablesActivity extends AppCompatActivity {
    private ActivityTablesBinding binding;
    private Preferences preferences;
    private UserModel userModel;
    private String lang;
    private CreateOrderModel add_order_model;
    private List<TableModel> tableModelList;
    private Table2Adapter tableAdapter;

    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tables);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        add_order_model = (CreateOrderModel) intent.getSerializableExtra("data");
    }


    private void initView() {
        tableModelList = new ArrayList<>();

        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        tableAdapter = new Table2Adapter(tableModelList, this);
        binding.recView.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recView.setAdapter(tableAdapter);

        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        binding.setLang(lang);


        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createOrder();
            }
        });


        if (userModel != null) {
//            EventBus.getDefault().register(this);
//            getNotificationCount();
//            updateTokenFireBase();
//            updateLocation();
        }
        getTables();


    }

    private void getTables() {
        tableModelList.clear();
        tableAdapter.notifyDataSetChanged();

        Api.getService(Tags.base_url)
                .getTables()
                .enqueue(new Callback<TableDataModel>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(Call<TableDataModel> call, Response<TableDataModel> response) {
                        binding.progBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {

                            if (response.body() != null && response.body().getStatus() == 200) {
                                if (response.body().getData() != null) {
                                    if (response.body().getData().size() > 0) {
                                        if (add_order_model != null) {
                                            tableModelList.addAll(response.body().getData());
                                            tableAdapter.notifyDataSetChanged();
                                        } else {
                                        }

                                    } else {

                                    }
                                }
                            } else {

                                //  Toast.makeText(SignUpActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();

                            }


                        } else {
                            binding.progBar.setVisibility(View.GONE);

                            switch (response.code()) {
                                case 500:
                                    //   Toast.makeText(SignUpActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    //   Toast.makeText(SignUpActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            try {
                                Log.e("error_code", response.code() + "_");
                            } catch (NullPointerException e) {

                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<TableDataModel> call, Throwable t) {
                        try {


//                            binding.arrow.setVisibility(View.VISIBLE);
//
//                            binding.progBar.setVisibility(View.GONE);
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    //     Toast.makeText(SignUpActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    //  Toast.makeText(SignUpActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (Exception e) {

                        }
                    }
                });

    }

    public void choose(int id) {
        if (add_order_model != null) {
            add_order_model.setTable_id(id + "");
        }
    }

    public void createOrder() {
        try {
            ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();


            Api.getService(Tags.base_url)
                    .createOrder(add_order_model)
                    .enqueue(new Callback<SingleOrderDataModel>() {
                        @Override
                        public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                            dialog.dismiss();
                            if (response.isSuccessful()) {


                                if (response.body() != null && response.body().getData() != null) {
                                    add_order_model.setSale_id(response.body().getData().getId());
                                    Intent intent = getIntent();
                                    intent.putExtra("data", add_order_model);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                    // navigateToOrderDetialsActivity(response.body());

                                }

                            } else {
                                dialog.dismiss();
                                try {
                                    Log.e("error_code", response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (response.code() == 500) {
                                    // Toast.makeText(CheckoutActivity.this, "Server Error", Toast.LENGTH_SHORT).show();


                                } else {
                                    //Toast.makeText(CheckoutActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();

                                    try {

                                        Log.e("error", response.code() + "_" + response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<SingleOrderDataModel> call, Throwable t) {
                            try {
                                dialog.dismiss();
                                if (t.getMessage() != "") {
                                    Log.e("error", t.getMessage());
                                    if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                        //      Toast.makeText(CheckoutActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                    } else {
                                        //    Toast.makeText(CheckoutActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } catch (Exception e) {
                            }
                        }
                    });
        } catch (Exception e) {

        }
    }


}