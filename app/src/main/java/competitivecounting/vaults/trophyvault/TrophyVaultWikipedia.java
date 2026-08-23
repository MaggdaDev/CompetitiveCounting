package competitivecounting.vaults.trophyvault;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class TrophyVaultWikipedia {
    private static final int MIN_PAGE_SIZE_BITES = 2000;
    private static final String WIKI_URI = "https://en.wikipedia.org/",
            API_ADDON = "w/api.php?",
            PAGES_ADDON = "wiki/",
            NAME_QUERY_PARAMS = "action=query"
                    + "&format=json"
                    + "&list=random"
                    + "&rnnamespace=0"
                    + "&minsize=" + MIN_PAGE_SIZE_BITES
                    + "&rnlimit=1",
            USER_AGENT = "https://github.com/MaggdaDev/CompetitiveCounting (davidbetko05@gmail.com)";
    private final HttpRequest nameQuery;
    private HttpClient client;
    public final String[] EXCLUDED_HEADINGS = {
            "See also",
            "References",
            "External links",
            "Further reading",
            "Notes",
            "Bibliography",
            "Sources",
            "Footnotes"
    };

    public TrophyVaultWikipedia() {
        client = HttpClient.newHttpClient();
        String url = WIKI_URI + API_ADDON + NAME_QUERY_PARAMS;
        nameQuery = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(
                        "User-Agent",
                        USER_AGENT
                )
                .header("Accept-Encoding", "gzip")
                .GET()
                .build();
    }

    public WikiArticelObject getRandomPage() throws IOException, InterruptedException {
        String title = getRandomTitle();
        if (title.isEmpty()) {
            return null;
        }
        List<SectionObject> sections = getTextSections(title.replace(" ", "_"));
        if (sections.isEmpty()) {
            Thread.sleep(50);
            return getRandomPage();
        }
        WikiArticelObject wikiArticelObject = new WikiArticelObject(title, sections.toArray(new SectionObject[0]));
        return wikiArticelObject;
    }

    private String getRandomTitle() {
        HttpResponse<byte[]> response;
        try {
            response = client.send(
                    nameQuery,
                    HttpResponse.BodyHandlers.ofByteArray()
            );
        } catch (IOException | InterruptedException e) {
            System.err.println("Error while fetching random page name from Wikipedia: " + e.getMessage());
            return "";
        }
        String body;
        if (response.headers()
                .firstValue("Content-Encoding")
                .map("gzip"::equalsIgnoreCase)
                .orElse(false)) {
            try (InputStream gzip = new GZIPInputStream(
                    new ByteArrayInputStream(response.body()))) {
                body = new String(
                        gzip.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            } catch (IOException e) {
                System.err.println("Error while decompressing gzip response from Wikipedia: " + e.getMessage());
                return "";
            }
        } else {
            body = new String(
                    response.body(),
                    StandardCharsets.UTF_8
            );
        }
        JsonObject bodyJson = JsonParser.parseString(body).getAsJsonObject();
        String randomTitle = bodyJson.getAsJsonObject("query")
                .getAsJsonArray("random")
                .get(0).getAsJsonObject()
                .get("title")
                .getAsString();
        return randomTitle;
    }

    public List<SectionObject> getTextSections(String title)
            throws IOException, InterruptedException {
        String url =
                WIKI_URI + "w/rest.php/v1/page/"
                        + title
                        + "/html";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept-Encoding", "gzip")
                .GET()
                .build();
        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );
        if (response.statusCode() != 200) {
            throw new IOException(
                    "Wikipedia returned HTTP "
                            + response.statusCode()
            );
        }
        String html = decodeResponse(response);
        return extractSections(html);
    }

    private String decodeResponse(HttpResponse<byte[]> response)
            throws IOException {
        boolean gzip = response.headers()
                .firstValue("Content-Encoding")
                .map(value -> value.equalsIgnoreCase("gzip"))
                .orElse(false);

        if (gzip) {
            try (GZIPInputStream input = new GZIPInputStream(
                    new ByteArrayInputStream(response.body()))) {

                return new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }
        }

        return new String(
                response.body(),
                StandardCharsets.UTF_8
        );
    }

    private List<SectionObject> extractSections(
            String html
    ) {

        Document document = Jsoup.parse(html);

        /*
         * Remove elements that should not become
         * part of the article text.
         */
        document.select(
                "figure, table, sup, style, script, nav, aside"
        ).remove();

        List<SectionObject> sections =
                new ArrayList<>();

        String currentTitle = "";
        StringBuilder currentContent =
                new StringBuilder();

        Elements elements = document.select(
                "h1, h2, h3, h4, h5, h6, p"
        );

        for (Element element : elements) {

            String tag = element.tagName();
            String text = element.text().trim();

            if (text.isEmpty()) {
                continue;
            }

            boolean isHeading =
                    tag.matches("h[1-6]");

            if (isHeading) {

                /*
                 * Save the previous section before
                 * starting the next one.
                 */
                if (acceptSection(currentTitle, currentContent)) {
                    sections.add(
                            SectionObjectFactory.create(
                                    currentTitle,
                                    currentContent
                                            .toString()
                                            .trim()
                            )
                    );
                }
                currentTitle = text;
                currentContent.setLength(0);
            } else {
                if (currentContent.length() > 0) {
                    currentContent.append("\n\n");
                }
                currentContent.append(text);
            }
        }

        /*
         * Add the final section.
         */
        if (acceptSection(currentTitle, currentContent)) {
            sections.add(
                    SectionObjectFactory.create(
                            currentTitle,
                            currentContent
                                    .toString()
                                    .trim()
                    )
            );
        }
        return sections;
    }

    private boolean acceptSection(String title, StringBuilder content) {
        for (String excluded : EXCLUDED_HEADINGS) {
            if (title.equalsIgnoreCase(excluded)) {
                return false;
            }
        }
        return !title.isEmpty()
                || content.length()>0;
    }
}
