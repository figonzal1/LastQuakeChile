package cl.figonzal.lastquakechile.core.utils

import cl.figonzal.lastquakechile.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ViewsExtKtTest {


    @Test
    fun `magnitude color return correct values`() {
        val expected1 = R.color.magnitude1
        val expected2 = R.color.magnitude2
        val expected3 = R.color.magnitude3
        val expected7 = R.color.magnitude7
        val expected8 = R.color.magnitude8

        val expectedElse = R.color.colorPrimary

        assertThat(getMagnitudeColor(0.9, false)).isEqualTo(expectedElse)

        assertThat(getMagnitudeColor(1.49, false)).isEqualTo(expected1)
        assertThat(getMagnitudeColor(1.51, false)).isEqualTo(expected1)
        assertThat(getMagnitudeColor(1.99, false)).isEqualTo(expected1)

        assertThat(getMagnitudeColor(2.49, false)).isEqualTo(expected2)
        assertThat(getMagnitudeColor(2.51, false)).isEqualTo(expected2)
        assertThat(getMagnitudeColor(2.99, false)).isEqualTo(expected2)

        assertThat(getMagnitudeColor(3.49, false)).isEqualTo(expected3)
        assertThat(getMagnitudeColor(3.51, false)).isEqualTo(expected3)
        assertThat(getMagnitudeColor(3.99, false)).isEqualTo(expected3)

        assertThat(getMagnitudeColor(8.49, false)).isEqualTo(expected8)
        assertThat(getMagnitudeColor(9.51, false)).isEqualTo(expected8)
        assertThat(getMagnitudeColor(7.99, false)).isEqualTo(expected7)
    }

    @Test
    fun `magnitude color return correct values for Map`() {
        val expected1 = R.color.magnitude1_alpha
        val expected2 = R.color.magnitude2_alpha
        val expected3 = R.color.magnitude3_alpha
        val expected7 = R.color.magnitude7_alpha
        val expected8 = R.color.magnitude8_alpha

        val expectedElse = R.color.colorPrimary

        assertThat(getMagnitudeColor(0.9, true)).isEqualTo(expectedElse)

        assertThat(getMagnitudeColor(1.49, true)).isEqualTo(expected1)
        assertThat(getMagnitudeColor(1.51, true)).isEqualTo(expected1)
        assertThat(getMagnitudeColor(1.99, true)).isEqualTo(expected1)

        assertThat(getMagnitudeColor(2.49, true)).isEqualTo(expected2)
        assertThat(getMagnitudeColor(2.51, true)).isEqualTo(expected2)
        assertThat(getMagnitudeColor(2.99, true)).isEqualTo(expected2)

        assertThat(getMagnitudeColor(3.49, true)).isEqualTo(expected3)
        assertThat(getMagnitudeColor(3.51, true)).isEqualTo(expected3)
        assertThat(getMagnitudeColor(3.99, true)).isEqualTo(expected3)

        assertThat(getMagnitudeColor(8.49, true)).isEqualTo(expected8)
        assertThat(getMagnitudeColor(9.51, true)).isEqualTo(expected8)
        assertThat(getMagnitudeColor(7.99, true)).isEqualTo(expected7)
    }
}