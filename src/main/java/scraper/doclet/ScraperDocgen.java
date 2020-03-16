package scraper.doclet;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ScraperDocgen {
    static Map<String, Object> docs = new HashMap<>();
    public static void main(String[] args) {
        if(args.length < 2) throw new IllegalArgumentException("First argument output file, following arguments folder paths to include");

        DirExplorer.FileHandler handleNode = (level, path, file) -> {
            try {
                new VoidVisitorAdapter<>() {
                    @Override
                    public void visit(JavadocComment comment, Object arg) {
                        super.visit(comment, arg);

                        String nname = file.getName().substring(0, file.getName().length()-5);

                        // replace Abstract
                        nname = nname.replaceAll("Abstract", "");

                        // empty javadoc node: return
                        Optional<Node> nod = comment.getCommentedNode();
                        if (nod.isEmpty()) return;

                        Node nodd = nod.get();
                        Map nodeDoc = (Map) docs.getOrDefault(nname, new HashMap<>());
                        docs.put(nname, nodeDoc);
                        List fields = (List) nodeDoc.getOrDefault("fields", new LinkedList<>());
                        nodeDoc.put("fields", fields);

                        nodeDoc.putIfAbsent("flow", "false");

                        // top level doc
                        if (nodd instanceof ClassOrInterfaceDeclaration) {
                            nodeDoc.put("doc", Map.of("txt", cleanComment(comment.getContent())));
                            try {
                                String ext = ((ClassOrInterfaceDeclaration) nodd).getImplementedTypes().get(0).toString();
                                if(!ext.contains("Container")) {
                                    if(ext.equalsIgnoreCase("Node")) {
                                        nodeDoc.put("extends", List.of("Node"));
                                    } else {
                                        nodeDoc.put("extends", List.of("Node", ext));
                                    }
                                } else {
                                    nodeDoc.put("extends", List.of());
                                }
                            } catch (Exception ignored){
                                nodeDoc.put("extends", List.of());
                            }

                            // stateful
                            {
                                Optional<AnnotationExpr> annot = ((ClassOrInterfaceDeclaration) nodd).getAnnotationByName("Stateful");
                                nodeDoc.put("stateful", String.valueOf(annot.isPresent()));
                            }

                            // io
                            {
                                Optional<AnnotationExpr> annot = ((ClassOrInterfaceDeclaration) nodd).getAnnotationByName("Io");
                                nodeDoc.put("io", String.valueOf(annot.isPresent()));
                            }

                            // version
                            Optional<AnnotationExpr> annotOpt = ((ClassOrInterfaceDeclaration) nodd).getAnnotationByName("NodePlugin");
                            if(annotOpt.isEmpty()) return;
                            AnnotationExpr annot = annotOpt.get();
                            String version;
                            if(annot instanceof SingleMemberAnnotationExpr) {
                                version = ((SingleMemberAnnotationExpr) annot).getMemberValue().toString();
                            } else {
                                Optional<MemberValuePair> defaultVal = ((NormalAnnotationExpr) annot).getPairs().stream()
                                        .filter(p -> p.getName().getId().equals("value"))
                                        .findFirst();
                                if (defaultVal.isEmpty()){
                                    version = "\"0.0.0\"";
                                } else {
                                    version = defaultVal.get().getValue().toString();
                                }
                            }
                            nodeDoc.put("version", version.substring(1, version.length()-1));
                        }


                        // field doc
                        if (nodd instanceof FieldDeclaration && ((FieldDeclaration) nodd).getAnnotationByName("FlowKey").isPresent()) {
                            FieldDeclaration fieldDeclaration = (FieldDeclaration) nodd;
                            String fieldName = fieldDeclaration.getVariable(0).getNameAsString();

                            Map<String, String> fieldDoc = new HashMap<>();
                            fieldDoc.put("txt", cleanComment(comment.getContent()));
                            fieldDoc.put("name", fieldName);
                            fieldDoc.put("type", ((FieldDeclaration) nodd).getElementType().toString());

                            if(((FieldDeclaration) nodd).getElementType().toString().contains("Address")) {
                                nodeDoc.put("flow", "true");
                            }

                            try {
                                NormalAnnotationExpr annot = (NormalAnnotationExpr) ((FieldDeclaration) nodd).getAnnotationByName("FlowKey").get();
                                Optional<MemberValuePair> defaultVal = annot.getPairs().stream()
                                        .filter(p -> p.getName().getId().equals("defaultValue"))
                                        .findFirst();

                                Optional<MemberValuePair> mandatory = annot.getPairs().stream()
                                        .filter(p -> p.getName().getId().equals("mandatory"))
                                        .filter(p -> p.getValue().toString().equalsIgnoreCase("true"))
                                        .findFirst();

                                fieldDoc.put("mandatory", mandatory.isPresent() ? "true" : "false");
                                fieldDoc.put("defaultValue", defaultVal.isPresent() ? unescapeString(defaultVal.get().getValue().toString()) : "null");
                            } catch (Exception e) {
                                fieldDoc.put("mandatory", "false");
                                fieldDoc.put("defaultValue", "null");
                            }

                            if (((FieldDeclaration) nodd).getAnnotationByName("Argument").isPresent()) {
                                fieldDoc.put("argument", "true");
                            } else {
                                fieldDoc.put("argument", "false");
                            }
                            fields.add(fieldDoc);
                        }
                    }

                }.visit(JavaParser.parse(file), null);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        String output = args[0];
        String[] inputPaths = Arrays.copyOfRange(args, 1, args.length);

        for (String inputPath : inputPaths) {
            File projectDir = new File(inputPath);
            new DirExplorer((level, path, file) ->
                    path.endsWith(".java") && !path.contains("module-info") && path.contains("Node"),
                    handleNode).explore(projectDir);
        }

        HtmlGenerator.main(output);
    }

    private static String unescapeString(String toString) {
        String removeOuter = toString.substring(1, toString.length()-1);
        return removeOuter.replaceAll("\\\\\"", "\"");
    }

    private static String cleanComment(String content) {
        Matcher m = Pattern.compile("^\\s*\\*?",Pattern.MULTILINE).matcher(content);
        return m.replaceAll("").replaceAll("\\n$", "");
    }
}