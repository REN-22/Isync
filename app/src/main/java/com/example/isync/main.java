package com.example.isync;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.IBinder;

import net.fortuna.ical4j.data.ParserException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class main {

    public main() {
    }

    public static class MonService extends Service {

        public int onStartCommand(Intent intent, int flags, int startId, int number) {
            // Création de l'objet pour planifier l'exécution
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            // Récupération de l'heure actuelle
            Calendar now = Calendar.getInstance();

            // Calcul de l'heure de la première exécution (demain à 3h00)
            Calendar nextExecutionTime = Calendar.getInstance();
            nextExecutionTime.set(Calendar.HOUR_OF_DAY, 3);
            nextExecutionTime.set(Calendar.MINUTE, 0);
            nextExecutionTime.set(Calendar.SECOND, 0);
            if (nextExecutionTime.before(now)) {
                nextExecutionTime.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Calcul de la durée entre les exécutions (24 heures)
            long period = 24 * 60 * 60 * 1000;

            // Obtenez une instance de SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("isync", Context.MODE_PRIVATE);

            // Récupérez le numéro et l'adresse e-mail des préférences
            int num = sharedPreferences.getInt("num", 0); // 0 est la valeur par défaut si "num" n'existe pas
            String email = sharedPreferences.getString("email", ""); // "" est la valeur par défaut si "email" n'existe pas

            // Planification de l'exécution de la méthode main()
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    // Appeler la méthode code() ici
                    code.exec(getApplicationContext(), num);
                    try {
                        gogle.deleteAllEvents(num);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ParserException e) {
                        throw new RuntimeException(e);
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                    code.modif(num);
                    try {
                        gogle.addAllEvents(num);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ParserException e) {
                        throw new RuntimeException(e);
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, nextExecutionTime.getTimeInMillis() - now.getTimeInMillis(), period, TimeUnit.MILLISECONDS);


            // N'oubliez pas d'arrêter le service en appelant la méthode stopSelf() lorsque vous avez terminé
            stopSelf();
            return START_NOT_STICKY;
        }


        @Override
        public IBinder onBind(Intent intent) {
            // Cette méthode est requise mais vous n'avez pas besoin de la mettre en œuvre
            return null;
        }
    }

}
