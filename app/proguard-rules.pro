# 제네릭 및 라인 넘버 유지
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Kotlin 메타데이터 유지
-keep class kotlin.Metadata { *; }

# 코루틴 Continuation 유지
-keep class kotlin.coroutines.Continuation

# Kotlinx Serialization 규칙
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keepclassmembers class * {
    static ** Companion;
}
-keepclassmembers class * {
    static ** serializer();
}
# 생성된 Serializer 클래스 보호
-keep class **$$serializer { *; }

# 앱 데이터 모델 보호
-keep class com.kjw.fridgerecipe.data.remote.** { *; }
-keep class com.kjw.fridgerecipe.domain.model.** { *; }

# 릴리스 빌드에서 로그 제거 (Log.d, Log.v, Log.i 등)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
#    public static int w(...);
    public static int d(...);
#    public static int e(...);
}