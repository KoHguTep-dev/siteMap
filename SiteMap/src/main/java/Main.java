import java.io.IOException;
import java.util.Set;

public class Main {

    public static final String LINK = "https://your_link.com/";

    public static void main(String[] args) throws IOException {

        Set<String> set = Parser.getMap(LINK);
        Parser.writeFile(set, LINK);

    }
}