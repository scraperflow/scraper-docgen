package scraper.doclet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import j2html.attributes.Attr;
import j2html.tags.ContainerTag;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
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
                        div(sideFilter()).withClasses("sidefilter"),
                        div(sideTextFilter()).withClasses("sidetextfilter"),
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

    private static ContainerTag[] sideFilter() {
        Function<String, String> filter = f -> rawHtml("onclick=\"filterNodes('" + f + "')\"").render();
        return List.of(
                span("Filter").withClasses("filter-text"),
                span("≫").withClasses("stream", "filter-btn").attr(filter.apply("stream")),
                span("⇒").withClasses("flow", "filter-btn").attr(filter.apply("flow")),
                span("Ŝ").withClasses("stateful", "filter-btn").attr(filter.apply("stateful")),
                span("IO").withClasses("io", "filter-btn").attr(filter.apply("io")),
                span("λ").withClasses("lambda", "filter-btn").attr(filter.apply("lambda"))
        ).toArray(new ContainerTag[0]);
    }

    private static ContainerTag[] sideTextFilter() {
        return List.of(
                div(input().withPlaceholder("Name...").withClass("filter-area"))
        ).toArray(new ContainerTag[0]);
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
                                                span("IO").withClasses("io") :
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
                                                span("IO").withClasses("io") :
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
                                !((List) ((Map) ScraperDocgen.docs.getOrDefault(p, Map.of())).getOrDefault("extends", List.of())).contains("StreamNode") &&
                                        !((List) ((Map) ScraperDocgen.docs.getOrDefault(p, Map.of())).getOrDefault("extends", List.of())).contains("FunctionalNode")
                        )
                        .filter(p ->
                                !((List) ((Map) ScraperDocgen.docs.getOrDefault(p, Map.of())).getOrDefault("fields", List.of())).isEmpty()
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
                                        span("IO").withClasses("io") :
                                        span()
                        ))
        );

        return allNodes
                .map(c -> c.withClass("node-list"))
                .collect(Collectors.toList())
                .toArray(ContainerTag[]::new);
    }
}
