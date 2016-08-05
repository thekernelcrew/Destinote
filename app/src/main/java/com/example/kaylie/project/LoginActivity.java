package com.example.kaylie.project;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kaylie.project.Geofence.GeofenceTransitionsReceiver;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends Activity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private boolean forLogout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build());
        setContentView(R.layout.activity_main);


        //Find TextViews
        TextView tvWelcome = (TextView)findViewById(R.id.tvDescription);
        TextView tvTagline = (TextView)findViewById(R.id.tvTagline);

        //Create custom Typefaces
        Typeface customLato = Typeface.createFromAsset(getAssets(),  "fonts/SourceSansPro-Light.otf");
        Typeface customSourceSans = Typeface.createFromAsset(getAssets(),  "fonts/SourceSansPro-Light.otf");

        //Set the Type Face to the Text View
        tvWelcome.setTypeface(customLato);
        tvTagline.setTypeface(customSourceSans);

        forLogout = getIntent().getBooleanExtra("from_logout", false);
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        if (Profile.getCurrentProfile() != null && forLogout == false) {
            Log.d("LOGIN_USER", Profile.getCurrentProfile().toString());
            Toast.makeText(this, "Logged in!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), DisplayHomeActivity.class);
            startActivity(i);
        } else {
            Log.d("LOGIN_USER", "No user logged in");


        }

        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d("LOGIN", "Success");
                    forLogout = false;
                    Intent i = new Intent(getApplicationContext(), DisplayHomeActivity.class);
                    startActivity(i);
                }

                @Override
                public void onCancel() {
                    Log.d("LOGIN", "Canceled");
            }


                @Override
                public void onError(FacebookException exception) {
                    Log.d("LOGIN", "Error");
                }
        });

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onSkipLogin(View view) {
        Intent i = new Intent(getApplicationContext(), DisplayHomeActivity.class);
        startActivity(i);
    }

    @Override
    public void onResume(){
        super.onResume();
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, GeofenceTransitionsReceiver.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        am.cancel(pi);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()
                        + 30*1000,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
    }

}


