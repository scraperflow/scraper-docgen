package scraper.doclet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import j2html.tags.ContainerTag;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static j2html.TagCreator.*;

@SuppressWarnings({"rawtypes", "unchecked"}) // do it again with proper classes
public class HtmlGenerator {
    public static void main(String output) {
        ObjectMapper m =  new ObjectMapper();
        String str;
        try {
            str = m.writeValueAsString(ScraperDocgen.docs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
        String htmlStr = html(
                head(
                        meta().withName("viewport").withContent("width=device-width, initial-scale=1, shrink-to-fit=no"),
                        meta().withTitle("Node Documentation"),
                        meta().withCharset("utf-8"),
                        styleWithInlineFile("/scraper/doclet/style.css"),
                        scriptWithInlineFile("/scraper/doclet/script.js")
                ),
                body(
                        div(sideNav()).withClasses("sidenav"),
                        div().withId("main").withClasses("main"),
                        script( rawHtml("map = " + str) )
                )
        ).render();

        try(PrintWriter pw = new PrintWriter(new FileOutputStream(output))) {
            pw.println(htmlStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static ContainerTag[] sideNav() {
        // functional
        @SuppressWarnings("unchecked")
        Stream<ContainerTag> funcAndStream = Stream.concat(
                ScraperDocgen.docs.keySet()
                        .stream()
                        .sorted()
                        .filter(p ->
                                ((List)
                                        ((Map)
                                                ScraperDocgen.docs.
                                                        getOrDefault(p, Map.of()))
                                                .getOrDefault("extends", List.of()))
                                        .contains("FunctionalNode"))
                        .map(o ->
                                div(
                                        a(o).attr(rawHtml("onclick=\"showDoc('" + o + "')\"").render()),
                                        span("λ").withClasses("lambda"),
                                        ((String) ((Map) ScraperDocgen.docs.getOrDefault(o, Map.of())).getOrDefault("flow", "false")).equalsIgnoreCase("true") ?
                                                span("⇒").withClasses("flow") :
                                                span(),
                                        ((String) ((Map) ScraperDocgen.docs.getOrDefault(o, Map.of())).getOrDefault("stateful", "false")).equalsIgnoreCase("true") ?
                                                span("Ŝ").withClasses("stateful") :
                                                span(),
                                        ((String) ((Map) ScraperDocgen.docs.getOrDefault(o, Map.of())).getOrDefault("io", "false")).equalsIgnoreCase("true") ?
                                                span("I/O").withClasses("io") :
                                                span()
                                )
                        ),
                ScraperDocgen.docs.keySet()
                        .stream()
                        .sorted()
                        .filter(p -> ((List) ((Map) ScraperDocgen.docs.getOrDefault(p, Map.of())).getOrDefault("extends", List.of())).contains("StreamNode"))
                        .map(o ->
                                div(
                                        a(o).attr(rawHtml("onclick=\"showDoc('" + o + "')\"").render()),
                                        span("≫").withClasses("stream"),
                                        ((String) ((Map) ScraperDocgen.docs.getOrDefault(o, Map.of())).getOrDefault("flow", "false")).equalsIgnoreCase("true") ?
                                                span("⇒").withClasses("flow") :
                                                span(),
                                        ((String) ((Map) ScraperDocgen.docs.getOrDefault(o, Map.of())).getOrDefault("stateful", "false")).equalsIgnoreCase("true") ?
                                                span("Ŝ").withClasses("stateful") :
                                                span(),
                                        ((String) ((Map) ScraperDocgen.docs.getOrDefault(o, Map.of())).getOrDefault("io", "false")).equalsIgnoreCase("true") ?
                                                span("I/O").withClasses("io") :
                                                span()
                                )
                        )
        );

        Stream<ContainerTag> allNodes = Stream.concat(
                funcAndStream,
                ScraperDocgen.docs.keySet()
                        .stream()
                        .sorted()
                        .filter(p ->
                                p.endsWith("Node") &&
                                !((List) ((Map) ScraperDocgen.docs.getOrDefault(p, Map.of())).getOrDefault("extends", List.of())).contains("StreamNode") &&
                                        !((List) ((Map) ScraperDocgen.docs.getOrDefault(p, Map.of())).getOrDefault("extends", List.of())).contains("FunctionalNode")
                        )
                        .map(o -> div(
                                a(o).attr(rawHtml("onclick=\"showDoc('" + o + "')\"").render()),
                                ((String) ((Map) ScraperDocgen.docs.getOrDefault(o, Map.of())).getOrDefault("flow", "false")).equalsIgnoreCase("true") ?
                                        span("⇒").withClasses("flow") :
                                        span(),
                                ((String) ((Map) ScraperDocgen.docs.getOrDefault(o, Map.of())).getOrDefault("stateful", "false")).equalsIgnoreCase("true") ?
                                        span("Ŝ").withClasses("stateful") :
                                        span(),
                                ((String) ((Map) ScraperDocgen.docs.getOrDefault(o, Map.of())).getOrDefault("io", "false")).equalsIgnoreCase("true") ?
                                        span("I/O").withClasses("io") :
                                        span()
                        ))
        );

        return allNodes
                .collect(Collectors.toList())
                .toArray(ContainerTag[]::new);
    }
}
