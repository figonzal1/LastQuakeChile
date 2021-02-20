package cl.figonzal.lastquakechile;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import cl.figonzal.lastquakechile.handlers.DateHandler;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DateToStringTest {

    private final String sEsperado;
    private final Date dActual;
    private Context context;
    private DateHandler dateHandler;

    public DateToStringTest(Date dActual, String sEsperado) {
        this.dActual = dActual;
        this.sEsperado = sEsperado;
    }

    @NonNull
    @Parameterized.Parameters(name = "{0} = dateToString({1})")
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][]{
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse("2019-03-13 14:00:00"), "2019-03-13 14:00:00"},
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse("2019-08-09 04:45:00"), "2019-08-09 04:45:00"},
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse("2020-12-12 15:25:00"), "2020-12-12 15:25:00"},
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse("2022-12-31 23:59:00"), "2022-12-31 23:59:00"},
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse("2011-01-01 02:59:00"), "2011-01-01 02:59:00"},
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse("2020-02-29 02:59:00"), "2020-02-29 02:59:00"}
        });
    }

    @Before
    public void setUp() {
        dateHandler = new DateHandler();
    }

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void date_to_string() {
        String sActual = dateHandler.dateToString(context, dActual);

        assertEquals(sEsperado, sActual);
    }
}
