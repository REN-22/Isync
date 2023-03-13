package com.example.isync;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.oauth2.GoogleCredentials;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class gogle {
    public static void supprr(int num) throws IOException, ParserException {
        class CredentialHelper {
            public static GoogleCredentials getCredentials() throws IOException {
                FileInputStream credentialsStream = new FileInputStream("C:/Users/renzo/Documents/ISYNC/certifica.json");
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
                return credentials;
            }
        }
        // Créer un objet File pour le fichier ICS
        File file = new File("PASTOUCHE/g" + num + "N.ics");
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(new FileInputStream(file));
        List<VEvent> events = ((Calendar) calendar).getComponents().stream()
                .filter(component -> component instanceof VEvent)
                .map(component -> (VEvent) component)
                .collect(Collectors.toList());

        // Supprimer les évènements de Google Calendar
        GoogleCredentials credentials = CredentialHelper.getCredentials();
        NetHttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        Calendar service = new Calendar.Builder(httpTransport, jsonFactory, credentials)
                .setApplicationName("iSync")
                .build();
        String calendarId = "primary";
        for (VEvent event : events) {
            String eventId = event.getUid().getValue();
            service.events().delete(calendarId, eventId).execute();
        }
    }
}
