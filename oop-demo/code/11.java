import java.util.Date;
import java.util.Random;
// java.lang.System needs no import

class PrintingObjects {
    public static void main(String[] args){
        Date d = new Date();
        System.out.println("Current date: " + d);
        System.out.println("Millisec. from 1/1/1970: " + d.getTime());

        Random r = new Random();
        System.out.println("Random number: " + r.nextInt());
        System.out.println("Another random number: " + r.nextInt());
        System.out.println("N. random in (0-99): " + r.nextInt(100));
        String vjava = System.getProperty("java.version");
        String osname = System.getProperty("os.name");
        String usrdir = System.getProperty("user.dir");
        System.out.println("Java version: " + vjava);
        System.out.println("OS Name: " + osname);
        System.out.println("Usr dir: " + usrdir);
    }
}
