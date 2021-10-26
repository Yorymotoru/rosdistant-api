package org.yorymotoru.api;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Practice {

    @Getter
    @Setter
    private String uid;

    @Getter
    @Setter
    private String summary;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String startTime;

    @Getter
    @Setter
    private String endTime;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private String link;

    @Getter
    @Setter
    private String type;

    public String getVElement() {
        return "BEGIN:VEVENT\n" +
                "UID:" + uid + "\n" +
                "SUMMARY;LANGUAGE=ru:" + summary + "\n" +
                "DESCRIPTION:" + description + "\n" +
                "LOCATION:" + location + "\n" +
                "DTSTART:" + startTime + "\n" +
                "DTEND:" + endTime + "\n" +
                "BEGIN:VALARM\n" +
                "TRIGGER:-PT10M\n" +
                "ACTION:DISPLAY\n" +
                "DESCRIPTION:Reminder\n" +
                "END:VALARM\n" +
                "END:VEVENT\n";
    }

}
