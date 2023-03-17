package com.example.isyncIUT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import net.fortuna.ical4j.data.ParserException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity implements code.CodeExecutionListener {

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText inputNum = findViewById(R.id.gXXXXinput);
        Button downloadBtn = findViewById(R.id.downloadBtn);
        downloadBtn.setOnClickListener(v -> {
            String num = inputNum.getText().toString().trim();
            // Récupérer les préférences partagées
            SharedPreferences sharedPreferences = getSharedPreferences("isync", MODE_PRIVATE);

            // Éditer les préférences partagées
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Ajouter les données à sauvegarder
            editor.putString("numero", num);

            // Enregistrer les modifications
            editor.apply();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    // Vérifier les autorisations d'accès pour le fichier téléchargé ici
                }
            } else {
                // Les autorisations d'accès sont accordées automatiquement en dessous d'Android 6.0
            }

            if (num.isEmpty()) {
                Toast.makeText(MainActivity.this, "Veuillez entrer un numéro valide", Toast.LENGTH_SHORT).show();
            } else {
                int numInt = Integer.parseInt(num);
                new AsyncTask<Integer, Void, Void>() {
                    @Override
                    protected Void doInBackground(Integer... params) {
                        code.exec((Context) MainActivity.this, numInt, (code.CodeExecutionListener) MainActivity.this);
                        return null;
                    }
                }.execute(numInt);
            }
        });
    }

    @Override
    public void onExecuted(int numInt) {
        try {
            gogle.deleteAllEvents(numInt);
        } catch (IOException | ParserException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        code.modif(numInt);
        try {
            gogle.addAllEvents(numInt);
        } catch (IOException | ParserException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
