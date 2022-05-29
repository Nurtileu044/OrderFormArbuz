package kz.ablazim.orderformarbuz.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = viewModelScope.coroutineContext

    protected fun CoroutineScope.launchSafe(
        start: (() -> Unit)? = null,
        body: suspend () -> Unit,
        handleError: (Throwable) -> Unit,
        finish: ((Boolean) -> Unit)? = null
    ): Job =
        launch {
            var error: Throwable? = null
            try {
                start?.invoke()
                body.invoke()
            } catch (e: Throwable) {
                error = e
                handleError(e)
            } finally {
                finish?.invoke(error == null)
            }
        }
}

interface Action