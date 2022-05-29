package kz.ablazim.orderformarbuz

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import kz.ablazim.orderformarbuz.databinding.ActivityMainBinding
import kz.ablazim.orderformarbuz.util.FormatHelper
import kz.ablazim.orderformarbuz.validator.DateRangeValidator
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.util.*

private const val PERIOD_IN_DAYS = 9L
private const val DATE_FORMAT = "%s"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rowsAdapter: ArrayAdapter<String> by lazy {
            ArrayAdapter(this, R.layout.item_auto_complete_dropdown)
        }

        val placesAdapter: ArrayAdapter<String> by lazy {
            ArrayAdapter(this, R.layout.item_auto_complete_dropdown)
        }

        rowsAdapter.addAll(viewModel.getRows())
        placesAdapter.addAll(viewModel.getPlaces())

        with(binding) {
            toolbar.setOnMenuItemClickListener { menuItem ->
                viewModel.onMenuItemClicked(menuItem)
                true
            }
            sumAmountTextView.text = getString(R.string.total_amount, "200")
            statusTextView.text = getString(R.string.status, "undefined")
            slicesCheckBox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onSlicesCheckBoxClicked(isChecked)
            }

            customerAddressTextInputEditText.doAfterTextChanged {
                customerAddressTextInputLayout.isErrorEnabled = false
            }
            customerPhoneTextInputEditText.doAfterTextChanged { customerPhoneTextInputLayout.isErrorEnabled = false }
            chooseDateTextInputEditText.doAfterTextChanged { chooseDateInputLayout.isErrorEnabled = false }

            rowGardenBedTextInputAutoComplete.doAfterTextChanged { rowGardenBedTextInputLayout.isErrorEnabled = false }
            rowGardenBedTextInputAutoComplete.setAdapter(rowsAdapter)
            rowGardenBedTextInputAutoComplete.setOnItemClickListener { _, _, position, _ ->
                rowGardenBedTextInputLayout.isErrorEnabled = false
                viewModel.selectRow(rowsAdapter.getItem(position) ?: "empty")
            }
            rowGardenBedTextInputAutoComplete.setBackgroundResource(R.color.white)

            placeGardenBedTextInputAutoComplete.doAfterTextChanged {
                placeGardenBedTextInputLayout.isErrorEnabled = false
            }
            placeGardenBedTextInputAutoComplete.setAdapter(placesAdapter)
            placeGardenBedTextInputAutoComplete.setOnItemClickListener { _, _, position, _ ->
                placeGardenBedTextInputLayout.isErrorEnabled = false
                viewModel.selectPlace(placesAdapter.getItem(position) ?: "empty")
            }
            placeGardenBedTextInputAutoComplete.setBackgroundResource(R.color.white)

            plusButton.setOnClickListener {
                viewModel.onPlusButtonClicked(countTextView.text.toString())
            }

            minusButton.setOnClickListener {
                viewModel.onMinusButtonClicked(countTextView.text.toString())
            }

            checkStatusButton.setOnClickListener {
                viewModel.onCheckStatusButtonClicked(
                    row = rowGardenBedTextInputAutoComplete.editableText.toString(),
                    place = placeGardenBedTextInputAutoComplete.editableText.toString()
                )
            }

            confirmButton.setOnClickListener {
                viewModel.onConfirmButtonClicked(
                    address = customerAddressTextInputEditText.editableText.toString(),
                    phone = customerPhoneTextInputEditText.editableText.toString(),
                    date = chooseDateTextInputEditText.editableText.toString()
                )
            }

            dateButton.setOnClickListener {
                val dateRangeValidator = DateRangeValidator(PERIOD_IN_DAYS)
                MaterialDatePicker.Builder.datePicker()
                    .setTheme(R.style.MaterialCalendarTheme)
                    .setTitleText(R.string.date)
                    .setCalendarConstraints(
                        CalendarConstraints.Builder().setValidator(dateRangeValidator).build()
                    )
                    .apply {
                        if (viewModel.date.value != null) {
                            setSelection(viewModel.date.value?.time)
                        }
                    }
                    .build()
                    .also {
                        dateRangeValidator.datePicker = WeakReference(it)
                        it.addOnPositiveButtonClickListener { date ->
                            viewModel.setPeriod(Date(date))
                        }
                    }
                    .show(this@MainActivity.supportFragmentManager, null)
            }

            viewModel.date.observe(this@MainActivity) {
                chooseDateTextInputEditText.setText(
                    it?.let {
                        String.format(
                            DATE_FORMAT,
                            FormatHelper.getDate(it)
                        )
                    }.orEmpty()
                )
            }

            viewModel.status.observe(this@MainActivity) { status ->
                statusTextView.text = getString(R.string.status, status)
            }

            viewModel.progressLoading.observe(this@MainActivity) { isLoading ->
                progressStateView.isVisible = isLoading
            }

            viewModel.actions.observe(this@MainActivity) { action ->
                when (action) {
                    is MainAction.ShowMessage -> {
                        Toast.makeText(
                            this@MainActivity,
                            getString(action.localMessage),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is MainAction.ChangeCountSuccess -> {
                        countTextView.text = action.count
                        sumAmountTextView.text = getString(R.string.total_amount, action.totalAmount)
                    }
                    is MainAction.ChangeTotalAmount -> sumAmountTextView.text =
                        getString(R.string.total_amount, action.totalAmount)
                    is MainAction.RefreshPage -> {
                        resetTheFields()
                        when (action.field) {
                            MainViewModel.Field.CONFIRM -> Toast.makeText(
                                this@MainActivity,
                                getString(R.string.order_confirmed),
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> Timber.e("Could not be confirmed")
                        }
                    }
                    is MainAction.UpdateTotalAmount -> sumAmountTextView.text =
                        getString(R.string.total_amount, action.totalAmount)
                    is MainAction.ShowFieldError -> showFieldError(
                        textInputLayout = when (action.field) {
                            MainViewModel.Field.ADDRESS -> customerAddressTextInputLayout
                            MainViewModel.Field.DATE -> chooseDateInputLayout
                            MainViewModel.Field.PHONE -> customerPhoneTextInputLayout
                            MainViewModel.Field.ROW -> rowGardenBedTextInputLayout
                            else -> placeGardenBedTextInputLayout
                        },
                        message = getString(action.localMessage)
                    )
                }
            }
        }
    }

    private fun showFieldError(textInputLayout: TextInputLayout, message: String) {
        textInputLayout.error = message
        textInputLayout.isErrorEnabled = true
    }

    private fun resetTheFields() {
        with(binding) {
            customerAddressTextInputEditText.editableText.clear()
            customerPhoneTextInputEditText.editableText.clear()
            chooseDateTextInputEditText.editableText.clear()
            rowGardenBedTextInputAutoComplete.editableText.clear()
            placeGardenBedTextInputAutoComplete.editableText.clear()
            statusTextView.text = getString(R.string.status, "undefined")
            countTextView.text = getString(R.string.one)
            slicesCheckBox.isChecked = false
            viewModel.setPrice(BigDecimal(200))
        }
    }
}