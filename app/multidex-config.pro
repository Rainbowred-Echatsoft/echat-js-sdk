-keep public class * extends java.lang.Thread { *; }
-keep interface android.content.SharedPreferences { *; }
-keep class android.os.Handler { *; }
-keep class com.synaric.common.BaseSPKey { *; }
-keep class android.os.Messenger { *; }
-keep class android.content.Intent { *; }

-keep class com.blankj.utilcode.util.LogUtils { *; }
-keep class com.blankj.utilcode.util.PathUtils { *; }
-keep class com.blankj.utilcode.util.SPUtils { *; }
-keep class com.blankj.utilcode.util.Utils { *; }
-keep class com.blankj.utilcode.util.Utils$* { *; }
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Service


-keep class com.github.echatmulti.sample.** { *; }
-keep class android.support.multidex.** {*;}
#-keep class com.echat.** {*;}
#-keep class com.echatsoft.** {*;}
#-keep class org.cometd.** { *; }#ws用
-keep class com.pgyersdk.** { *; }
-keep class com.pgyersdk.**$* { *; }
-keep class com.umeng.** {*;}
-keep class com.taobao.** {*;}
-keep class android.arch.lifecycle.** { *; }
#-keep class javax.websocket.** {*;}
#-keep class org.slf4j.** { *; }#ws用
#-keep class org.dom4j.** { *; }
#-keep class org.eclipse.** { *; }#ws用
#-keep class com.googlecode.** { *; }
#-keep class org.apache.** { *; }