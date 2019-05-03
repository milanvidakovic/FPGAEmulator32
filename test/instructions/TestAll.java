package instructions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import instructions.andorxor.TestAndOrXor;

@RunWith(Suite.class)
@SuiteClasses({
        TestAndOrXor.class
        })
public class TestAll {

}
