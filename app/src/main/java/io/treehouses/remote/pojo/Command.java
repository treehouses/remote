package io.treehouses.remote.pojo;

public class Command {
    String command;
    String start = null, stop = null;
    String response;

    public Command(String msg) {
        this.command = msg;
    }

    public Command(String msg, String start, String stop) {
        this.command = msg;
        this.start = start;
        this.stop = stop;
    }
}
