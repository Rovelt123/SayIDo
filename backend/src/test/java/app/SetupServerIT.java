package app;

import app.server.Setup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class SetupServerIT extends SetupTest {

protected Setup testServerSetup;
    protected final int testPort = 9393;

    // ________________________________________________________

@BeforeEach
    void serverSetup() {
    // em is initialized in SetupTest
testServerSetup = new Setup(em, testPort);

testServerSetup.initialize();
}

// ________________________________________________________

@AfterEach
    void  stopServer(){
    testServerSetup.endSession();
}
}
