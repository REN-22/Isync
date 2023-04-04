package com.example.isyncIUT;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import androidx.appcompat.app.AppCompatActivity;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Summary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class code extends AppCompatActivity {
    private Context mContext;

    public code(Context context) {
        mContext = context;
    }


    public interface CodeExecutionListener {
        void onExecuted(int numInt);
    }

    /**
     * telecharge le fhichier ICS
     *
     * @param num le numéro du fichier ICS
     */
    public static void exec(Context context, int num, CodeExecutionListener listener) {
        String fileName = "g" + num + "N.ics";
        File directory = new File(context.getFilesDir(), "PASTOUCHE");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);

        String url = "https://edt.iut-tlse3.fr/planning/info/g" + num + ".ics";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
            }
            if (file.exists()) {
                System.out.println("Le fichier " + file.getAbsolutePath() + " a été téléchargé avec succès et est présent dans le répertoire.");
            } else {
                System.out.println("Le téléchargement du fichier a échoué ou le fichier est introuvable.");
            }
            listener.onExecuted(num);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * modifie le fichier ICS pour que le résultat soi plus lisible
     *
     * @param num le numéro du fichier ICS
     */
    public static void modif(int num) {

        try {
            @SuppressLint("SdCardPath") File file = new File("/data/user/0/com.example.isyncIUT/files/PASTOUCHE/g" + num + "N.ics");
            if (!file.exists()) {
                System.err.println("Le fichier " + file.getAbsolutePath() + " n'existe pas. modif");
                return;
            }
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(Files.newInputStream(file.toPath()));

            String[] INR = {"JAVA", "IHM", "QUALITER DEV", "C", "RÉSEAUX", "BASE DE DONNÉE", "GRAPHE", "STAT", "MÉTODE NUMÉRIQUE", "GESTION DE PROJET", "DROIT", "ANGLAIS", "COMMUNICATION"};
            String[] INS = {"SAÉ DÉVELOPEMENT D'UNE APP", "SAÉ ALGORITHME", "SAÉ RÉSEAU", "SAÉ BASE DE DONNÉE", "SAÉ GESTION DE PROJET", "SAÉ TRAVAIL D'ÉQUIPE"};

            for (Object obj : calendar.getComponents(Component.VEVENT)) {
                VEvent event = (VEvent) obj;
                Summary summary = event.getSummary();
                if (summary != null) {
                    String summaryValue = summary.getValue();
                    if (summaryValue.startsWith("Indisponibilité - IN")) {
                        summaryValue = summaryValue.replace("Indisponibilité - ", "");
                        summary.setValue(summaryValue);
                    } else if (summaryValue.startsWith("Indisponibilité - ")) {
                        calendar.getComponents().remove(event);
                    }
                    summaryValue = summary.getValue();
                    for (int i = 201; i <= 213; i++) {
                        String pattern = "INR" + i;
                        if (summaryValue.contains(pattern)) {
                            String replacement = INR[i - 201];
                            summaryValue = summaryValue.replace(pattern, replacement);
                            summary.setValue(summaryValue);
                        }
                    }
                    summaryValue = summary.getValue();
                    for (int i = 201; i <= 206; i++) {
                        String pattern = "InS" + i;
                        if (summaryValue.contains(pattern)) {
                            String replacement = INS[i - 201];
                            summaryValue = summaryValue.replace(pattern, replacement);
                            summary.setValue(summaryValue);
                        }
                    }
                }
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public void insert(int num) {
        // Récupérer le chemin absolu du fichier ICS
        String filePath = "/data/user/0/com.example.isyncIUT/files/PASTOUCHE/g" + num + "N.ics";

        // Créer un nouvel analyseur de calendrier iCal4j
        CalendarBuilder builder = new CalendarBuilder();

        try {
            // Parser le fichier ICS
            FileInputStream inputStream = new FileInputStream(filePath);
            Calendar calendar = builder.build(inputStream);

            // Récupérer les événements du calendrier
            List<VEvent> events = calendar.getComponents(Component.VEVENT);

            // Pour chaque événement, créer un nouvel événement dans le calendrier du téléphone
            for (VEvent event : events) {
                // Vérifier s'il y a déjà un événement sur le créneau
                String[] projection = new String[] { CalendarContract.Events._ID };
                String selection = CalendarContract.Events.CALENDAR_ID + " = ? AND " +
                        CalendarContract.Events.DTSTART + " = ? AND " +
                        CalendarContract.Events.DTEND + " = ?";
                String[] selectionArgs = new String[] { "1", Long.toString(event.getStartDate().getDate().getTime()), Long.toString(event.getEndDate().getDate().getTime()) };
                Cursor cursor = mContext.getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null);
                long eventId;
                if (cursor.moveToFirst()) {
                    // L'événement existe déjà, le remplacer
                    eventId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID));
                    Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                    getContentResolver().update(uri, getEventContentValues(event), null, null);
                } else {
                    // L'événement n'existe pas, l'ajouter
                    getContentResolver().insert(CalendarContract.Events.CONTENT_URI, getEventContentValues(event));
                }
                cursor.close();
            }
        } catch (IOException | ParserException e) {
            e.printStackTrace();
        }
    }

    private ContentValues getEventContentValues(VEvent event) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.TITLE, event.getSummary().getValue());
        values.put(CalendarContract.Events.DESCRIPTION, event.getDescription().getValue());
        values.put(CalendarContract.Events.EVENT_LOCATION, event.getLocation().getValue());
        values.put(CalendarContract.Events.DTSTART, event.getStartDate().getDate().getTime());
        values.put(CalendarContract.Events.DTEND, event.getEndDate().getDate().getTime());
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // l'identifiant du calendrier par défaut du téléphone
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        return values;
    }

}