import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class Parser extends RecursiveTask<Set<String>> {

    private String link;
    private static Set<String> links = new TreeSet<>();
    private static Set<String> lostLinks = new TreeSet<>();

    public Parser(String link) {
        this.link = link;
    }

    @Override
    protected Set<String> compute() {
        List<Parser> parsers = new ArrayList<>();
        Set<String> set = new TreeSet<>();
        if (!links.contains(link)) {
            links.add(link);
            try {
                Thread.sleep(200);
                Document document = Jsoup.connect(link).get();
                System.out.println(link);
                Elements elements = document.getElementsByTag("a");

                String[] l = link.split("/");
                List<String> list = elements.stream()
                        .map(e -> e.attr("abs:href"))
                        .filter(s -> s.matches(".*//" + l[2] + ".*"))
                        .collect(Collectors.toList());

                list.removeIf(s -> s.contains("#") ||
                        s.contains("@") ||
                        s.contains("?") ||
                        s.contains("=") ||
                        s.matches(".+" + l[2] + ".+\\..+") ||
                        s.matches(link));
                set = Set.copyOf(list);

                for (String s : set) {
                    if (!links.contains(s)) {
                        Parser parser = new Parser(s);
                        parser.fork();
                        parsers.add(parser);
                    }
                }
            } catch (HttpStatusException ex) {
                System.out.println(ex.getMessage() + ": " + link);
                lostLinks.add(link);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            for (Parser p : parsers) {
                links.addAll(p.join());
            }
        }
        return set;
    }

    //use ForkJoinPool
    public static Set<String> getMap(String link) {
        new ForkJoinPool().invoke(new Parser(link));
        return links;
    }

    //use recursion
    public static Set<String> parser(String link) throws IOException, InterruptedException {
        links.add(link);
        try {
            Thread.sleep(200);
            Document document = Jsoup.connect(link).get();
            System.out.println(link);
            Elements elements = document.getElementsByTag("a");

            String[] l = link.split("/");
            List<String> list = elements.stream()
                    .map(e -> e.attr("abs:href"))
                    .filter(s -> s.matches(".*//" + l[2] + ".*"))
                    .collect(Collectors.toList());

            Set<String> set = Set.copyOf(list);
            list.clear();
            list = new ArrayList<>(set);

            list.removeIf(s -> s.contains("#") ||
                    s.contains("@") ||
                    s.contains("?") ||
                    s.contains("=") ||
                    s.matches(".+" + l[2] + ".+\\..+") ||
                    s.matches(link));

            for (String s : list) {
                if (!links.contains(s)) {
                    parser(s);
                }
            }
        } catch (HttpStatusException ex) {
            System.out.println(ex.getMessage() + ": " + link);
            lostLinks.add(link);
        }
        return links;
    }

    private static void getActualLinks(Set<String> set) {
        for (String s : lostLinks) {
            set.remove(s);
        }
    }

    public static Set<String> readFile(String path) throws IOException {
        Set<String> set = new TreeSet();
        File f = new File(path);
        FileReader fr = new FileReader(f);
        BufferedReader bf = new BufferedReader(fr);
        String line = bf.readLine();
        while (line != null) {
            set.add(line);
            line = bf.readLine();
        }
        return set;
    }

    public static void writeFile(Set<String> set, String link) throws IOException {
        getActualLinks(set);
        formatList(set, link);
        String[] l = link.split("/");
        File file = new File("data/" + l[2] + ".txt");
        FileWriter writer = new FileWriter(file);
        for (String s : set) {
            writer.write(s + "\n");
        }
        writer.flush();
    }

    private static void formatList(Set<String> set, String link) {
        Set<String> set1 = new TreeSet<>();
        for (String s : set) {
            String s1 = s.replace(link, "");
            String[] strings = s1.split("/");
            s = addTab(s, strings);
            set1.add(s);
        }
        set.clear();
        set.addAll(set1);
    }

    private static String addTab(String string, String[] strings) {
        String s = "";
        String s1;
        for (String s2 : strings) {
            if (!s2.equals(""))
                s = "\t" + s;
        }
        s1 = s + string;
        return s1;
    }

}
