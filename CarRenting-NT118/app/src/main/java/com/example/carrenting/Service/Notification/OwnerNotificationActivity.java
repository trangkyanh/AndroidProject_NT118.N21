package com.example.carrenting.Service.Notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carrenting.Model.Notification;
import com.example.carrenting.Model.User;
import com.example.carrenting.Model.Vehicle;
import com.example.carrenting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class OwnerNotificationActivity extends AppCompatActivity {

    FirebaseFirestore dtb;
    Intent intent;
    String ProvideID, vehicle_id;
    String NotiID,noti_status;


    private ArrayList<Vehicle> ls = new ArrayList<Vehicle>();
    private TextView tv_id,name,email,phoneNumber, tv_status;// Thông tin nhà cung cấp
    private TextView tv_BrandCar,tv_Gia,tv_DiaDiem,pickup,dropoff,totalCost;// Thông tin xe
    private Button btn_xacnhan,btn_huy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail_provide);
        intent = getIntent();

        String OrderID = intent.getStringExtra("NotiID");
        NotiID = OrderID;
        
        init();

        dtb = FirebaseFirestore.getInstance();
        dtb.collection("Notification")
                .whereEqualTo("NotiID", NotiID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Notification temp = new Notification();
                                temp.setNotiID(document.getId());
                                temp.setProvideID(document.get("ProviderID").toString());
                                temp.setVehicle_id(document.get("vehicle_id").toString());
                                temp.setStatus(document.get("Status").toString());
                                ProvideID = temp.getProvideID();
                                vehicle_id = temp.getVehicle_id();

                                tv_id.setText(NotiID);
                                if (temp.getStatus() == "Đang chờ"){
                                     tv_status.setText("Đang chờ");
                                } else {
                                    if (temp.getStatus() == "Đã xác nhận"){
                                        tv_status.setText("Đã xác nhận");
                                    }
                                    else tv_status.setText("Không được xác nhận");
                                }

                                getuser(ProvideID);
                                getvehicle(vehicle_id);
                            }
                        } else {
                            Toast.makeText(OwnerNotificationActivity.this, "Không thể lấy thông báo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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
                                user.setUser_id(document.get("user_id").toString());
                                user.setUsername(document.get("user_fullname").toString());
                                user.setEmail(document.get("email").toString());
                                user.setPhoneNumber(document.get("phonenumber").toString());
                                name.setText(user.getUsername());
                                email.setText(user.getEmail());
                                phoneNumber.setText(user.getPhoneNumber());
                            }
                        } else {
                            Toast.makeText(OwnerNotificationActivity.this, "Không thể lấy thông tin nhà cung cấp", Toast.LENGTH_SHORT).show();
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
                                temp.setVehicle_name(document.get("vehicle_name").toString());
                                temp.setVehicle_availability(document.get("availability").toString());
                                temp.setVehicle_price(document.get("vehicle_price").toString());
                                temp.setOwner_address(document.get("vehicle_address").toString());
                                tv_BrandCar.setText(temp.getVehicle_name());
                                tv_Gia.setText(temp.getVehicle_price() + " Đ /ngày");
                                tv_DiaDiem.setText(temp.getOwner_address());
                            }
                        } else {
                            Toast.makeText(OwnerNotificationActivity.this, "Không thể lấy thông tin xe", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void OwnerConfirm(String status)
    {
        btn_xacnhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        btn_huy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }


    public void init(){
        tv_id=findViewById(R.id.txtview_noti_id);
        email=findViewById(R.id.txtview_noti_email);
        name=findViewById(R.id.txtview_noti_name);
        phoneNumber=findViewById(R.id.txtview_noti_phoneNumber);
        tv_BrandCar=findViewById(R.id.txtview_noti_BrandCar);
        tv_DiaDiem=findViewById(R.id.noti_DiaDiem);


        tv_Gia=findViewById(R.id.txtview_noti_price);
        pickup=findViewById(R.id.noti_pickup);
        dropoff=findViewById(R.id.noti_dropoff);
        totalCost=findViewById(R.id.txtview_noti_totalCost);
        tv_status=findViewById(R.id.txtview_noti_status);

        btn_xacnhan=findViewById(R.id.btn_noti_XacNhan);
        btn_huy=findViewById(R.id.btn_noti_huy);
    }
}
