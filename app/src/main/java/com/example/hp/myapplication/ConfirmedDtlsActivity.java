package com.example.hp.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.hp.myapplication.Request.DtlsRequest;
import com.example.hp.myapplication.Request.Ip;
import com.sinch.android.rtc.calling.Call;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConfirmedDtlsActivity extends BaseActivity  {

    private SimpleDateFormat dateff = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
    TextView date_time, subj, name, dtls;
    Button callBtn, play;
    RequestQueue requestQueue;
    int success;
    Intent i;
    Ip ip;
    String meetings_id, customer_id, dateselected;

    Session session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmed_dtls);

        date_time =(TextView) findViewById(R.id.date_time);
        subj =(TextView) findViewById(R.id.subj);
        name =(TextView) findViewById(R.id.name);
        dtls =(TextView) findViewById(R.id.dtls);
        callBtn =(Button) findViewById(R.id.call_btn);
        play= (Button) findViewById(R.id.play_btn) ;
        ip = new Ip();


        session = new Session(this);
        callBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Call call = getSinchServiceInterface().callUserVideo(customer_id);
                String callId = call.getCallId();
                Log.i("++++++++++++++", "customer_id"+customer_id+" user_id"+session.getUserId());
                Intent callScreen = new Intent(ConfirmedDtlsActivity.this, CallScreenActivity.class);
                callScreen.putExtra("meetings_id",meetings_id);
                callScreen.putExtra(SinchService.CALL_ID, callId);
                startActivity(callScreen);
            }
        });

        requestQueue = Volley.newRequestQueue(ConfirmedDtlsActivity.this);

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

        DtlsRequest dtlsRequest = new DtlsRequest(meetings_id,  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Login Response", response);
                // Response from the server is in the form if a JSON, so we need a JSON Object
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    success = jsonObject.getInt("success");
                    if (success==1) {

                        name.setText("Customer: "+jsonObject.getString("firstName")+" "+jsonObject.getString("lastName"));

                        date_time.setText("Date: "+jsonObject.getString("date")+" "+jsonObject.getString("time"));
                        customer_id= jsonObject.getString("customer_id");
                        dateselected=jsonObject.getString("date");

                        if (jsonObject.getString("subject").equals("null")){
                            subj.setVisibility(View.GONE);
                            dtls.setVisibility(View.GONE);
                        }else{
                            subj.setText("Subject: "+jsonObject.getString("subject"));
                            dtls.setText("Details: "+jsonObject.getString("details"));
                        }
                        try {
                            Date date = dateff.parse(dateselected);
                            Log.d("****************+++", ""+dateff.format(date));
                            String selectedDateRdv = dateff.format(date);
                            Date nn = new Date();
                            Date dateNow = formatter.parse(nn.toString());
                            Log.d("****************now", ""+dateff.format(dateNow));
                            String selectedDateNow = dateff.format(dateNow);
                            if(selectedDateNow.equals(selectedDateRdv)){

                            }else {

                                callBtn.setVisibility(View.GONE);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    } else {
                        if(jsonObject.getString("message").equals("No user found"))
                            Toast.makeText(ConfirmedDtlsActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ConfirmedDtlsActivity.this, "Bad Response From Server", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof ServerError)
                    Toast.makeText(ConfirmedDtlsActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                else if (error instanceof TimeoutError)
                    Toast.makeText(ConfirmedDtlsActivity.this, "Connection Timed Out", Toast.LENGTH_SHORT).show();
                else if (error instanceof NetworkError)
                    Toast.makeText(ConfirmedDtlsActivity.this, "Bad Network Connection", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(dtlsRequest);


    }

    @Override
    protected void onServiceConnected() {
    }

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }

    //to kill the current session of SinchService
    private void stopButtonClicked() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        finish();
    }

}
