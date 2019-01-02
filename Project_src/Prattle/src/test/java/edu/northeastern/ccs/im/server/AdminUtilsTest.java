package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author oz
 */
public class AdminUtilsTest {

/**
 * test get instance
 */
    @Test
    public void testGetInstance() {
        AdminUtils utils = AdminUtils.getInstance();
        AdminUtils utils2 = AdminUtils.getInstance();

        assertEquals(utils, utils2);
    }

    /**
     * test subpoena group
     */
    @Test
    public void testSubpoenaGroup() {
        AdminUtils utils = AdminUtils.getInstance();
        utils.setSubpoenaForGroup("#DUMMYGROUP", "subpoena");

        List<String> listeners = Prattle.getActiveTaps("#DUMMYGROUP");
        assertTrue(listeners.contains("subpoena"));

        utils.removeSubpoenaForGroup("#DUMMYGROUP", "subpoena");
        assertFalse(listeners.contains("subpoena"));
    }

    /**
     * test subpoena users
     * @throws IOException
     */
    @Test
    public void testSubpoenaUsers() throws IOException {
        AdminUtils utils = AdminUtils.getInstance();
        utils.setSubpoenaForUsers("rak", "oz", "subpoena");

        List<String> listeners1 = Prattle.getActiveTaps("rak->oz");
        List<String> listeners2 = Prattle.getActiveTaps("oz->rak");

        assertTrue(listeners1.contains("subpoena"));
        assertTrue(listeners2.contains("subpoena"));

        utils.removeSubpoenaForUsers("rak", "oz", "subpoena");

        assertFalse(listeners1.contains("subpoena"));
        assertFalse(listeners2.contains("subpoena"));
    }

    /**
     * test invalid user
     * @throws IOException
     */
    @Test
    public void testInvalidUser() throws IOException {
        AdminUtils utils = AdminUtils.getInstance();
        utils.setSubpoenaForUsers("invalidusername1", "oz", "subpoena");

        List<String> listeners1 = Prattle.getActiveTaps("invalidusername1->oz");
        List<String> listeners2 = Prattle.getActiveTaps("oz->invalidusername1");
        // If they point to null objects, it means the subpoena for invalid users wasn't created.
        try {
            assertFalse(listeners1.contains("subpoena"));
        } catch (NullPointerException e) {
            assertTrue(true);
        }
        try {
            assertFalse(listeners2.contains("subpoena"));
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }
}
