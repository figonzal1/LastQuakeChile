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

import static junit.framework.TestCase.assertEquals;

@RunWith(Parameterized.class)
public class StringToDateTest {

    private final String sActual;
    private final Date dEsperada;
    private Context context;
    private DateHandler dateHandler;

    public StringToDateTest(String sActual, Date dEsperada) {
        this.sActual = sActual;
        this.dEsperada = dEsperada;
    }

    @NonNull
    @Parameterized.Parameters(name = "{0} = stringToDate({1})")
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][]{
                {"2019-03-13 14:00:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse("2019-03-13 14:00:00")}
                , {"2019-08-09 04:45:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .parse("2019-08-09 04:45:00")}
                , {"2020-12-12 15:25:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .parse("2020-12-12 15:25:00")}
                , {"2022-12-31 23:59:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .parse("2022-12-31 23:59:00")}
                , {"2011-01-01 02:59:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .parse("2011-01-01 02:59:00")}
                , {"2020-02-29 02:59:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .parse("2020-02-29 02:59:00")}
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
    public void string_to_date() {

        Date dActual = null;
        try {
            dActual = dateHandler.stringToDate(context, sActual);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertEquals(dEsperada, dActual);

    }
}
