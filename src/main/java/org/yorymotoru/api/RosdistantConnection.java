package org.yorymotoru.api;

import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;

import static org.yorymotoru.api.RosdistantURLs.*;

public class RosdistantConnection {
    private final static String[] DT_START = {"083000", "101000", "122500", "140500", "162000", "180000"};
    private final static String[] DT_END = {"100000", "114000", "135500", "153500", "175000", "193000"};

    private String login;
    private String password;

    private final char sh = '\\';

    public RosdistantConnection(String login, String password) {
        this.login = login;
        this.password = password;

        try {
            Content result = null;

            result = Request.Get(ROSDISTANT_HOME)
                    .execute().returnContent();
            Document document = Jsoup.parse(result.asString(), ROSDISTANT_HOME);
            Elements field = document.select("#block-login > input[type=hidden]:nth-child(3)");

            final Collection<NameValuePair> paramsForLogin = new ArrayList<>();
            paramsForLogin.add(new BasicNameValuePair("username", login));
            paramsForLogin.add(new BasicNameValuePair("password", password));
            paramsForLogin.add(new BasicNameValuePair("logintoken", field.attr("value")));
            paramsForLogin.add(new BasicNameValuePair("submit", ""));

            result = Request.Post(ROSDISTANT_LOGIN)
                    .bodyForm(paramsForLogin, Charset.defaultCharset())
                    .execute().returnContent();

            if (result.asString().contains("<title>Ввод СНИЛС</title>")) {
                result = Request.Post(ROSDISTANT_SNILS)
                        .bodyForm(new BasicNameValuePair("skip", ""))
                        .execute().returnContent();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Practice> getPractices() {
        ArrayList<Practice> practices = new ArrayList<>();
        Content result = null;
        try {
            result = Request.Get(ROSDISTANT_SCHEDULE)
                    .execute().returnContent();

            //System.out.println(result);

            Document document = Jsoup.parse(result.asString(), ROSDISTANT_HOME);
            Elements table = document.select("#region-main > div > div > table");
            String[] dates = new String[6];

            Elements rows = table.select("tbody");
            Elements heads = table.select("thead");

            for (int i = 0; i < heads.size(); i++) {
                Element head = heads.get(i);
                Element row = rows.get(i + 1);

                Elements cols = head.select(("tr > th"));
                for (int ii = 1; ii < cols.size(); ii++) {
                    String s = cols.get(ii).select("h4 > a").text();
                    s = s.substring(s.length() - 10);
                    String[] ss = s.split("\\.");
                    s = ss[2] + ss[1] + ss[0];
                    dates[ii - 1] = s;
                }

                Elements rws = row.select("tr");
                for (int j = 0; j < rws.size(); j++) {
                    cols = rws.get(j).select("td");

                    int sn = Integer.parseInt(cols.get(0).select("h5 > p").text());
                    for (int ii = 1; ii < cols.size(); ii++) {
                        String s = cols.get(ii).select("h5 > p").text();
                        if (!s.equals("")) {
                            Practice p = new Practice();

                            p.setDescription("Тип:" + s.split("Тип:")[1]);
                            if (cols.get(ii).select("h5 > p > a").size() > 0) {
                                p.setLink(cols.get(ii).select("h5 > p > a").first().attr("href"));
                                p.setDescription(p.getDescription().replaceAll("перейти ", Matcher.quoteReplacement(p.getLink() + "\\n")));
                            }

                            p.setDescription(p.getDescription().replaceAll("Преподаватель:", Matcher.quoteReplacement("\\nПреподаватель:")));
                            p.setDescription(p.getDescription().replaceAll("Аудитория:", Matcher.quoteReplacement("\\nАудитория:")));
                            p.setDescription(p.getDescription().replaceAll("Мероприятие:", Matcher.quoteReplacement("\\nМероприятие:")));
                            p.setDescription(p.getDescription().replaceAll("Подгруппа:", Matcher.quoteReplacement("\\nПодгруппа:")));
                            p.setDescription(p.getDescription().replaceAll("Ссылка на мероприятие появится позднее. ",
                                    Matcher.quoteReplacement("\\nСсылка на мероприятие появится позднее. \\n")));

                            p.setType(p.getDescription().split("Тип: ")[1].split(Matcher.quoteReplacement("\\n"))[0].trim());
                            p.setSummary(p.getType() + ": " + s.split("Тип:")[0]);
                            p.setLocation(s.split("Аудитория:")[1].split("Ссылка|Мероприятие")[0]);
                            //TODO: Надо переписать под обычный формат времени
                            p.setStartTime(dates[ii - 1] + "T" + DT_START[sn - 1]);
                            p.setEndTime(dates[ii - 1] + "T" + DT_END[sn - 1]);
                            p.setUid(p.getStartTime() + p.getEndTime() + i + ii);

                            practices.add(p);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return practices;
    }
}
