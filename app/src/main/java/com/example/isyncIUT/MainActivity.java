package com.example.isyncIUT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import net.fortuna.ical4j.data.ParserException;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity implements code.CodeExecutionListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleSignInManager.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInManager.handleSignInResult(task);
            GoogleSignInAccount account = task.getResult();
            if (account != null) {
                // L'utilisateur est connecté avec succès
                // Vous pouvez utiliser account.getIdToken() pour récupérer le jeton d'identification Google
            } else {
                // La connexion a échoué
            }
        }
    }

    // Vérifie si l'application a l'autorisation d'accéder au stockage externe
    public static boolean checkStoragePermission(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    // Cette méthode est appelée après que l'utilisateur a répondu à la demande d'autorisation
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // L'autorisation a été accordée, vous pouvez accéder au stockage externe
        // L'autorisation a été refusée
    }

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

            // Vérifie si l'application a la permission d'accéder au stockage externe
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Demande la permission d'accéder au stockage externe
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }

            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }  // Vérifier les autorisations d'accès pour le fichier téléchargé ici


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