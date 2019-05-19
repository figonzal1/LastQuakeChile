package cl.figonzal.lastquakechile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import cl.figonzal.lastquakechile.services.QuakeUtils;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MagnitudeColorUnitTest {

	private final double magnitud;
	private final int colorEsperado;

	public MagnitudeColorUnitTest(double magnitud, int colorEsperado) {
		this.colorEsperado = colorEsperado;
		this.magnitud = magnitud;
	}

	@Parameterized.Parameters(name = "{0} = getMagnitudeColor({1})")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{1.2, R.color.magnitude1}
				, {1.9, R.color.magnitude1}
				, {2.4, R.color.magnitude2}
				, {2.5, R.color.magnitude2}
				, {3.4, R.color.magnitude3}
				, {3.5, R.color.magnitude3}
				, {4.3, R.color.magnitude4}
				, {4.5, R.color.magnitude4}
				, {5.4, R.color.magnitude5}
				, {5.5, R.color.magnitude5}
				, {6.4, R.color.magnitude6}
				, {6.5, R.color.magnitude6}
				, {7.4, R.color.magnitude7}
				, {7.5, R.color.magnitude7}
				, {8.4, R.color.magnitude8}
				, {8.5, R.color.magnitude8}
		});
	}

	@Test
	public void give_magnitude_return_magnitud_color() {
		assertEquals(colorEsperado, QuakeUtils.getMagnitudeColor(magnitud, false));
	}
}
