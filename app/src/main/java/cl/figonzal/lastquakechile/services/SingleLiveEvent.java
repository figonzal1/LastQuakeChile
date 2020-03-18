package cl.figonzal.lastquakechile.services;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

public class SingleLiveEvent<T> extends MutableLiveData<T> {

    private final LiveData<T> liveDataToObserver;
    private final AtomicBoolean status = new AtomicBoolean(false);

    public SingleLiveEvent() {
        final MediatorLiveData<T> outputLiveData = new MediatorLiveData<>();
        outputLiveData.addSource(this, new Observer<T>() {
            @Override
            public void onChanged(T currentValue) {
                outputLiveData.setValue(currentValue);
                status.set(false);
            }
        });
        liveDataToObserver = outputLiveData;
    }

    @MainThread
    public void observe(@NonNull LifecycleOwner owner,
                        @NonNull final Observer<? super T> observer) {

        /*if (hasActiveObservers()) {
            Log.w("SINGLE_LIVE_DATA", "Multiple observers registered but only one will be " +
                    "notified of changes.");
        }
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                if (status.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }
            }
        });*/
        liveDataToObserver.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                if (status.get()) {
                    observer.onChanged(t);
                }
            }
        });
    }

    @MainThread
    public void setValue(T t) {
        status.set(true);
        super.setValue(t);
    }

    @SuppressWarnings("unused")
    public void call() {
        setValue(null);
    }


}
