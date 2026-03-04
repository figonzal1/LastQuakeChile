package cl.figonzal.lastquakechile.quake_feature.domain.model

import android.os.Parcel
import android.os.Parcelable

data class Quake(
    val quakeCode: Int,
    val localDate: String,
    val city: String,
    val reference: String,
    val magnitude: Double,
    val depth: Double,
    val scale: String,
    val isSensitive: Boolean,
    val isVerified: Boolean,
    val coordinate: Coordinate
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(Coordinate::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(quakeCode)
        parcel.writeString(localDate)
        parcel.writeString(city)
        parcel.writeString(reference)
        parcel.writeDouble(magnitude)
        parcel.writeDouble(depth)
        parcel.writeString(scale)
        parcel.writeByte(if (isSensitive) 1 else 0)
        parcel.writeByte(if (isVerified) 1 else 0)
        parcel.writeParcelable(coordinate, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Quake> {
        override fun createFromParcel(parcel: Parcel): Quake = Quake(parcel)
        override fun newArray(size: Int): Array<Quake?> = arrayOfNulls(size)
    }
}
