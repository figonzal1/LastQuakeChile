package cl.figonzal.lastquakechile;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

public class CrashlyticsTree extends Timber.Tree {


    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {

        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return;
        } else {

            FirebaseCrashlytics.getInstance().log(message);
            if (t != null) {
                FirebaseCrashlytics.getInstance().recordException(t);
            }
        }
    }
}
