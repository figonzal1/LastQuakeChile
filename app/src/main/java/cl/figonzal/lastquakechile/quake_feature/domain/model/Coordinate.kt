package cl.figonzal.lastquakechile.quake_feature.domain.model

import android.os.Parcel
import android.os.Parcelable

data class Coordinate(
    val latitude: Double,
    val longitude: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Coordinate> {
        override fun createFromParcel(parcel: Parcel): Coordinate = Coordinate(parcel)
        override fun newArray(size: Int): Array<Coordinate?> = arrayOfNulls(size)
    }
}
