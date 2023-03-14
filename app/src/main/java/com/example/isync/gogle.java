package com.example.isync;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleCalendarUtils {

    private static final String APPLICATION_NAME = "iSync";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Supprime tous les événements d'un fichier ICS de Google Calendar.
     *
     * @param num le numéro du fichier ICS à synchroniser
     * @throws IOException
     * @throws ParserException
     * @throws GeneralSecurityException
     */
    public static void deleteAllEvents(int num) throws IOException, ParserException, GeneralSecurityException {
        // Créer un objet File pour le fichier ICS
        File file = new File("PASTOUCHE/g" + num + "N.ics");
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = builder.build(new FileInputStream(file));
        List<VEvent> events = calendar.getComponents().stream()
                .filter(component -> component instanceof VEvent)
                .map(component -> (VEvent) component)
                .collect(Collectors.toList());

        // Supprimer les évènements de Google Calendar
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:/Users/renzo/Documents/ISYNC/certifica.json"))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
        Calendar service = new Calendar.Builder(new NetHttpTransport(), JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
        String calendarId = "primary";
        for (VEvent event : events) {
            String eventId = event.getUid().getValue();
            service.events().delete(calendarId, eventId).execute();
        }
    }
}
