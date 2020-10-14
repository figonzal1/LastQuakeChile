package cl.figonzal.lastquakechile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(Parameterized.class)
public class UtcToLocalUnitTest {

	@Parameterized.Parameters(name = "{index}: {0} = utcToLocal({1})")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{"2019-03-13 14:00:00", "2019-03-13 11:00:00"}
				, {"2019-08-09 04:45:00", "2019-08-09 00:45:00"}
				, {"2020-12-12 15:25:00", "2020-12-12 12:25:00"}
				, {"2022-12-31 23:59:00", "2022-12-31 20:59:00"}
				, {"2011-01-01 02:59:00", "2010-12-31 23:59:00"}
				, {"2020-02-29 02:59:00", "2020-02-28 23:59:00"}
		});
	}

	private final String actual_utc;
	private final String esperado_local;

	public UtcToLocalUnitTest(String actual_utc, String esperado_local) {
		this.actual_utc = actual_utc;
		this.esperado_local = esperado_local;
	}

	@Test
	public void utcToLocal() {

		Date date_actual = null;

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {

			//Se testea quakeutils con actual
            date_actual = Utils.utcToLocal(Objects.requireNonNull(format.parse(actual_utc)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
        assertEquals(esperado_local, format.format(Objects.requireNonNull(date_actual)));

	}

}