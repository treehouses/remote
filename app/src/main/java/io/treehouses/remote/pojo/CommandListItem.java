package io.treehouses.remote.pojo;

public class CommandListItem {
    String title, command;

    public CommandListItem(String title, String command) {
        this.title = title;
        this.command = command;
    }

    public CommandListItem() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
