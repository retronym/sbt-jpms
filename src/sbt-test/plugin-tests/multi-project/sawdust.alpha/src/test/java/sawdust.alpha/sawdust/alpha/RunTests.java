package sawdust.alpha;

public class RunTests {
    public static void main(String[] args) {
        new AlphaProtectedTests().simpleNameIsAlphaProtected();
        new AlphaPublicTests().simpleNameIsAlphaPublic();
        System.out.println("Finished: " + RunTests.class);
    }
}
