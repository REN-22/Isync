package com.example.isync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.isyncIUT.R;

public class MainActivity extends AppCompatActivity {

    public static int number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ajouter le code ici
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
                editor.putString("email", "example@mail.com");

                // Enregistrer les modifications
                editor.apply();

                if (num.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez entrer un numéro valide", Toast.LENGTH_SHORT).show();
                } else {
                    int numInt = Integer.parseInt(num);
                    code.exec(MainActivity.this, numInt);
                    code.modif(numInt);
                    Toast.makeText(MainActivity.this, "Le fichier a été téléchargé avec succès", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
