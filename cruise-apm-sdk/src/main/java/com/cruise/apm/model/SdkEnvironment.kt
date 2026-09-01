package com.cruise.apm.model

/**
 * Backend environment target for CruiseAPM SDK.
 */
enum class SdkEnvironment(val endpointUrl: String) {
    SANDBOX("https://sandbox-telemetry.githubcruise.com/v1"),
    STAGING("https://staging-telemetry.githubcruise.com/v1"),
    PRODUCTION("https://telemetry.githubcruise.com/v1")
}
