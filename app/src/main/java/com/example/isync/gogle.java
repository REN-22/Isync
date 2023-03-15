package com.example.isync;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
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
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class gogle {

    private static final String APPLICATION_NAME = "iSync";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Supprime tous les événements d'un fichier ICS de Google Calendar.
     *
     * @param num le numéro du fichier ICS à synchroniser
     */
    public static void deleteAllEvents(int num) throws IOException, ParserException, GeneralSecurityException {
        // Créer un objet File pour le fichier ICS
        File file = new File("PASTOUCHE/g" + num + "O.ics");
        if (!file.exists()) {
            System.err.println("Le fichier " + file.getAbsolutePath() + " n'existe pas.");
            return;
        }

        try {
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar calendar = builder.build(new FileInputStream(file));
            List<VEvent> events = calendar.getComponents().stream()
                    .filter(component -> component.getName().equals("VEVENT"))
                    .map(component -> (VEvent) component)
                    .collect(Collectors.toList());

            // Supprimer les évènements de Google Calendar
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:/Users/renzo/Documents/ISYNC/certifica.json"))
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
            Calendar service = new Calendar.Builder(new NetHttpTransport(), JSON_FACTORY, (HttpRequestInitializer) credentials)
                    .setApplicationName("Isync")
                    .build();
            String calendarId = "primary";
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
        File file = new File("PASTOUCHE/g" + num + "N.ics");
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = builder.build(new FileInputStream(file));
        List<VEvent> events = calendar.getComponents().stream()
                .filter(component -> component.getName().equals("VEVENT"))
                .map(component -> (VEvent) component)
                .collect(Collectors.toList());

        // Ajouter les évènements à Google Calendar
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:/Users/renzo/Documents/ISYNC/certifica.json"))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
        Calendar service = new Calendar.Builder(new NetHttpTransport(), JSON_FACTORY, (HttpRequestInitializer) credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
        String calendarId = "primary";
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