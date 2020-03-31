package io.treehouses.remote.pojo;

public class Command {
    String msg;
    String start, stop;

    public Command(String msg) {
        this.msg = msg;
    }

    public Command(String msg, String start, String stop) {
        this.msg = msg;
        this.start = start;
        this.stop = stop;
    }
}
