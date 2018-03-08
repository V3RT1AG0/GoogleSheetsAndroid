package com.example.v3rt1ag0.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Splash extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try
        {
            getSupportActionBar().setTitle("IYF, Whiltefield");
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        SharedPreferences sharedPref = this.getSharedPreferences(
                "com.userDetails", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        if(sharedPref.getBoolean("alreadyEntered",false))
        {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
        else
        {
            final EditText name = findViewById(R.id.name);
            Button button = findViewById(R.id.Submit);

            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    editor.putBoolean("alreadyEntered", true);
                    editor.putString("MyName",name.getText().toString());
                    editor.commit();
                    startActivity(new Intent(view.getContext(), MainActivity.class));
                }
            });
        }

    }

}
