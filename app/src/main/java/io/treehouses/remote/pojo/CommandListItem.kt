package io.treehouses.remote.pojo

class CommandListItem (private var title: String, private var command:String) {

    fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title!!
    }

    fun getCommand(): String {
        return command
    }

    fun setCommand(command: String?) {
        this.command = command!!
    }

}