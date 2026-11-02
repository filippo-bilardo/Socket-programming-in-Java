/** Variante finita: tre datagrammi, stesso gruppo e stessa porta su entrambi i lati. */
public class BaseSender {
    public static void main(String[] args) throws Exception { MulticastSender.main(args.length == 0 ? new String[]{"239.255.0.1", "19876", "100", "1", "3"} : args); }
}
