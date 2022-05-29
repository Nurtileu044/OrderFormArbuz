package kz.ablazim.orderformarbuz.validator

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

@Parcelize
class DateRangeValidator(private val duration: Long) : CalendarConstraints.DateValidator {
    @IgnoredOnParcel
    var datePicker: WeakReference<MaterialDatePicker<Long?>>? = null

    @IgnoredOnParcel
    private val periodInMillis = TimeUnit.DAYS.toMillis(duration)

    override fun isValid(date: Long): Boolean {
        val current = System.currentTimeMillis()
        return current < date && date < current + periodInMillis
    }
}