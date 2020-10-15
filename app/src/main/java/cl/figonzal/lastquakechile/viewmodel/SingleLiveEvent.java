package cl.figonzal.lastquakechile.viewmodel;

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
        outputLiveData.addSource(this, currentValue -> {

            outputLiveData.setValue(currentValue);
            status.set(false);
        });

        liveDataToObserver = outputLiveData;
    }

    @MainThread
    public void observe(@NonNull LifecycleOwner owner,
                        @NonNull final Observer<? super T> observer) {

        liveDataToObserver.observe(owner, t -> {

            if (status.get()) {

                observer.onChanged(t);
            }
        });
    }

    @MainThread
    public void setValue(T t) {

        status.set(true);
        super.setValue(t);
    }

    public void call() {
        setValue(null);
    }


}
