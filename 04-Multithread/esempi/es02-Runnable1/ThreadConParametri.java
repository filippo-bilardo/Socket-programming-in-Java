public class ThreadConParametri implements Runnable {
    private final String messaggio;
    private final int ripetizioni;
    
    public ThreadConParametri(String messaggio, int ripetizioni) {
        this.messaggio = messaggio;
        this.ripetizioni = ripetizioni;
    }
    
    @Override
    public void run() {
        System.out.println("Avvio Thread " + Thread.currentThread().getName());
        for (int i = 0; i < ripetizioni; i++) {
            System.out.println(messaggio + " (" + (i+1) + "/" + ripetizioni + ")");
            try {
                Thread.sleep(1000); // Pausa di 1 secondo
            } catch (InterruptedException e) {
                System.out.println("Thread interrotto");
                return;
            }
        }
    }
    
    public static void main(String[] args) {
        ThreadConParametri runnable1 = new ThreadConParametri("Primo thread", 5);
        Thread t1 = new Thread(runnable1, "Thread1");
        Thread t2 = new Thread(new ThreadConParametri("Secondo thread", 3), "Thread2");
        
        t1.start();
        t2.start();
    }
}