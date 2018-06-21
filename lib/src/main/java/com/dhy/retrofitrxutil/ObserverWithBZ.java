package com.dhy.retrofitrxutil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class ObserverWithBZ<T> implements Observer<T> {
    private static IErrorHandler defaultErrorHandler;
    private static StyledProgressGenerator defaultStyledProgressGenerator;

    public static void setDefaultErrorHandler(IErrorHandler defaultErrorHandler) {
        ObserverWithBZ.defaultErrorHandler = defaultErrorHandler;
    }

    public static void setDefaultStyledProgressGenerator(StyledProgressGenerator defaultStyledProgressGenerator) {
        ObserverWithBZ.defaultStyledProgressGenerator = defaultStyledProgressGenerator;
    }

    @Nullable
    private Context context;
    protected StyledProgress styledProgress;
    protected final boolean successOnly, autoDismiss;
    private Disposable disposable;

    public ObserverWithBZ(@Nullable Context context) {
        this(context, true, true);
    }

    /**
     * @param autoDismiss autoDismissProgress
     */
    public ObserverWithBZ(@Nullable Context context, boolean successOnly, boolean autoDismiss) {
        this.context = context;
        this.successOnly = successOnly;
        this.autoDismiss = autoDismiss;
        styledProgress = getStyledProgress();
        if (styledProgress == null) styledProgress = StyledProgressOfNone.getInstance();
    }

    @Nullable
    public Context getContext() {
        return context;
    }

    /**
     * Override this for custom progress
     */
    @Nullable
    protected StyledProgress getStyledProgress() {
        return defaultStyledProgressGenerator.generate(this);
    }

    @Override
    public void onSubscribe(Disposable disposable) {
        this.disposable = disposable;
        styledProgress.showProgress();
        if (context instanceof IDisposable) {
            ((IDisposable) context).registerDisposable(context, disposable);
        }
    }

    @Override
    public void onNext(T t) {
        if (successOnly && t instanceof IResponseStatus) {
            IResponseStatus status = (IResponseStatus) t;
            if (status.isSuccess()) {
                onResponse(t);
            } else {
                onError(new ThrowableBZ(status));
            }
        } else onResponse(t);
    }

    /**
     * Override this for custom ErrorHandler
     */
    @NonNull
    protected IErrorHandler getErrorHandler() {
        return defaultErrorHandler;
    }

    @Override
    public void onError(Throwable e) {
        getErrorHandler().onError(this, e);
    }

    @Override
    public void onComplete() {
        cancel();
        if (autoDismiss) dismissProgress();
    }

    public void showProgress() {
        styledProgress.showProgress();
    }

    public void dismissProgress() {
        styledProgress.dismissProgress();
    }

    protected abstract void onResponse(T response);

    public void cancel() {
        if (disposable != null) {
            disposable.dispose();
            if (context instanceof IDisposable) {
                ((IDisposable) context).onComplete(context, disposable);
            }
        }
    }
}