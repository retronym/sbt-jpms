package user.view;

public class RunTests {
    public static void main(String[] args) {
        new UserViewTests().accessExportedTypes();
        System.out.println("Finished: " + RunTests.class);
    }
}
