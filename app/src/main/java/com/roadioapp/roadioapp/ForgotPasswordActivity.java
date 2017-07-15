package com.roadioapp.roadioapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.roadioapp.roadioapp.mObjects.ButtonEffects;
import com.roadioapp.roadioapp.mObjects.SaveLocalMemory;
import com.roadioapp.roadioapp.mObjects.ProgressBarObject;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Activity activity;

    private ButtonEffects buttonEffects;
    private ProgressBarObject progressBarObj;
    private String DOMAIN;
    private SaveLocalMemory saveLocalMemory;

    private LinearLayout subBtn;
    private EditText mobNumber;

    private String mob_no_str;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        activity = this;

        DOMAIN = getString(R.string.app_api_domain);
        progressBarObj = new ProgressBarObject(activity);
        buttonEffects = new ButtonEffects(activity);
        saveLocalMemory = new SaveLocalMemory(activity).selectPref("forgotPass");

        mobNumber = (EditText) findViewById(R.id.mobNumber);

        subBtn = (LinearLayout) findViewById(R.id.subBtn);
        buttonEffects.btnEventEffRounded(subBtn);
        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mob_no_str = mobNumber.getText().toString().trim();
                String err = "";
                if(mob_no_str.isEmpty()){
                    err = "Mobile Number is required!";
                }else if(mob_no_str.length() != 12){
                    err = "Mobile Number is invalid!";
                }

                if(!err.isEmpty()){
                    Toast.makeText(activity, err, Toast.LENGTH_SHORT).show();
                }else{
                    forgotPassReqSend();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!saveLocalMemory.getVal("mob_no").isEmpty()){
            switchAct(ForgotPassConfirmActivity.class);
        }
    }

    private void forgotPassReqSend(){
        progressBarObj.showProgressDialog();
        JSONObject params = new JSONObject();
        try{
            params.put("mob_no", mob_no_str);
            params.put("type", "driver");
        } catch (JSONException e){
            e.printStackTrace();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        String reqUrl = DOMAIN + "/forgot_password";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, reqUrl, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.getString("status").equals("ok")){
                        progressBarObj.hideProgressDialog();
                        saveLocalMemory.editPref().putVal("token", response.getString("token")).putVal("mob_no", mob_no_str).commitPref();
                        switchAct(ForgotPassConfirmActivity.class);
                    } else {
                        progressBarObj.hideProgressDialog();
                        Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e){
                    progressBarObj.hideProgressDialog();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBarObj.hideProgressDialog();
                Log.e("ErrorResponse", error.toString());
                Toast.makeText(activity, "Bad Request", Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(300000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }

    private void switchAct(Class classname){
        startActivity(new Intent(activity, classname));
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switchAct(LoginActivity.class);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}