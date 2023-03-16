package com.example.isyncIUT;

import android.content.Context;
import android.os.AsyncTask;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Summary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class code {

    /**
     * telecharge le fhichier ICS
     *
     * @param num le numéro du fichier ICS
     */
    public static void exec(Context context, int num) {
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(num);
    }

    /**
     * modifie le fichier ICS pour que le résultat soi plus lisible
     *
     * @param num le numéro du fichier ICS
     */
    public static void modif(int num) {
        try {
            File file = new File("PASTOUCHE/g" + num + "N.ics");
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
                            String replacement = INR[i-201];
                            summaryValue = summaryValue.replace(pattern, replacement);
                            summary.setValue(summaryValue);
                        }
                    }
                    summaryValue = summary.getValue();
                    for (int i = 201; i <= 206; i++) {
                        String pattern = "InS" + i;
                        if (summaryValue.contains(pattern)) {
                            String replacement = INS[i-201];
                            summaryValue = summaryValue.replace(pattern, replacement);
                            summary.setValue(summaryValue);
                        }
                    }
                }
            }

            if (file.delete()) {
                System.out.println("Le fichier a été supprimé avec succès.");
            } else {
                System.out.println("Impossible de supprimer le fichier.");
            }

            File oldFile = new File("PASTOUCHE/g" + num + "N.ics");
            File newFile = new File("PASTOUCHE/g" + num + "O.ics");
            if (oldFile.exists()) {
                boolean renamed = oldFile.renameTo(newFile);
                if (renamed) {
                    System.out.println("File renamed successfully.");
                } else {
                    System.out.println("File renaming failed.");
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
}