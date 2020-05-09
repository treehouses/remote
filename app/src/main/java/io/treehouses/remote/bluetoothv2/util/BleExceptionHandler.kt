package io.treehouses.remote.bluetoothv2.util


import android.app.Activity
import android.util.Log
import android.view.View
import com.polidea.rxandroidble2.exceptions.BleScanException
import io.treehouses.remote.R
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val ERROR_MESSAGES = mapOf(
        BleScanException.BLUETOOTH_NOT_AVAILABLE to R.string.error_bluetooth_not_available,
        BleScanException.BLUETOOTH_DISABLED to R.string.error_bluetooth_disabled,
        BleScanException.LOCATION_PERMISSION_MISSING to R.string.error_location_permission_missing,
        BleScanException.LOCATION_SERVICES_DISABLED to R.string.error_location_services_disabled,
        BleScanException.SCAN_FAILED_ALREADY_STARTED to R.string.error_scan_failed_already_started,
        BleScanException.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED to
                R.string.error_scan_failed_application_registration_failed,
        BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED to R.string.error_scan_failed_feature_unsupported,
        BleScanException.SCAN_FAILED_INTERNAL_ERROR to R.string.error_scan_failed_internal_error,
        BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES to R.string.error_scan_failed_out_of_hardware_resources,
        BleScanException.BLUETOOTH_CANNOT_START to R.string.error_bluetooth_cannot_start,
        BleScanException.UNKNOWN_ERROR_CODE to R.string.error_unknown_error
)

internal fun Activity.showError(exception: BleScanException, view : View) =
        getErrorMessage(exception).let { errorMessage ->
            Log.e("Scanning", errorMessage, exception)
            showSnackbarShort(errorMessage, view)
        }


private fun Activity.getErrorMessage(exception: BleScanException): String =
        if (exception.reason == BleScanException.UNDOCUMENTED_SCAN_THROTTLE) {
            getScanThrottleErrorMessage(exception.retryDateSuggestion)
        } else {
            ERROR_MESSAGES[exception.reason]?.let { errorResId ->
                getString(errorResId)
            } ?: run {
                Log.w("Scanning", String.format(getString(R.string.error_no_message), exception.reason))
                getString(R.string.error_unknown_error)
            }
        }

private fun Activity.getScanThrottleErrorMessage(retryDate: Date?): String =
        with(StringBuilder(getString(R.string.error_undocumented_scan_throttle))) {
            retryDate?.let { date ->
                String.format(
                        Locale.getDefault(),
                        getString(R.string.error_undocumented_scan_throttle_retry),
                        date.secondsUntil
                ).let { append(it) }
            }
            toString()
        }


private val Date.secondsUntil: Long
    get() = TimeUnit.MILLISECONDS.toSeconds(time - System.currentTimeMillis())