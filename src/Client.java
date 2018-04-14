import java.io.*;

public class Client {

    private DistributedMap distributedMap;

    private void start() throws Exception {
        distributedMap = new DistributedMap();
        eventLoop();
    }

    private void eventLoop() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Opcje: \na - put\nb - remove\nc - get\nd - containsKey\ne - show map");
        while (true){
            switch (in.readLine().toLowerCase()){
                case "a":
                    System.out.print("Podaj klucz: ");
                    String key = in.readLine();
                    System.out.print("Podaj wartość: ");
                    String value = in.readLine();
                    this.distributedMap.put(key, value);
                    break;

                case "b":
                    System.out.print("Podaj klucz, który usunąć: ");
                    String key_ = in.readLine();
                    this.distributedMap.remove(key_);
                    break;

                case "c":
                    System.out.print("Podaj klucz: ");
                    String key__ = in.readLine();
                    String val = this.distributedMap.get(key__);
                    System.out.println("Value = " + val);
                    break;

                case "d":
                    System.out.print("Podaj klucz: ");
                    String key___ = in.readLine();
                    System.out.println(this.distributedMap.containsKey(key___));
                    break;
                case "e":
                    distributedMap.printMap();
            }

        }
    }

    public static void main(String[] args) throws Exception {
        new Client().start();
    }
}
