package cl.figonzal.lastquakechile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cl.figonzal.lastquakechile.services.QuakeUtils;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class LatLongToDMSUnitTest {

    @Parameterized.Parameters(name = "{0} = {1}")
    public static Collection<Object[]> data() {

        Map<String, Double> map1 = new HashMap<>();
        map1.put("grados", 100.0);
        map1.put("minutos", 8.0);
        map1.put("segundos", 29.0);

        Map<String, Double> map2 = new HashMap<>();
        map2.put("grados", 25.0);
        map2.put("minutos", 38.0);
        map2.put("segundos", 60.0);

        Map<String, Double> map3 = new HashMap<>();
        map3.put("grados", 87.0);
        map3.put("minutos", 27.0);
        map3.put("segundos", 23.0);

        Map<String, Double> map4 = new HashMap<>();
        map4.put("grados", 0.0);
        map4.put("minutos", 39.0);
        map4.put("segundos", 27.0);

        return Arrays.asList(new Object[][]{
                {100.141312, map1}
                , {-25.65, map2}
                , {87.456340, map3}
                , {-0.657599, map4}
        });
    }

    public LatLongToDMSUnitTest (double actual_lat_long, Map<String, Double> map_esperado) {
        this.actual_lat_long = actual_lat_long;
        this.map_esperado = map_esperado;
    }

    private final double actual_lat_long;
    private final Map<String, Double> map_esperado;

    @Test
    public void give_lat_or_long_return_dms() {
	    assertEquals(map_esperado, QuakeUtils.latLonToDMS(actual_lat_long));
    }
}
