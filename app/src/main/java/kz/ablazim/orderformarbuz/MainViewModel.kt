package kz.ablazim.orderformarbuz

import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import kz.ablazim.orderformarbuz.base.Action
import kz.ablazim.orderformarbuz.base.BaseViewModel
import kz.ablazim.orderformarbuz.base.SingleLiveEvent
import timber.log.Timber
import java.math.BigDecimal
import java.util.*

class MainViewModel : BaseViewModel() {
    private val _actions = SingleLiveEvent<MainAction>()
    val actions: LiveData<MainAction> = _actions

    private val _progressLoading = MutableLiveData(false)
    val progressLoading: LiveData<Boolean> = _progressLoading

    private val _date = MutableLiveData<Date?>()
    val date: LiveData<Date?> = _date

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    private val oneArbuzAmount = BigDecimal(200)
    private val maxProductCount = BigDecimal(3)
    private val minProductCount = BigDecimal(1)
    private var updatedPrice = oneArbuzAmount
    private var selectedRow = ""
    private var selectedPlace = ""

    fun setPeriod(value: Date?) {
        _date.value = value
    }

    fun getRows(): List<String> = listOf("1", "2", "3", "4")

    fun getPlaces(): List<String> = listOf("10", "11", "12", "13", "14", "15", "16")

    fun selectRow(row: String) {
        selectedRow = row
    }

    fun selectPlace(place: String) {
        selectedPlace = place
    }

    fun onCheckStatusButtonClicked(row: String, place: String) {
        val errorActions = mutableListOf<MainAction>()
        if (row.isEmpty()) errorActions.add(
            MainAction.ShowFieldError(
                Field.ROW,
                R.string.row_of_garden_bed_should_be_selected
            )
        )

        if (place.isEmpty()) errorActions.add(
            MainAction.ShowFieldError(
                Field.PLACE,
                R.string.place_of_garden_bed_should_be_selected
            )
        )

        if (errorActions.isNotEmpty()) {
            errorActions.forEach { _actions.value = it }
            return
        }
        val statusInt = Random().nextInt(4 - 1) + 1
        launchSafe(
            start = { _progressLoading.value = true },
            finish = { _progressLoading.value = false },
            body = {
                delay(1000L)
                _status.postValue(
                    when (statusInt) {
                        1 -> "mature"
                        2 -> "immature"
                        else -> "already thwarted"
                    }
                )
            },
            handleError = {
                Timber.e(it)
            }
        )
    }

    fun onPlusButtonClicked(count: String) {
        if (count.toBigDecimal() >= maxProductCount) {
            _actions.value = MainAction.ShowMessage(R.string.count_caanot_more_than_3)
            return
        }

        updatedPrice = updatedPrice.plus(BigDecimal(200))
        _actions.value = MainAction.ChangeCountSuccess(
            count = count.toBigDecimal().inc().toString(),
            totalAmount = updatedPrice.toString()
        )
    }

    fun onMinusButtonClicked(count: String) {
        if (count.toBigDecimal() <= minProductCount) {
            _actions.value = MainAction.ShowMessage(R.string.count_cannot_be_less_than_1)
            return
        }

        updatedPrice = updatedPrice.minus(BigDecimal(200))
        _actions.value = MainAction.ChangeCountSuccess(
            count = count.toBigDecimal().dec().toString(),
            totalAmount = updatedPrice.toString()
        )
    }

    fun onSlicesCheckBoxClicked(isChecked: Boolean) {
        if (isChecked) {
            updatedPrice = updatedPrice.plus(BigDecimal(100))
            _actions.value = MainAction.ChangeTotalAmount(updatedPrice.toString())
        } else {
            updatedPrice = updatedPrice.minus(BigDecimal(100))
            _actions.value = MainAction.ChangeTotalAmount(updatedPrice.toString())
        }
    }

    fun onMenuItemClicked(item: MenuItem) {
        if (item.itemId == R.id.clear) {
            _actions.value = MainAction.RefreshPage(Field.CANCEL)
        }
    }

    fun setPrice(price: BigDecimal) {
        updatedPrice = price
        _actions.value = MainAction.UpdateTotalAmount(updatedPrice.toString())
    }

    fun onConfirmButtonClicked(address: String, phone: String, date: String) {
        val errorActions = mutableListOf<MainAction>()
        var isPhoneNumberFractional = false
        if (address.isEmpty()) errorActions.add(
            MainAction.ShowFieldError(
                Field.ADDRESS,
                R.string.address_field_cannot_be_empty
            )
        )

        if (phone.isEmpty()) errorActions.add(
            MainAction.ShowFieldError(
                Field.PHONE,
                R.string.phone_field_cannot_be_empty
            )
        )

        if (phone.isNotEmpty() && phone.length != 11) errorActions.add(
            MainAction.ShowFieldError(
                Field.PHONE,
                R.string.phone_length_should_not_be_less_than_11_starting_with_87
            )
        ) else if (phone.isNotEmpty() && phone.length == 11) {
            isPhoneNumberFractional = phone.toBigDecimal().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 1
        }

        if (isPhoneNumberFractional) errorActions.add(
            MainAction.ShowFieldError(
                Field.PHONE,
                R.string.phone_number_cannot_be_fractional
            )
        )

        if (date.isEmpty()) errorActions.add(
            MainAction.ShowFieldError(
                Field.DATE,
                R.string.date_field_should_be_selected
            )
        )

        if (selectedRow.isEmpty()) errorActions.add(
            MainAction.ShowFieldError(
                Field.ROW,
                R.string.row_of_garden_bed_should_be_selected
            )
        )

        if (selectedPlace.isEmpty()) errorActions.add(
            MainAction.ShowFieldError(
                Field.PLACE,
                R.string.place_of_garden_bed_should_be_selected
            )
        )
        if (errorActions.isNotEmpty()) {
            errorActions.forEach { _actions.value = it }
            return
        }
        launchSafe(
            start = { _progressLoading.value = true },
            finish = { _progressLoading.value = false },
            body = {
                delay(1000L)
                _actions.postValue(MainAction.RefreshPage(Field.CONFIRM))
            },
            handleError = {
                Timber.e(it)
            }
        )
    }

    enum class Field {
        CONFIRM, CANCEL, ADDRESS, PHONE, DATE, ROW, PLACE
    }
}

sealed class MainAction : Action {
    data class ShowMessage(@StringRes val localMessage: Int = 0) : MainAction()
    data class ShowFieldError(val field: MainViewModel.Field, @StringRes val localMessage: Int = 0) : MainAction()
    data class ChangeCountSuccess(val count: String, val totalAmount: String) : MainAction()
    data class ChangeTotalAmount(val totalAmount: String) : MainAction()
    data class RefreshPage(val field: MainViewModel.Field) : MainAction()
    data class UpdateTotalAmount(val totalAmount: String) : MainAction()
}