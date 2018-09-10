package com.example.hp.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.hp.myapplication.Request.DenyRequest;
import com.example.hp.myapplication.Request.DtlsRequest;
import com.example.hp.myapplication.Request.Ip;
import com.example.hp.myapplication.Request.LoginRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class DemandDtlsActivity extends AppCompatActivity {

    TextView np, subj, dtls;
    RequestQueue requestQueue;
    int success;
    Intent i;
    String meetings_id;
    Button confirm, deny, play;
    String email;
    Ip ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demand_dtls);

        ip = new Ip();
        confirm= (Button) findViewById(R.id.cnf_btn) ;
        deny= (Button) findViewById(R.id.deny_btn) ;
        play= (Button) findViewById(R.id.playBtn) ;
        i= getIntent();
        meetings_id = i.getStringExtra("meetings_id");



        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setDataSource(ip.getIp()+"uploads/"+meetings_id+".3gp");
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    // make something
                }

            }
        });
        deny.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {

                DenyRequest denyRequest = new DenyRequest(meetings_id,  new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Deny Response", response);
                        // Response from the server is in the form if a JSON, so we need a JSON Object
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            success = jsonObject.getInt("success");
                            if (success==1) {
                                new SimpleMail().sendEmail(email, "Confirmation RDV", "Your RDV request was refused by the agent!");

                                Intent mainIntent = new Intent(DemandDtlsActivity.this,DemandsActivity.class);
                                startActivity(mainIntent);
                                finish();

                            } else {
                                if(jsonObject.getString("message").equals("No request found"))
                                    Toast.makeText(DemandDtlsActivity.this, "Request Not Found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(DemandDtlsActivity.this, "Bad Response From Server", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof ServerError)
                            Toast.makeText(DemandDtlsActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                        else if (error instanceof TimeoutError)
                            Toast.makeText(DemandDtlsActivity.this, "Connection Timed Out", Toast.LENGTH_SHORT).show();
                        else if (error instanceof NetworkError)
                            Toast.makeText(DemandDtlsActivity.this, "Bad Network Connection", Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(denyRequest);




            }
        });


        requestQueue = Volley.newRequestQueue(DemandDtlsActivity.this);







        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent in = new Intent(getApplicationContext(), confActivity.class);
                in.putExtra("meetings_id", meetings_id);
                in.putExtra("email", email);
                startActivity(in);
            }
        });


        np = (TextView) findViewById(R.id.np);
        subj = (TextView) findViewById(R.id.sub);
        dtls = (TextView) findViewById(R.id.dtl);

        requestQueue = Volley.newRequestQueue(DemandDtlsActivity.this);

        DtlsRequest dtlsRequest = new DtlsRequest(meetings_id,  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Login Response", response);
                // Response from the server is in the form if a JSON, so we need a JSON Object
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    success = jsonObject.getInt("success");
                    if (success==1) {

                        np.setText("Customer: "+jsonObject.getString("firstName")+" "+jsonObject.getString("lastName"));

                        if (jsonObject.getString("subject").equals("null")){
                            subj.setVisibility(View.GONE);
                            dtls.setVisibility(View.GONE);
                        }else{
                            subj.setText("Subject: "+jsonObject.getString("subject"));
                            dtls.setText("Details: "+jsonObject.getString("details"));
                            play.setVisibility(View.GONE);
                        }
                        email= jsonObject.getString("userName");


                    } else {
                        if(jsonObject.getString("message").equals("No user found"))
                            Toast.makeText(DemandDtlsActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DemandDtlsActivity.this, "Bad Response From Server", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof ServerError)
                    Toast.makeText(DemandDtlsActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                else if (error instanceof TimeoutError)
                    Toast.makeText(DemandDtlsActivity.this, "Connection Timed Out", Toast.LENGTH_SHORT).show();
                else if (error instanceof NetworkError)
                    Toast.makeText(DemandDtlsActivity.this, "Bad Network Connection", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(dtlsRequest);


    }
}



