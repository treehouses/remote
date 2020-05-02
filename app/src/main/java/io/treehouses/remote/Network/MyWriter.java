package io.treehouses.remote.Network;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;

public class MyWriter implements Subscriber<Byte> {
    ArrayList<Byte> buffer = new ArrayList<>();
    Subscriber subscriber;
    private static int DELIMITER = '~';

    public MyWriter(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(Subscription s) {
        subscriber.onSubscribe(s);
    }

    @Override
    public void onNext(Byte b) {
        if (b == DELIMITER) emit();
        else buffer.add(b);
    }

    @Override
    public void onError(Throwable t) {
        if (!buffer.isEmpty()) emit();
        subscriber.onError(t);
    }

    @Override
    public void onComplete() {
        if (!buffer.isEmpty()) emit();
        subscriber.onComplete();
    }

    private void emit() {
        if (buffer.isEmpty()) {
            subscriber.onNext("");
            return;
        }

        byte[] bArray = new byte[buffer.size()];

        for (int i = 0; i < buffer.size(); i++) bArray[i] = buffer.get(i);
        subscriber.onNext(new String(bArray));
        buffer.clear();
    }
}
