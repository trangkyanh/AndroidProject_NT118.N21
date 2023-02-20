package com.example.carrenting.Service.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carrenting.Model.CreateOrder;
import com.example.carrenting.Model.Notification;
import com.example.carrenting.Model.User;
import com.example.carrenting.Model.Vehicle;
import com.example.carrenting.R;
import com.example.carrenting.Service.ZaloPay.Constant.AppInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CustomerActivityDetail extends AppCompatActivity {

    FirebaseFirestore dtb;
    CreateOrder orderApi;
    Intent intent;
    String ProvideID, vehicle_id, ownername, owneremail, ownerphone, vehiclename, vehicleprice, vehicleaddress, vehiclepickup, vehicledrop, totalcost;
    String NotiID,noti_status;
    String amount = "1000";
    String token;
    ImageView vehicleImage;
    String vnp_url, vnp_tmnCode;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Vehicle> ls = new ArrayList<Vehicle>();
    private TextView tv_id,name,email,phoneNumber, tv_status;// Thông tin nhà cung cấp
    private TextView tv_BrandCar,tv_Gia,tv_DiaDiem,pickup,dropoff,totalCost;// Thông tin xe
    private Button btn_payment, btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail_custormer);
        intent = getIntent();

        String OrderID = intent.getStringExtra("NotiID");
        NotiID = OrderID;
        init();

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);

        dtb = FirebaseFirestore.getInstance();
        dtb.collection("Notification")
                .whereEqualTo("noti_id", NotiID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Notification temp = new Notification();
                                temp.setNoti_id(document.getId());
                                temp.setProvider_id(document.get("provider_id").toString());
                                temp.setVehicle_id(document.get("vehicle_id").toString());
                                temp.setStatus(document.get("status").toString());
                                ProvideID = temp.getProvider_id();
                                vehicle_id = temp.getVehicle_id();
                                noti_status=temp.getStatus();

                                tv_id.setText(NotiID);
                                if(noti_status.equals( "Dang cho"))
                                {
                                    tv_status.setText("Chưa được xác nhận");
                                }
                                else
                                {
                                    if(tv_status.equals( "Thanh toan"))
                                    {
                                        tv_status.setText("Đang chờ thanh toán");
                                        btn_payment.setVisibility(View.VISIBLE);
                                    }
                                    else
                                    if(noti_status.equals( "Xac nhan"))
                                    {
                                        tv_status.setText("Đã xác nhận");
                                    }
                                    else
                                    {
                                        tv_status.setText("Không được xác nhận");
                                    }

                                }
                                getuser(ProvideID);
                                getvehicle(vehicle_id);
                                setstatus();
                            }
                        } else {
                            Toast.makeText(CustomerActivityDetail.this, "Không thể lấy thông báo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        findViewById(R.id.btn_customer_pay).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                //asd
                orderApi = new CreateOrder();
                createorder();
                checkout(token);
                Log.d("CustomerActivityDetail", "Clicked");
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void createorder(){
        try {
            JSONObject data = orderApi.createOrder(amount);
            String code = data.getString("returncode");
            if (code.equals("1")) {
                token = data.getString("zptranstoken");
                //Log.e("Token", token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void checkout(String token){
        ZaloPaySDK.getInstance().payOrder(CustomerActivityDetail.this, token, "demozpdk://app", new PayOrderListener() {

            @Override
            public void onPaymentSucceeded(String s, String s1, String s2) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Transaction Token", s);
                        new AlertDialog.Builder(CustomerActivityDetail.this)
                                .setTitle("Payment Success")
                                .setMessage(String.format("TransactionId: %s - TransToken: %s", s, s1))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setNegativeButton("Cancel", null).show();
                    }
                });
            }

            @Override
            public void onPaymentCanceled(String s, String s1) {
                Log.e("Transaction Token", s);
                new AlertDialog.Builder(CustomerActivityDetail.this)
                        .setTitle("User Cancel Payment")
                        .setMessage(String.format("zpTransToken: %s \n", s))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }

            @Override
            public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                Log.e("Transaction Token", s);
                new AlertDialog.Builder(CustomerActivityDetail.this)
                        .setTitle("Payment Fail")
                        .setMessage(String.format("ZaloPayErrorCode: %s \nTransToken: %s", zaloPayError.toString(), s))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });
    }
    private void setstatus(){
        if(noti_status.equals( "Dang cho"))
        {
            tv_status.setText("Nhà cung cấp chưa xác nhận");
        }
        else
        {
            if(noti_status.equals("Thanh toan"))
            {
                tv_status.setText("Đang chờ thanh toán");
            }
            else
            if (noti_status.equals("Khong xac nhan")) {
                tv_status.setText("Nhà cung cấp không xác nhận");
            }
            else{
                tv_status.setText("Đã xác nhận thuê xe");
            }
        }
    }
    private void getuser(String ProvideID){
        dtb.collection("Users")
                .whereEqualTo("user_id", ProvideID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                User user = new User();
                                ownername = document.get("username").toString();
                                owneremail = document.get("email").toString();
                                ownerphone = document.get("phoneNumber").toString();
                                user.setUser_id(document.get("user_id").toString());
                                user.setUsername(ownername);
                                user.setEmail(owneremail);
                                user.setPhoneNumber(ownerphone);
                                name.setText(user.getUsername());
                                email.setText(user.getEmail());
                                phoneNumber.setText(user.getPhoneNumber());
                            }
                        } else {
                            Toast.makeText(CustomerActivityDetail.this, "Không thể lấy thông tin nhà cung cấp", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void getvehicle(String vehicle_id){
        dtb.collection("Vehicles")
                .whereEqualTo("vehicle_id", vehicle_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Vehicle temp = new Vehicle();
                                temp.setVehicle_id(document.getId());

                                vehiclename = document.get("vehicle_name").toString();
                                vehicleprice = document.get("vehicle_price").toString();
                                vehicleaddress = document.get("provider_address").toString();

                                temp.setVehicle_name(vehiclename);
                                temp.setVehicle_availability(document.get("vehicle_availability").toString());
                                temp.setVehicle_price(vehicleprice);
                                temp.setProvider_address(vehicleaddress);
                                tv_BrandCar.setText(temp.getVehicle_name());
                                tv_Gia.setText(temp.getVehicle_price() + " Đ /ngày");
                                tv_DiaDiem.setText(temp.getProvider_address());

                                temp.setVehicle_imageURL(document.get("vehicle_imageURL").toString());
                                if (!document.get("vehicle_imageURL").toString().isEmpty()) {
                                    Picasso.get().load(temp.getVehicle_imageURL()).into(vehicleImage);
                                }
                                else {
                                    temp.setVehicle_imageURL("");
                                }
                            }
                        } else {
                            Toast.makeText(CustomerActivityDetail.this, "Không thể lấy thông tin xe", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void init(){
        tv_id=findViewById(R.id.txtview_noti_id);
        tv_status=findViewById(R.id.txtview_noti_status);

        email=findViewById(R.id.txtview_noti_email);
        name=findViewById(R.id.txtview_noti_name);
        phoneNumber=findViewById(R.id.txtview_noti_phoneNumber);
        tv_BrandCar=findViewById(R.id.txtview_noti_BrandCar);
        tv_DiaDiem=findViewById(R.id.txt_checkout_address);

        btn_payment=findViewById(R.id.btn_customer_pay);
        btn_back=findViewById(R.id.btn_noti_back);

        tv_Gia=findViewById(R.id.txtview_noti_price);
        pickup=findViewById(R.id.tv_noti_pickup);
        dropoff=findViewById(R.id.tv_noti_dropoff);
        totalCost=findViewById(R.id.txtview_noti_totalCost);
        vehicleImage=findViewById(R.id.img_noti_car);

        //btn_payment.setVisibility(View.GONE);
        //btn_payment.setEnabled(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}
