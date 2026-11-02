/** Variante finita: tre datagrammi, stesso gruppo e stessa porta su entrambi i lati. */
public class BaseReceiver {
    public static void main(String[] args) throws Exception { MulticastReceiver.main(args.length == 0 ? new String[]{"239.255.0.1", "19876", "3"} : args); }
}
