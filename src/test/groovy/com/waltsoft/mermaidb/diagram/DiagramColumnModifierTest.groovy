import com.waltsoft.mermaidb.diagram.DiagramColumnModifier
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import static org.junit.jupiter.api.Assertions.assertEquals

class DiagramColumnModifierTest {

    @Nested
    @DisplayName("Tests for toUppercase method")
    class ToUppercaseTest {

        private static final String SUCCESS_SIMPLE_IN  = "        String name"
        private static final String SUCCESS_SIMPLE_OUT = "        String NAME"

        private static final String SUCCESS_META_IN    = "        int id PK \"Primary Key\""
        private static final String SUCCESS_META_OUT   = "        int ID PK \"Primary Key\""

        private static final String SUCCESS_SPACE_IN   = "        boolean     active"
        private static final String SUCCESS_SPACE_OUT  = "        boolean ACTIVE"

        private static final String IGNORE_TABLE       = "    USER {"
        private static final String IGNORE_RELATION    = "USER ||--o{ ORDER : places"
        private static final String IGNORE_INDENT      = "         int id"

        private static final String EDGE_EMPTY         = ""

        @ParameterizedTest(name = "[{index}] Input: ''{0}'' => Expected: ''{1}''")
        @MethodSource("provideTestCases")
        void "should process diagram lines correctly"(String input, String expected) {
            def modifier = new DiagramColumnModifier(input)
            def result = modifier.toUppercase()
            assertEquals(expected, result)
        }

        private static Stream<Arguments> provideTestCases() {
            return Stream.of(

                    Arguments.of(SUCCESS_SIMPLE_IN, SUCCESS_SIMPLE_OUT),
                    Arguments.of(SUCCESS_META_IN,   SUCCESS_META_OUT),
                    Arguments.of(SUCCESS_SPACE_IN,  SUCCESS_SPACE_OUT),

                    Arguments.of(IGNORE_TABLE,    IGNORE_TABLE),
                    Arguments.of(IGNORE_RELATION, IGNORE_RELATION),
                    Arguments.of(IGNORE_INDENT,   IGNORE_INDENT),

                    Arguments.of(EDGE_EMPTY,    EDGE_EMPTY)
            )
        }
    }
}