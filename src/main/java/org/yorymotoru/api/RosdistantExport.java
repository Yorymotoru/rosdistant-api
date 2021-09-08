package org.yorymotoru.api;

import java.io.FileWriter;
import java.io.IOException;

public class RosdistantExport {
    public static void exportToICalendar(RosdistantConnection rosdistantConnection) {
        try(FileWriter writer = new FileWriter("schedule.ics", false))
        {
            for (Practice p : rosdistantConnection.getPractices()) {
                String header = "BEGIN:VCALENDAR\n" +
                        "PRODID:-//tzurl.org//NONSGML Olson 2020b//EN\n" +
                        "VERSION:2.0\n" +
                        "METHOD:PUBLISH\n" +
                        "X-MS-OLK-FORCEINSPECTOROPEN:FALSE\n" +
                        "BEGIN:VTIMEZONE\n" +
                        "TZID:Europe/Samara\n" +
                        "LAST-MODIFIED:20201011T015911Z\n" +
                        "TZURL:http://tzurl.org/zoneinfo-outlook/Europe/Samara\n" +
                        "X-LIC-LOCATION:Europe/Samara\n" +
                        "BEGIN:STANDARD\n" +
                        "TZNAME:+04\n" +
                        "TZOFFSETFROM:+0400\n" +
                        "TZOFFSETTO:+0400\n" +
                        "DTSTART:19700101T000000\n" +
                        "END:STANDARD\n" +
                        "END:VTIMEZONE\n";
                writer.write(header);
                writer.write(p.getVElement());
                writer.write("END:VCALENDAR\n");
            }
            writer.flush();
        }
        catch(IOException ex){
            System.out.println("[RosdistantExporter] Can't write file");
        }
    }
}
