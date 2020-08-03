# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn javax.annotation.**
-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class retrofit.** { *; }
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn retrofit.**

-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
   long producerNode;
   long consumerNode;
}

-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class retrofit.** { *; }
-keep class com.google.android.** { *; }
-dontwarn com.google.android.**



-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-keep class com.jakewharton.** { *; }
-dontwarn com.jakewharton.**
-keep class com.google.** { *; }
-dontwarn com.google.**

-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

-keep class android.support.v7.app.** { *; }
-keep interface android.support.v7.app.** { *; }

-keep class android.support.v7.widget.** { *; }
-keep class android.support.v4.widget.** { *; }


-keep class android.support.** { *; }
-dontwarn android.support.**

-keep class com.wdullaer.** { *; }
-dontwarn  com.wdullaer.**

-keep class android.support.design.widget.** { *; }
-keep class android.support.**{*;}
-dontwarn android.support.**

-keepattributes Signature
-keepattributes Annotation
-keep class okhttp3.**{*;}
-keep interface okhttp3.**{*;}
-dontwarn okhttp3.**
-keep class okio.**{*;}
-keep interface okio.**{*;}
-dontwarn okio.**
-keep class com.fasterxml.jackson.** {*;}
-dontwarn com.fasterxml.jackson.**

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-keep class com.google.** { *; }
-dontwarn com.google.**
-keep class com.hornet.** { *; }
-dontwarn com.hornet.**
-keep class com.github.szugyi.** { *; }
-dontwarn com.github.szugyi.**

-keep class com.crystal.** { *; }
-dontwarn com.crystal.**

-keep class com.github.AnkitKiet.** { *; }
-dontwarn com.github.AnkitKiet.**

-keep class co.lujun.** { *; }
-dontwarn co.lujun.**

-keep class de.hdodenhof.** { *; }
-dontwarn de.hdodenhof.**


-keep class com.miguelcatalan.** { *; }
-dontwarn com.miguelcatalan.**


-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

-keep class com.github.hkk595.** { *; }
-keep interface com.github.hkk595.** { *; }

-keep class com.github.AnkitKiet.** { *; }
-keep interface com.github.AnkitKiet.** { *; }

-keep class com.mikepenz.** { *; }
-keep interface com.mikepenz.** { *; }

-keep class com.github.nkzawa.** { *; }
-keep interface com.github.nkzawa.** { *; }

-keep class io.treehouses.remote.pojo.** { *; }
-keep class io.treehouses.remote.SSH.beans.** { *; }

-keep public class com.trilead.ssh2.compression.**
-keep public class com.trilead.ssh2.crypto.**

-keep class org.conscrypt.** { *; }



