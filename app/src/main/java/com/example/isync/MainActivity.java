package com.example.isync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.isyncIUT.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;

import net.fortuna.ical4j.data.ParserException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    public static int number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText inputNum = findViewById(R.id.gXXXXinput);
        Button downloadBtn = findViewById(R.id.downloadBtn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = inputNum.getText().toString().trim();
                // Récupérer les préférences partagées
                SharedPreferences sharedPreferences = getSharedPreferences("isync", MODE_PRIVATE);

                // Éditer les préférences partagées
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Ajouter les données à sauvegarder
                editor.putString("numero", num);

                // Enregistrer les modifications
                editor.apply();

                if (num.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez entrer un numéro valide", Toast.LENGTH_SHORT).show();
                } else {
                    int numInt = Integer.parseInt(num);
                    code.exec(MainActivity.this, numInt);
                    try {
                        gogle.deleteAllEvents(numInt);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ParserException e) {
                        throw new RuntimeException(e);
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                    code.modif(numInt);
                    try {
                        gogle.addAllEvents(numInt);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ParserException e) {
                        throw new RuntimeException(e);
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(MainActivity.this, "Le fichier a été téléchargé avec succès", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
