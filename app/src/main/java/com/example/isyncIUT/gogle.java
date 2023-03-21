package com.example.isyncIUT;

import android.annotation.SuppressLint;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.oauth2.GoogleCredentials;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class gogle {

    private static final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    /**
     * Supprime tous les événements d'un fichier ICS de Google Calendar.
     *
     * @param num le numéro du fichier ICS à synchroniser
     */
    public static void deleteAllEvents(int num) throws IOException, ParserException, GeneralSecurityException {
        // Créer un objet File pour le fichier ICS
        @SuppressLint("SdCardPath") File file = new File("/data/user/0/com.example.isyncIUT/files/PASTOUCHE/g" + num + "O.ics");
        if (!file.exists()) {
            System.err.println("Le fichier " + file.getAbsolutePath() + " n'existe pas. deleteAllEvents");
            return;
        }

        // Charger les identifiants d'authentification depuis le fichier JSON
        GoogleCredentials credentials = GoogleCredentials.fromStream(Objects.requireNonNull(gogle.class.getClassLoader()).getResourceAsStream("app/src/main/java/com/example/isyncIUT/certifica.json"))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        // Créer l'objet service pour l'API Google Calendar
        Calendar service = new Calendar.Builder(new NetHttpTransport(), jsonFactory, (HttpRequestInitializer) credentials)
                .setApplicationName("Isync")
                .build();

        // Obtenir l'identifiant du calendrier "primary"
        String calendarId = "primary";

        try {
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar calendar = builder.build(new FileInputStream(file));
            List<VEvent> events = calendar.getComponents().stream()
                    .filter(component -> component.getName().equals("VEVENT"))
                    .map(component -> (VEvent) component)
                    .collect(Collectors.toList());

            // Supprimer les évènements de Google Calendar
            for (VEvent event : events) {
                String eventId = event.getUid().getValue();
                service.events().delete(calendarId, eventId).execute();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * ajoute les évènement du fichier ICS a google calendar
     *
     * @param num le numéro du fichier ICS à synchroniser
     */
    public static void addAllEvents(int num) throws IOException, ParserException, GeneralSecurityException {
        // Créer un objet File pour le fichier ICS
        @SuppressLint("SdCardPath") File file = new File("/data/user/0/com.example.isyncIUT/files/PASTOUCHE/g" + num + "N.ics");
        if (!file.exists()) {
            System.err.println("Le fichier " + file.getAbsolutePath() + " n'existe pas. addAllEvents");
            return;
        }

        // Charger les identifiants d'authentification depuis le fichier JSON
        GoogleCredentials credentials = GoogleCredentials.fromStream(Objects.requireNonNull(gogle.class.getClassLoader()).getResourceAsStream("app/src/main/java/com/example/isyncIUT/certifica.json"))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        // Créer l'objet service pour l'API Google Calendar
        Calendar service = new Calendar.Builder(new NetHttpTransport(), jsonFactory, (HttpRequestInitializer) credentials)
                .setApplicationName("Isync")
                .build();

        // Obtenir l'identifiant du calendrier "primary"

        String calendarId = "primary";
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = builder.build(Files.newInputStream(file.toPath()));
        List<VEvent> events = calendar.getComponents().stream()
                .filter(component -> component.getName().equals("VEVENT"))
                .map(component -> (VEvent) component)
                .collect(Collectors.toList());

        // Ajouter les évènements à Google Calendar
        for (VEvent event : events) {
            Event googleEvent = new Event();
            googleEvent.setSummary(event.getSummary().getValue());
            googleEvent.setLocation(event.getLocation().getValue());
            googleEvent.setDescription(event.getDescription().getValue());
            googleEvent.setStart(new EventDateTime().setDateTime(new DateTime(event.getStartDate().getDate())));
            googleEvent.setEnd(new EventDateTime().setDateTime(new DateTime(event.getEndDate().getDate())));
            service.events().insert(calendarId, googleEvent).execute();
        }
    }
}