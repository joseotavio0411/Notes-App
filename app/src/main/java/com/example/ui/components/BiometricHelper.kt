package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.widget.Toast

/**
 * Finds the Activity associated with a given Context, unwrapping any ContextWrapper.
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object BiometricHelper {
    /**
     * Checks if the device has biometric support available or capable.
     */
    fun canAuthenticate(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val biometricManager = context.getSystemService(BiometricManager::class.java)
            if (biometricManager != null) {
                val status = biometricManager.canAuthenticate()
                // Return true if supported or if user has not enrolled yet (so we can prompt them)
                return status != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageManager = context.packageManager
            return packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_FINGERPRINT) ||
                    packageManager.hasSystemFeature("android.hardware.biometrics.face")
        }
        return false
    }

    /**
     * Authenticates using device biometrics on supported Android versions,
     * or invokes onError with clear user-friendly guidance.
     */
    fun authenticate(
        activity: Activity,
        title: String = "Desbloquear Nota",
        subtitle: String = "Confirme sua identidade",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Pre-check for clear messaging if none enrolled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val biometricManager = activity.getSystemService(BiometricManager::class.java)
                if (biometricManager != null) {
                    val status = biometricManager.canAuthenticate()
                    when (status) {
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                            onError("Nenhuma biometria cadastrada no aparelho. Cadastre sua impressão digital nas configurações do Android ou use seu PIN.")
                            return
                        }
                        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                            onError("Este dispositivo não possui leitor biométrico. Use seu PIN.")
                            return
                        }
                        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                            onError("Sensor biométrico temporariamente indisponível. Use seu PIN.")
                            return
                        }
                        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                            onError("Atualização de segurança necessária para biometria. Use seu PIN.")
                            return
                        }
                        else -> { /* Proceed to BiometricPrompt */ }
                    }
                }
            }

            try {
                val cancellationSignal = CancellationSignal()
                val executor = activity.mainExecutor

                val prompt = BiometricPrompt.Builder(activity)
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButton("Usar PIN", executor) { _, _ ->
                        onError("Autenticação biométrica cancelada. Digite seu PIN.")
                    }
                    .build()

                prompt.authenticate(
                    cancellationSignal,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            activity.runOnUiThread {
                                onSuccess()
                            }
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errorCode, errString)
                            activity.runOnUiThread {
                                if (errorCode == BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED ||
                                    errorCode == BiometricPrompt.BIOMETRIC_ERROR_NEGATIVE_BUTTON
                                ) {
                                    onError("Autenticação biométrica cancelada. Digite seu PIN.")
                                } else if (errorCode == BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS) {
                                    onError("Nenhuma biometria cadastrada no aparelho. Cadastre nas configurações do Android ou use seu PIN.")
                                } else {
                                    onError(errString?.toString() ?: "Erro na autenticação biométrica")
                                }
                            }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            activity.runOnUiThread {
                                Toast.makeText(activity, "Biometria não reconhecida. Tente novamente.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Biometria indisponível neste dispositivo")
            }
        } else {
            onError("Biometria requer Android 9 ou superior")
        }
    }
}
