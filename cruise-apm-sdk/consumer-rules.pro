# Consumer ProGuard rules for CruiseAPM SDK
# Packaged into the AAR and applied automatically to any app consuming CruiseAPM.

# Preserve Public Entrypoint and Configuration
-keep public class com.cruise.apm.CruiseApm {
    public *;
}
-keep public class com.cruise.apm.CruiseApmConfig {
    public *;
}
-keep public class com.cruise.apm.CruiseApmConfig$* {
    public *;
}
-keep public interface com.cruise.apm.CruiseApmCallback {
    public *;
}

# Preserve Trace API
-keep public class com.cruise.apm.trace.** {
    public *;
}

# Preserve Public Data Models
-keep class com.cruise.apm.model.** {
    public *;
}

# Preserve Network Interceptor
-keep public class com.cruise.apm.network.CruiseApmOkHttpInterceptor {
    public *;
}
