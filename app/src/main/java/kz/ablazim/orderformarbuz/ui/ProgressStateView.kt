package kz.ablazim.orderformarbuz.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.use
import kz.ablazim.orderformarbuz.R
import kz.ablazim.orderformarbuz.databinding.ViewProgressStateBinding

class ProgressStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = ViewProgressStateBinding.inflate(LayoutInflater.from(context), this, true)

    var text: String
        get() = binding.textTextView.text.toString()
        set(value) {
            binding.textTextView.text = value
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ProgressStateView, defStyleAttr, 0).use { attributes ->
            binding.textTextView.text = attributes.getString(R.styleable.ProgressStateView_text).orEmpty()
        }
    }
}