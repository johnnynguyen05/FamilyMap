package UnitTests.DAO;

import DataAccess.DataAccessException;
import DataAccess.Database;
import DataAccess.EventDao;
import Model.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//We will use this to test that our insert method is working and failing in the right ways
public class EventDAOTest {
    private Database db;
    private Event bestEvent;
    private EventDao eDao;

    @BeforeEach
    public void setUp() throws DataAccessException
    {
        //here we can set up any classes or variables we will need for the rest of our tests
        //lets create a new database
        db = new Database();
        //and a new event with random data
        bestEvent = new Event("Biking_123A", "Gale", "Gale123A",
                35.9f, 140.1f, "Japan", "Ushiku",
                "Biking_Around", 2016);
        //Here, we'll open the connection in preparation for the test case to use it
        Connection conn = db.getConnection();
        //Let's clear the database as well so any lingering data doesn't affect our tests
        db.clearTables();
        //Then we pass that connection to the EventDAO so it can access the database
        eDao = new EventDao(conn);
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        //Here we close the connection to the database file so it can be opened elsewhere.
        //We will leave commit to false because we have no need to save the changes to the database
        //between test cases
        db.closeConnection(false);
    }

    @Test
    public void insertPass() throws DataAccessException {
        //While insert returns a bool we can't use that to verify that our function actually worked
        //only that it ran without causing an error
        eDao.insert(bestEvent);
        //So lets use a find method to get the event that we just put in back out
        Event compareTest = eDao.find(bestEvent.getEventID());
        //First lets see if our find found anything at all. If it did then we know that if nothing
        //else something was put into our database, since we cleared it in the beginning
        assertNotNull(compareTest);
        //Now lets make sure that what we put in is exactly the same as what we got out. If this
        //passes then we know that our insert did put something in, and that it didn't change the
        //data in any way
        assertEquals(bestEvent, compareTest);
    }

    @Test
    public void insertFail() throws DataAccessException {
        //lets do this test again but this time lets try to make it fail
        //if we call the method the first time it will insert it successfully
        eDao.insert(bestEvent);
        //but our sql table is set up so that "eventID" must be unique. So trying to insert it
        //again will cause the method to throw an exception
        //Note: This call uses a lambda function. What a lambda function is is beyond the scope
        //of this class. All you need to know is that this line of code runs the code that
        //comes after the "()->" and expects it to throw an instance of the class in the first parameter.
        assertThrows(DataAccessException.class, ()-> eDao.insert(bestEvent));
    }

    @Test //Checks all remove variations
    public void removeSuccess() throws DataAccessException {
        eDao.insert(bestEvent);
        eDao.remove(bestEvent);
        assertNull(eDao.find(bestEvent.getEventID()));

        eDao.insert(bestEvent);
        eDao.remove(bestEvent.getAssociatedUsername());
        assertNull(eDao.find(bestEvent.getEventID()));
    }

    @Test
    public void removeAlternative() throws DataAccessException {
        assertDoesNotThrow(() -> eDao.remove(bestEvent));

        assertDoesNotThrow(() -> eDao.remove(bestEvent.getAssociatedUsername()));
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        eDao.insert(bestEvent);
        eDao.clear();
        assertNull(eDao.find(bestEvent.getEventID()));
    }

    @Test
    void clearAlternative() throws DataAccessException {
        //make sure clear with nothing in database does not crash
        assertDoesNotThrow(() -> eDao.clear());
    }

    @Test //Checks all find methods
    void findSuccess() throws DataAccessException {
        eDao.insert(bestEvent);

        Event firstEvent = eDao.find(bestEvent.getEventID());
        Event secondEvent = eDao.find(bestEvent.getEventID(), bestEvent.getAssociatedUsername());
        Event thirdEvent = eDao.findType(bestEvent.getEventType(), bestEvent.getPersonID());

        assertNotNull(firstEvent);
        assertNotNull(secondEvent);
        assertNotNull(thirdEvent);

        assertEquals(bestEvent, firstEvent);
        assertEquals(bestEvent, secondEvent);
        assertEquals(bestEvent, thirdEvent);
    }

    @Test
    void findAlternative() throws DataAccessException {
        Event firstEvent = eDao.find(bestEvent.getEventID());
        Event secondEvent = eDao.find(bestEvent.getEventID(), bestEvent.getAssociatedUsername());
        Event thirdEvent = eDao.findType(bestEvent.getEventType(), bestEvent.getPersonID());

        assertNull(firstEvent);
        assertNull(secondEvent);
        assertNull(thirdEvent);
    }

    @Test
    void findFromUserTest() throws DataAccessException {
        Event firstEvent = new Event("Biking_123A", "Gale", "Gale123A",
                35.9f, 140.1f, "Japan", "Ushiku",
                "Biking_Around", 2016);
        Event secondEvent = new Event("Biking_124A", "Gale", "Gale123A",
                35.9f, 140.1f, "Japan", "Ushiku",
                "Biking_Around", 2016);

        List<Event> bestEvents = new ArrayList<>();
        bestEvents.add(firstEvent);
        bestEvents.add(secondEvent);

        eDao.insert(firstEvent);
        eDao.insert(secondEvent);

        List<Event> events = eDao.findFromUser(firstEvent.getAssociatedUsername());

        assertNotNull(events);
        assertEquals(bestEvents, events);
    }

    @Test
    void findFromUserTestAlternative() throws DataAccessException {
        Event firstEvent = new Event("Biking_123A", "Gale", "Gale123A",
                35.9f, 140.1f, "Japan", "Ushiku",
                "Biking_Around", 2016);
        Event secondEvent = new Event("Biking_124A", "Gale", "Gale123A",
                35.9f, 140.1f, "Japan", "Ushiku",
                "Biking_Around", 2016);

        assertDoesNotThrow(() -> eDao.findFromUser(firstEvent.getAssociatedUsername()));
    }
}
