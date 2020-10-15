package cl.figonzal.lastquakechile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import cl.figonzal.lastquakechile.managers.DateManager;

import static org.junit.Assert.assertEquals;


@RunWith(Parameterized.class)
public class AddHoursToDateTest {

    private final Date dActual;
    private final Date dEsperado;
    private final int dHoras;

    private DateManager dateManager;

    public AddHoursToDateTest(Date dActual, Date dEsperado, int dHoras) {
        this.dActual = dActual;
        this.dEsperado = dEsperado;
        this.dHoras = dHoras;
    }

    @Before
    public void setUp() {
        dateManager = new DateManager();
    }

    @Parameterized.Parameters(name = "{index}: {0} = addHoursToJavaUtilDate({1},{2})")
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][]{
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-03-13 14:00:00"),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-03-13 15:00:00"), 1},
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-03-13 14:00:00"),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-03-14 14:00:00"), 24},
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-12-31 23:59:59"),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-01-01 09:59:59"), 10},
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-10-12 08:00:00"),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-10-13 04:00:00"), 20},
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-09-12 10:00:00"),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-09-12 15:00:00"), 5}
        });
    }

    @Test
    public void add_hours_to_date() {
        assertEquals(dEsperado, dateManager.addHoursToJavaUtilDate(dActual, dHoras));
    }
}
